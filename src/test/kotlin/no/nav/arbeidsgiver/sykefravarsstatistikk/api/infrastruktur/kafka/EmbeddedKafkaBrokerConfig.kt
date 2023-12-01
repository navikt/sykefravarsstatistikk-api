package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.KafkaTopic
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.EmbeddedKafkaZKBroker

@TestConfiguration
open class EmbeddedKafkaBrokerConfig {
    private val embeddedKafkaBroker: EmbeddedKafkaBroker =
        EmbeddedKafkaZKBroker(1, true, *KafkaTopic.entries.map { it.navn }.toTypedArray())

    init {
        embeddedKafkaBroker.brokerProperties(mapOf("listeners" to "PLAINTEXT://127.0.0.1:9092", "port" to "9092"))
    }

    @Bean("embeddedKafka", destroyMethod = "destroy")
    @Profile("kafka-test")
    open fun getEmbeddedKafkaBroker() = embeddedKafkaBroker
}