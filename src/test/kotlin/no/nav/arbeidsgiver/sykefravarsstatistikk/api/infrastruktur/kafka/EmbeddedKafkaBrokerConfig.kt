package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.KafkaTopic
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.kafka.test.EmbeddedKafkaBroker

@TestConfiguration
open class EmbeddedKafkaBrokerConfig {
    private val embeddedKafkaBroker: EmbeddedKafkaBroker =
        EmbeddedKafkaBroker(1, true, *KafkaTopic.values().map { it.navn }.toTypedArray())

    init {
        embeddedKafkaBroker.brokerProperties(mapOf("listeners" to "PLAINTEXT://127.0.0.1:9092", "port" to "9092"))
    }

    @Bean("embeddedKafka", destroyMethod = "destroy")
    @Profile("kafka-test")
    open fun getEmbeddedKafkaBroker() = embeddedKafkaBroker
}