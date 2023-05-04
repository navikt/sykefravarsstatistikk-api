package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.healthcheck

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.io.CharArrayWriter

@Unprotected
@RestController
class MetricsController {
    @GetMapping("/internal/metrics")
    fun metrics(): ResponseEntity<String> {
        val metrics = CharArrayWriter(1024)
            .also { writer ->
                TextFormat.write004(
                    writer,
                    CollectorRegistry.defaultRegistry.metricFamilySamples()
                )
            }.use { it.toString() }

        return ResponseEntity.ok()
            .contentType(MediaType.valueOf(TextFormat.CONTENT_TYPE_004))
            .body(metrics)
    }
}