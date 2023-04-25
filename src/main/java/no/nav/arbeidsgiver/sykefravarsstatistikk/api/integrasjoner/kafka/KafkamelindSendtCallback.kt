package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config.KafkaTopicNavn
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto.Kafkamelding
import org.springframework.kafka.support.SendResult
import org.springframework.util.concurrent.ListenableFutureCallback

class KafkamelindSendtCallback(
    private val kafkaUtsendingRapport: KafkaUtsendingRapport,
    private val kafkaTopic: KafkaTopicNavn,
    private val message: Kafkamelding
) : ListenableFutureCallback<SendResult<String, String>> {
    override fun onSuccess(result: SendResult<String, String>?) {
        kafkaUtsendingRapport.leggTilUtsendingSuksess()
    }

    override fun onFailure(ex: Throwable) {
        kafkaUtsendingRapport.leggTilError(
            "Feil ved utsending til Kafka topic '${kafkaTopic.topic}'. Melding: ${message.nøkkel} - ${message.innhold}. Feilmelding: ${ex.message}"
        )
    }
}