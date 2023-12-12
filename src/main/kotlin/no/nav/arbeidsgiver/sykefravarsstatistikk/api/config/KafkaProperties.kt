package no.nav.arbeidsgiver.sykefravarsstatistikk.api.config

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.LegacyKafkaUtsendingRapport
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "kafka.outbound")
@Component
data class KafkaProperties(
    var bootstrapServers: String? = null,
    var securityProtocol: String? = null,
    var caPath: String? = null,
    var keystorePath: String? = null,
    var truststorePath: String? = null,
    var credstorePassword: String? = null
) {

    fun asProperties(): Map<String, Any> {
        val props = HashMap<String, Any>()
        if (bootstrapServers != null) {
            props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers!!
        }
        if (credstorePassword != null) {
            props[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = credstorePassword!!
            props[SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG] = credstorePassword!!
            props[SslConfigs.SSL_KEY_PASSWORD_CONFIG] = credstorePassword!!
            props[SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG] = "JKS"
            props[SslConfigs.SSL_KEYSTORE_TYPE_CONFIG] = "PKCS12"
        }
        if (truststorePath != null) {
            props[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = truststorePath!!
        }
        if (keystorePath != null) {
            props[SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG] = keystorePath!!
        }
        if (securityProtocol != null) {
            props[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = securityProtocol!!
        }
        props[ProducerConfig.CLIENT_ID_CONFIG] = "sykefravarsstatistikk-api"
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.getName()
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.getName()
        props[ProducerConfig.RETRIES_CONFIG] = 10
        props[ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG] = 120000 // 2 min (default)
        props[ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG] = 10000
        props[ProducerConfig.LINGER_MS_CONFIG] = 100
        props[ProducerConfig.BATCH_SIZE_CONFIG] = (16384
                * 10 // størrelse av en melding er mellom 1000 bytes og 20K bytes (virksomhet med 70+ 5siffer næringskoder)
                )
        props[ProducerConfig.ACKS_CONFIG] = "1"
        props[ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION] = 5 // default
        return props
    }

    @get:Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    @get:Bean
    val kafkaUtsendingReport: LegacyKafkaUtsendingRapport
        get() = LegacyKafkaUtsendingRapport()
}
