package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.healthcheck

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.HttpStatusCode
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Unprotected
@RestController
class HealthcheckController(private val enhetsregisteretClient: EnhetsregisteretClient) {
    @GetMapping("/internal/healthcheck")
    fun healthcheck(): String {
        return "ok"
    }

    @GetMapping("/internal/healthcheck/avhengigheter")
    fun sjekkAvhengigheter(): Map<String, HttpStatusCode> {
        val statuser: MutableMap<String, HttpStatusCode> = HashMap()
        statuser["enhetsregisteret"] = enhetsregisteretClient.healthcheck()
        return statuser
    }
}