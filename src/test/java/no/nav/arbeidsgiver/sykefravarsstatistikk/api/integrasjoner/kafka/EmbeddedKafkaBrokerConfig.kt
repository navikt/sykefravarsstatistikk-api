package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config.KafkaTopicNavn
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.kafka.test.EmbeddedKafkaBroker

@TestConfiguration
open class EmbeddedKafkaBrokerConfig {
    private val embeddedKafkaBroker: EmbeddedKafkaBroker =
        EmbeddedKafkaBroker(1, true, *KafkaTopicNavn.values().map { it.topic }.toTypedArray())

    init {
        embeddedKafkaBroker.brokerProperties(mapOf("listeners" to "PLAINTEXT://localhost:9092", "port" to "9092"))
    }

    @Bean("embeddedKafka", destroyMethod = "destroy")
    @Profile("kafka-test")
    open fun getEmbeddedKafkaBroker() = embeddedKafkaBroker
}