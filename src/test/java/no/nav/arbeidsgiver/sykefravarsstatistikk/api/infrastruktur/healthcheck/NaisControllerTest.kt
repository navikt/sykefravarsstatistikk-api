package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.healthcheck

import common.SpringIntegrationTestbase
import io.prometheus.client.exporter.common.TextFormat.CONTENT_TYPE_004
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.Metrics
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

internal class NaisControllerTest : SpringIntegrationTestbase() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `metrics returnerer metrics`() {
        Metrics.kafkaMessageCounter.labels("dummy-topic").inc()

        mockMvc.get("/internal/metrics").andExpect {
            content {
                contentType(CONTENT_TYPE_004)
                string(
                    containsString(
                        "sykefravarsstatistikk_kafka_message_counter_total{topic_name=\"dummy-topic\",} 1.0"
                    )
                )
            }
        }
    }
}