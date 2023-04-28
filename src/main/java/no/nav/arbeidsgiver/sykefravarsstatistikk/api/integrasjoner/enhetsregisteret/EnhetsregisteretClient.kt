package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.json.OverordnetEnhetResponseJson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import java.io.IOException

@Component
open class EnhetsregisteretClient(
    private val restTemplate: RestTemplate,
    @param:Value("\${enhetsregisteret.url}") private val enhetsregisteretUrl: String
) {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    open fun hentEnhet(orgnrTilEnhet: Orgnr): Either<HentEnhetFeil, OverordnetEnhet> {
        val url = "${enhetsregisteretUrl}/enheter/{orgnr}"
        return try {
            val overordnetEnhet = restTemplate
                .getForObject(url, OverordnetEnhetResponseJson::class.java, orgnrTilEnhet.verdi)!!
                .toDomain()
            validerReturnertOrgnr(orgnrTilEnhet, overordnetEnhet.orgnr)
                .getOrElse { return it.left() }
            overordnetEnhet.right()
        } catch (e: RestClientException) {
            log.error("Feil ved kall til Enhetsregisteret: ", e)
            HentEnhetFeil.FeilVedKallTilEnhetsregisteret.left()
        }
    }

    sealed class HentEnhetFeil {
        object FeilVedKallTilEnhetsregisteret : HentEnhetFeil()
        object OrgnrEksistererIkke : HentEnhetFeil()
    }

    open fun hentUnderenhet(orgnrTilUnderenhet: Orgnr): Underenhet {
        return try {
            val url = "${enhetsregisteretUrl}/underenheter/${orgnrTilUnderenhet.verdi}"
            val respons = restTemplate.getForObject(url, String::class.java)
            val underenhet = mapTilUnderenhet(respons!!)
            validerReturnertOrgnr(orgnrTilUnderenhet, underenhet.orgnr)
            underenhet
        } catch (hsee: HttpServerErrorException) {
            throw EnhetsregisteretIkkeTilgjengeligException("Enhetsregisteret svarer ikke", hsee)
        } catch (e: RestClientException) {
            throw EnhetsregisteretException("Feil ved kall til Enhetsregisteret", e)
        }
    }

    private fun validerReturnertOrgnr(opprinneligOrgnr: Orgnr, returnertOrgnr: Orgnr): Either<HentEnhetFeil.OrgnrEksistererIkke, Unit> {
        if (opprinneligOrgnr != returnertOrgnr) {
            log.error(
                "Orgnr hentet fra Enhetsregisteret samsvarer ikke med det medsendte orgnr. Request: ${opprinneligOrgnr.verdi}, response: ${returnertOrgnr.verdi}"
            )
            return HentEnhetFeil.OrgnrEksistererIkke.left()
        }
        return Unit.right()
    }

    private fun mapTilUnderenhet(jsonResponseFraEnhetsregisteret: String): Underenhet {
        return try {
            val enhetJson = objectMapper.readTree(jsonResponseFraEnhetsregisteret)
            val næringskodeJson = enhetJson["naeringskode1"]
                ?: throw IngenNæringException(
                    "Feil ved kall til Enhetsregisteret. Ingen næring for virksomhet."
                )
            Underenhet(
                antallAnsatte = enhetJson["antallAnsatte"].intValue(),
                næringskode = objectMapper.treeToValue(næringskodeJson, Næringskode5Siffer::class.java),
                navn = enhetJson["navn"].textValue(),
                overordnetEnhetOrgnr = Orgnr(enhetJson["overordnetEnhet"].textValue()),
                orgnr = Orgnr(enhetJson["organisasjonsnummer"].textValue()),
            )
        } catch (e: IOException) {
            throw EnhetsregisteretMappingException(
                "Feil ved kall til Enhetsregisteret. Kunne ikke parse respons.", e
            )
        } catch (e: NullPointerException) {
            throw EnhetsregisteretMappingException(
                "Feil ved kall til Enhetsregisteret. Kunne ikke parse respons.", e
            )
        }
    }

    open fun healthcheck(): HttpStatusCode {
        return try {
            val response = restTemplate.exchange(
                enhetsregisteretUrl, HttpMethod.GET, HttpEntity.EMPTY, String::class.java
            )
            response.statusCode
        } catch (e: RestClientResponseException) {
            HttpStatus.valueOf(e.statusCode.value())
        }
    }

    companion object {
        private val objectMapper = ObjectMapper()
    }
}