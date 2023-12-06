package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.api

import config.FunksjonellSykefraværsstatistikkTestApp
import io.prometheus.client.exporter.common.TextFormat.CONTENT_TYPE_004
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.KafkaTopic
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.PrometheusMetrics
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest(
    classes = [FunksjonellSykefraværsstatistikkTestApp::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ContextConfiguration(initializers = [FunksjonellSykefraværsstatistikkTestApp::class])
@AutoConfigureMockMvc
@AutoConfigureObservability
@TestPropertySource(properties = ["spring.h2.console.enabled=false", "management.endpoints.web.exposure.include=prometheus", "management.endpoints.web.base-path=/internal/actuator"])
internal class PrometheusMetricsControllerTest {
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
                contentType(CONTENT_TYPE_004)
                string(
                    containsString(
                        "sykefravarsstatistikk_kafka_message_sent_counter_total{topic_name=\"${dummyTopic.name}\",} 1.0"
                    )
                )
            }
        }
    }
}