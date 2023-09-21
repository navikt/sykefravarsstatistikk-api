package no.nav.arbeidsgiver.sykefravarsstatistikk.api.config

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.ProducerListener
import org.springframework.lang.Nullable

@Configuration
internal open class KafkaConfig {

    private val log = LoggerFactory.getLogger(this::class.java)
    @Bean
    open fun kafkaTemplate(kafkaProperties: KafkaProperties): KafkaTemplate<String, String> {
        val kafkaTemplate = KafkaTemplate(DefaultKafkaProducerFactory<String, String>(kafkaProperties.asProperties()))
        kafkaTemplate.setProducerListener(producerListener)
        return kafkaTemplate
    }

    val producerListener: ProducerListener<String, String>
        get() = object : ProducerListener<String, String> {
            override fun onSuccess(
                producerRecord: ProducerRecord<String?, String?>, recordMetadata: RecordMetadata
            ) {
                log.debug(
                    "ProducerListener mottok success for record med offset '{}' på topic '{}'",
                    recordMetadata.topic(),
                    recordMetadata.offset()
                )
            }

            override fun onError(
                producerRecord: ProducerRecord<String, String>?,
                @Nullable recordMetadata: RecordMetadata?,
                exception: Exception?
            ) {
                val topicNavn =
                    if (recordMetadata != null) recordMetadata.topic() else "Ingen topic funnet (recordMetadat er null)"
                val offset = recordMetadata?.offset() ?: 0
                log.info(
                    "ProducerListener mottok en exception med melding '{}' for record med offset '{}' på topic '{}'",
                    exception?.message,
                    topicNavn,
                    offset
                )
            }
        }
}
