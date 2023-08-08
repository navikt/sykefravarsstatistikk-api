package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.ProxyWebClient
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.json.OverordnetEnhetResponseJson
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.json.UnderenhetResponseJson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.codec.DecodingException
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.util.retry.Retry

@Component
open class EnhetsregisteretClient(
    private val webClient: ProxyWebClient,
    @param:Value("\${enhetsregisteret.url}") private val enhetsregisteretUrl: String
) {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
    private val retrySpec = Retry.max(2).filter {
        it !is DecodingException
    }.onRetryExhaustedThrow { _, signal -> signal.failure() }

    open fun hentEnhet(orgnrTilEnhet: Orgnr): Either<HentEnhetFeil, OverordnetEnhet> {
        val url = "${enhetsregisteretUrl}/enheter/{orgnr}"
        return try {

            val overordnetEnhet = webClient.get()
                .uri(url, orgnrTilEnhet.verdi)
                .retrieve()
                .bodyToMono(OverordnetEnhetResponseJson::class.java)
                .retryWhen(retrySpec)
                .block()!!
                .toDomain()
            if (!validerReturnertOrgnr(orgnrTilEnhet, overordnetEnhet.orgnr)) {
                return HentEnhetFeil.OrgnrMatcherIkke.left()
            }
            overordnetEnhet.right()
        } catch (e: WebClientResponseException) {
            log.error("Feil ved kall til Enhetsregisteret ved henting av enhet ${orgnrTilEnhet.verdi}", e)
            HentEnhetFeil.FeilVedKallTilEnhetsregisteret.left()
        } catch (e: DecodingException) {
            log.error("Feil ved dekoding av JSON ved henting av enhet ${orgnrTilEnhet.verdi}", e)
            HentEnhetFeil.FeilVedDekodingAvJson.left()
        }
    }


    open fun hentUnderenhet(orgnrTilUnderenhet: Orgnr): Either<HentUnderenhetFeil, Underenhet> {
        return try {
            val url = "${enhetsregisteretUrl}/underenheter/${orgnrTilUnderenhet.verdi}"
            val underenhet = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(UnderenhetResponseJson::class.java)
                .retryWhen(retrySpec)
                .block()!!
                .toDomain()
            if (!validerReturnertOrgnr(orgnrTilUnderenhet, underenhet.orgnr)) {
                return HentUnderenhetFeil.OrgnrMatcherIkke.left()
            }
            underenhet.right()
        } catch (e: WebClientResponseException) {
            if (e.statusCode.is5xxServerError) {
                log.error("Enhetsregisteret svarer ikke", e)
                return HentUnderenhetFeil.EnhetsregisteretSvarerIkke.left()
            } else {
                log.error("Feil ved kall til Enhetsregisteret ved henting av underenhet ${orgnrTilUnderenhet.verdi}", e)
                return HentUnderenhetFeil.FeilVedKallTilEnhetsregisteret.left()
            }
        } catch (e: DecodingException) {
            log.error("Feil ved dekoding av JSON ved henting av underenhet ${orgnrTilUnderenhet.verdi}", e)
            return HentUnderenhetFeil.FeilVedDekodingAvJson.left()
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
            webClient.get()
                .uri(enhetsregisteretUrl)
                .retrieve()
                .toBodilessEntity()
                .block()!!
                .statusCode
        } catch (e: WebClientResponseException) {
            e.statusCode
        }
    }

    sealed class HentEnhetFeil {
        object FeilVedKallTilEnhetsregisteret : HentEnhetFeil()
        object FeilVedDekodingAvJson : HentEnhetFeil()
        object OrgnrMatcherIkke : HentEnhetFeil()
    }

    sealed class HentUnderenhetFeil {
        object EnhetsregisteretSvarerIkke : HentUnderenhetFeil()
        object FeilVedDekodingAvJson : HentUnderenhetFeil()
        object FeilVedKallTilEnhetsregisteret : HentUnderenhetFeil()
        object OrgnrMatcherIkke : HentUnderenhetFeil()
    }
}