package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.KafkaTopic
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.PrometheusMetrics
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.dto.Kafkamelding
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaClient internal constructor(
    private val kafkaTemplate: KafkaTemplate<String?, String?>,
    private val prometheusMetrics: PrometheusMetrics,
) {
    private val log = LoggerFactory.getLogger(KafkaClient::class.java)

    fun sendMelding(melding: Kafkamelding, topic: KafkaTopic) {
        kafkaTemplate.send(topic.navn, melding.nøkkel, melding.innhold)
            .thenAcceptAsync {
                prometheusMetrics.incrementKafkaMessageSentCounter(topic)
            }.exceptionally {
                prometheusMetrics.incrementKafkaMessageErrorCounter(topic)
                log.warn("Melding '${melding.nøkkel}' ble ikke sendt på '${topic.navn}'", it)
                null
            }
    }
}