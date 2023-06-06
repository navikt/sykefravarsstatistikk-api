package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.healthcheck

import common.SpringIntegrationTestbase
import io.prometheus.client.exporter.common.TextFormat.CONTENT_TYPE_004
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.PrometheusMetrics
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config.KafkaTopic
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

internal class PrometheusMetricsControllerTest : SpringIntegrationTestbase() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var prometheusMetrics: PrometheusMetrics

    @Test
    fun `metrics returnerer metrics`() {
        prometheusMetrics.incrementKafkaMessageSentCounter(KafkaTopic.SYKEFRAVARSSTATISTIKK_METADATA_V1)

        mockMvc.get("/internal/actuator/prometheus").andExpect {
            content {
                contentType(CONTENT_TYPE_004)
                string(
                    containsString(
                        "sykefravarsstatistikk_kafka_message_sent_counter_total{topic_name=\"${KafkaTopic.SYKEFRAVARSSTATISTIKK_METADATA_V1.name}\",} 1.0"
                    )
                )
            }
        }
    }
}