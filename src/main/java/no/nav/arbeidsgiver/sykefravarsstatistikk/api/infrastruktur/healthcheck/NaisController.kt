package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.healthcheck

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.CharArrayWriter

@Unprotected
@RestController
class NaisController(private val enhetsregisteretClient: EnhetsregisteretClient) {
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

    @GetMapping("/internal/metrics")
    fun metrics(
        @PathVariable name: List<String>
    ): ResponseEntity<String> {
        val formatted = CharArrayWriter(1024)
            .also { TextFormat.write004(it, CollectorRegistry.defaultRegistry.metricFamilySamples()) }
            .use { it.toString() }

        return ResponseEntity.ok().contentType(TextFormat.CONTENT_TYPE_004).body(formatted);
    }
}