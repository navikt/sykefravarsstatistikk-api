package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.api

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret.EnhetsregisteretClient
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Unprotected
@RestController
class InternalController(
    private val enhetsregisteretClient: EnhetsregisteretClient,
) {
    @GetMapping("/internal/liveness")
    fun healthcheck(): HttpStatusCode {
        return HttpStatus.OK
    }

    @GetMapping("/internal/readiness")
    fun readyness(): HttpStatusCode {
        val enhetsregisteretIsReady = enhetsregisteretClient.clientIsReady().is2xxSuccessful

        return if (enhetsregisteretIsReady) HttpStatus.OK else HttpStatus.SERVICE_UNAVAILABLE
    }
}