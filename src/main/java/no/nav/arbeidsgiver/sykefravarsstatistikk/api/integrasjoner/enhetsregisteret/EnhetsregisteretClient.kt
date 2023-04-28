package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.*
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
    open fun hentInformasjonOmEnhet(orgnrTilEnhet: Orgnr): OverordnetEnhet {
        val url = enhetsregisteretUrl + "enheter/" + orgnrTilEnhet.verdi
        return try {
            val respons = restTemplate.getForObject(url, String::class.java)
            val overordnetEnhet = mapTilEnhet(respons!!)
            validerReturnertOrgnr(orgnrTilEnhet, overordnetEnhet.orgnr)
            overordnetEnhet
        } catch (e: RestClientException) {
            throw EnhetsregisteretException("Feil ved kall til Enhetsregisteret", e)
        }
    }

    open fun hentInformasjonOmUnderenhet(orgnrTilUnderenhet: Orgnr): Underenhet {
        return try {
            val url = enhetsregisteretUrl + "underenheter/" + orgnrTilUnderenhet.verdi
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

    private fun mapTilEnhet(jsonResponseFraEnhetsregisteret: String): OverordnetEnhet {
        return try {
            val enhetJson = objectMapper.readTree(jsonResponseFraEnhetsregisteret)
                ?: throw NullPointerException("Json er null")
            val næringskodeJson = enhetJson["naeringskode1"]
                ?: throw NullPointerException("naeringskode1 er null")
            val sektorJson = enhetJson["institusjonellSektorkode"]
                ?: throw NullPointerException("institusjonellSektorkode er null")
            OverordnetEnhet(
                Orgnr(
                    enhetJson["organisasjonsnummer"].textValue()
                        ?: throw NullPointerException("organisasjonsnummer er null")
                ),
                enhetJson["navn"].textValue() ?: throw NullPointerException("navn er null"),
                objectMapper.treeToValue(næringskodeJson, Næringskode5Siffer::class.java) ?: throw NullPointerException(
                    "næringskode er null"
                ),
                objectMapper.treeToValue(sektorJson, InstitusjonellSektorkode::class.java)
                    ?: throw NullPointerException(
                        "institusjonellSektorkode er null"
                    ),
                enhetJson["antallAnsatte"].intValue()
            )
        } catch (e: IOException) {
            throw EnhetsregisteretMappingException(
                "Feil ved kall til Enhetsregisteret. Kunne ikke parse respons.", e
            )
        } catch (e: NullPointerException) {
            throw EnhetsregisteretMappingException(
                "Feil ved kall til Enhetsregisteret. Kunne ikke parse respons.", e
            )
        } catch (e: IllegalArgumentException) {
            throw EnhetsregisteretMappingException(
                "Feil ved kall til Enhetsregisteret. Kunne ikke parse respons.", e
            )
        }
    }

    private fun validerReturnertOrgnr(opprinneligOrgnr: Orgnr, returnertOrgnr: Orgnr) {
        if (opprinneligOrgnr != returnertOrgnr) {
            throw OrgnrEksistererIkkeException(
                "Orgnr hentet fra Enhetsregisteret samsvarer ikke med det medsendte orgnr. Request: "
                        + opprinneligOrgnr.verdi
                        + ", response: "
                        + returnertOrgnr.verdi
            )
        }
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