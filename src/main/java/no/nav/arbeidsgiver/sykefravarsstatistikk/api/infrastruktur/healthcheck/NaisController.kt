package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.healthcheck

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
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
        @RequestParam(required = false) name: Set<String>?
    ): ResponseEntity<String> {
        val names = name ?: emptySet()
        val metrics = CharArrayWriter(1024)
            .also { writer ->
                TextFormat.write004(
                    writer,
                    CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(names)
                )
            }.use { it.toString() }

        return ResponseEntity.ok()
            .contentType(MediaType.valueOf(TextFormat.CONTENT_TYPE_004))
            .body(metrics)
    }
}