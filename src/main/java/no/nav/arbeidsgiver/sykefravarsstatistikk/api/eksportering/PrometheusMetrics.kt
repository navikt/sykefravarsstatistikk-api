package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Counter
import org.springframework.stereotype.Component

@Component
class PrometheusMetrics(
    meterRegistry: CollectorRegistry,
) {
    private val kafkaMessageSentCounter: Counter
    private val kafkaMessageErrorCounter: Counter

    init {
        kafkaMessageSentCounter = Counter.build()
            .name("sykefravarsstatistikk_kafka_message_sent_counter")
            .labelNames("topic_name")
            .help("Hvor mange Kafka-meldinger som har blitt sendt ut fra sykefravarsstatistikk-api")
            .register(meterRegistry)

        kafkaMessageErrorCounter = Counter.build()
            .name("sykefravarsstatistikk_kafka_message_error_counter")
            .labelNames("topic_name")
            .help("Antall feilede forøk på å sende Kafka-meldinger fra sykefravarsstatistikk-api")
            .register(meterRegistry)
    }

    fun incrementKafkaMessageSentCounter(vararg labels: String) {
        kafkaMessageSentCounter.labels(*labels).inc()
    }

    fun incrementKafkaMessageErrorCounter(vararg labels: String) {
        kafkaMessageErrorCounter.labels(*labels).inc()
    }
}
