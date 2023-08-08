package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.PrometheusMetrics
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.KafkaTopic
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.VirksomhetSykefravær
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class KafkaService internal constructor(
    private val kafkaTemplate: KafkaTemplate<String?, String?>,
    private val kafkaUtsendingRapport: KafkaUtsendingRapport,
    private val kafkaUtsendingHistorikkRepository: KafkaUtsendingHistorikkRepository,
    private val prometheusMetrics: PrometheusMetrics,
) {
    private val log = LoggerFactory.getLogger(KafkaService::class.java)

    private val objectMapper = ObjectMapper()

    fun nullstillUtsendingRapport(
        totalMeldingerTilUtsending: Int, KafkaTopicNavn: KafkaTopic
    ) {
        log.info(
            "Gjør utsendingrapport klar før utsending på Kafka topic '{}'. '{}' meldinger vil bli sendt.",
            KafkaTopicNavn.navn,
            totalMeldingerTilUtsending
        )
        kafkaUtsendingRapport.reset(totalMeldingerTilUtsending)
    }

    val antallMeldingerMottattForUtsending: Int
        get() = kafkaUtsendingRapport.antallMeldingerMottattForUtsending

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


    fun send(
        årstallOgKvartal: ÅrstallOgKvartal,
        virksomhetSykefravær: VirksomhetSykefravær,
        næring5SifferSykefravær: List<SykefraværMedKategori?>?,
        næringSykefravær: SykefraværMedKategori?,
        sektorSykefravær: SykefraværMedKategori?,
        landSykefravær: SykefraværMedKategori?
    ) {
        // TODO bytt til Prometheus
        kafkaUtsendingRapport.leggTilMeldingMottattForUtsending()
        if (kafkaUtsendingRapport.antallMeldingerIError > 5) {
            throw KafkaUtsendingException(
                String.format(
                    "Antall error:'%d'. Avbryter eksportering. Totalt meldinger som var klar for sending er: '%d'."
                            + " Antall meldinger som har egentlig blitt sendt: '%d'",
                    kafkaUtsendingRapport.antallMeldingerIError,
                    kafkaUtsendingRapport.antallMeldingerSent,
                    kafkaUtsendingRapport.antallMeldingerMottattForUtsending
                )
            )
        }
        val key = KafkaTopicKey(
            virksomhetSykefravær.orgnr,
            årstallOgKvartal.kvartal,
            årstallOgKvartal.årstall
        )
        val value = KafkaTopicValue(
            virksomhetSykefravær,
            næring5SifferSykefravær,
            næringSykefravær,
            sektorSykefravær,
            landSykefravær
        )
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        val keyAsJsonString: String
        val dataAsJsonString: String
        try {
            keyAsJsonString = objectMapper.writeValueAsString(key)
            dataAsJsonString = objectMapper.writeValueAsString(value)
        } catch (e: JsonProcessingException) {
            kafkaUtsendingRapport.leggTilError(
                String.format(
                    "Kunne ikke parse orgnr '%s' til Json. Statistikk ikke sent for virksomheten.",
                    virksomhetSykefravær.orgnr
                ),
                Orgnr(virksomhetSykefravær.orgnr)
            )
            return
        }
        val futureResult: CompletableFuture<SendResult<String?, String?>> = kafkaTemplate.send(
            KafkaTopic.SYKEFRAVARSSTATISTIKK_V1.navn, keyAsJsonString, dataAsJsonString
        )
        futureResult
            .thenAcceptAsync { res: SendResult<String?, String?> ->
                kafkaUtsendingRapport.leggTilUtsendingSuksess(
                    Orgnr(virksomhetSykefravær.orgnr)
                )
                log.debug(
                    "Melding sendt fra service til topic {}. Record.key: {}. Record.offset: {}",
                    KafkaTopic.SYKEFRAVARSSTATISTIKK_V1.navn,
                    res.producerRecord.key(),
                    res.recordMetadata.offset()
                )
                kafkaUtsendingHistorikkRepository.opprettHistorikk(
                    virksomhetSykefravær.orgnr, keyAsJsonString, dataAsJsonString
                )
            }
            .exceptionally { throwable: Throwable ->
                kafkaUtsendingRapport.leggTilError(
                    String.format(
                        "Utsending feilet for orgnr '%s' med melding '%s'",
                        virksomhetSykefravær.orgnr, throwable.message
                    ),
                    Orgnr(virksomhetSykefravær.orgnr)
                )
                null
            }
    }

    val snittTidUtsendingTilKafka: Long
        get() = kafkaUtsendingRapport.snittTidUtsendingTilKafka
    val snittTidOppdateringIDB: Long
        get() = kafkaUtsendingRapport.snittTidOppdateringIDB
    val råDataVedDetaljertMåling: String
        get() = kafkaUtsendingRapport.råDataVedDetaljertMåling

    fun addUtsendingTilKafkaProcessingTime(
        startUtsendingProcess: Long, stopUtsendingProcess: Long
    ) {
        kafkaUtsendingRapport.addUtsendingTilKafkaProcessingTime(
            startUtsendingProcess, stopUtsendingProcess
        )
    }

    fun addDBOppdateringProcessingTime(
        startDBOppdateringProcess: Long, stopDBOppdateringProcess: Long
    ) {
        kafkaUtsendingRapport.addDBOppdateringProcessingTime(
            startDBOppdateringProcess, stopDBOppdateringProcess
        )
    }
}