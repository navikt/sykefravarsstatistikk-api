package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur

import io.prometheus.metrics.core.metrics.Counter
import io.prometheus.metrics.model.registry.PrometheusRegistry
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.KafkaTopic
import org.springframework.stereotype.Component

@Component
class PrometheusMetrics(
    meterRegistry: PrometheusRegistry,
) {
    private val kafkaMessageSentCounter: Counter = Counter.builder()
        .name("sykefravarsstatistikk_kafka_message_sent_counter")
        .labelNames("topic_name")
        .help("Hvor mange Kafka-meldinger som har blitt sendt ut fra sykefravarsstatistikk-api")
        .withoutExemplars()
        .register(meterRegistry)

    private val kafkaMessageErrorCounter: Counter = Counter.builder()
        .name("sykefravarsstatistikk_kafka_message_error_counter")
        .labelNames("topic_name")
        .help("Antall feilede forøk på å sende Kafka-meldinger fra sykefravarsstatistikk-api")
        .withoutExemplars()
        .register(meterRegistry)

    fun incrementKafkaMessageSentCounter(kafkaTopic: KafkaTopic) {
        kafkaMessageSentCounter.labelValues(kafkaTopic.name).inc()
    }

    fun incrementKafkaMessageErrorCounter(kafkaTopic: KafkaTopic) {
        kafkaMessageErrorCounter.labelValues(kafkaTopic.navn).inc()
    }
}
