package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.api

import config.SpringIntegrationTestbase
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.PrometheusMetrics
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.KafkaTopic
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
        val dummyTopic = KafkaTopic.SYKEFRAVARSSTATISTIKK_METADATA_V1
        prometheusMetrics.incrementKafkaMessageSentCounter(dummyTopic)

        mockMvc.get("/internal/actuator/prometheus").andExpect {
            content {
                string(
                    containsString(
                        "sykefravarsstatistikk_kafka_message_sent_counter_total{topic_name=\"${dummyTopic.name}\"} 1.0"
                    )
                )
            }
        }
    }
}