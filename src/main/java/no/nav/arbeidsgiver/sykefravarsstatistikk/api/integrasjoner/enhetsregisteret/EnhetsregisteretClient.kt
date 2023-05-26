package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.json.OverordnetEnhetResponseJson
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.json.UnderenhetResponseJson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate

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
            if (!validerReturnertOrgnr(orgnrTilEnhet, overordnetEnhet.orgnr)) {
                return HentEnhetFeil.OrgnrMatcherIkke.left()
            }
            overordnetEnhet.right()
        } catch (e: RestClientException) {
            log.error("Feil ved kall til Enhetsregisteret ved henting av enhet ${orgnrTilEnhet.verdi}", e)
            HentEnhetFeil.FeilVedKallTilEnhetsregisteret.left()
        }
    }

    open fun hentUnderenhet(orgnrTilUnderenhet: Orgnr): Either<HentUnderenhetFeil, Underenhet> {
        return try {
            val url = "${enhetsregisteretUrl}/underenheter/${orgnrTilUnderenhet.verdi}"
            val underenhet = restTemplate.getForObject(url, UnderenhetResponseJson::class.java)!!
                .toDomain()
            if (!validerReturnertOrgnr(orgnrTilUnderenhet, underenhet.orgnr)) {
                return HentUnderenhetFeil.OrgnrMatcherIkke.left()
            }
            underenhet.right()
        } catch (e: HttpServerErrorException) {
            log.error("Enhetsregisteret svarer ikke", e)
            return HentUnderenhetFeil.EnhetsregisteretSvarerIkke.left()
        } catch (e: RestClientException) {
            log.error("Feil ved kall til Enhetsregisteret ved henting av underenhet ${orgnrTilUnderenhet.verdi}", e)
            return HentUnderenhetFeil.FeilVedKallTilEnhetsregisteret.left()
        }
    }


    private fun validerReturnertOrgnr(
        opprinneligOrgnr: Orgnr,
        returnertOrgnr: Orgnr
    ): Boolean = if (opprinneligOrgnr != returnertOrgnr) {
        log.error(
            "Orgnr hentet fra Enhetsregisteret samsvarer ikke med det medsendte orgnr. Request: ${opprinneligOrgnr.verdi}, response: ${returnertOrgnr.verdi}"
        )
        false
    } else true

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

    sealed class HentEnhetFeil {
        object FeilVedKallTilEnhetsregisteret : HentEnhetFeil()
        object OrgnrMatcherIkke : HentEnhetFeil()
    }

    sealed class HentUnderenhetFeil {
        object EnhetsregisteretSvarerIkke : HentUnderenhetFeil()
        object FeilVedKallTilEnhetsregisteret : HentUnderenhetFeil()
        object OrgnrMatcherIkke : HentUnderenhetFeil()
    }
}