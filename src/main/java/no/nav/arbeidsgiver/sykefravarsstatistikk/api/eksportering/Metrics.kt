package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering

import io.prometheus.client.Counter

object Metrics {
    val kafkaMessageSentCounter: Counter = Counter.build()
        .name("sykefravarsstatistikk_kafka_message_sent_counter")
        .labelNames("topic_name")
        .help("Hvor mange Kafka-meldinger som har blitt sendt ut fra sykefravarsstatistikk-api")
        .register()

    var kafkaMessageErrorCounter: Counter = Counter.build()
        .name("sykefravarsstatistikk_kafka_message_error_counter")
        .labelNames("topic_name")
        .help("Antall feilede forøk på å sende Kafka-meldinger fra sykefravarsstatistikk-api")
        .register()
}
