package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.SykefraværMedKategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.VirksomhetSykefravær
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.KafkaTopic
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.PrometheusMetrics
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.dto.KafkaTopicKey
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.dto.KafkaTopicValue
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.dto.Kafkamelding
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class KafkaClient internal constructor(
    private val kafkaTemplate: KafkaTemplate<String?, String?>,
    private val legacyKafkaUtsendingRapport: LegacyKafkaUtsendingRapport,
    private val legacyKafkaUtsendingHistorikkRepository: LegacyKafkaUtsendingHistorikkRepository,
    private val prometheusMetrics: PrometheusMetrics,
) {
    private val log = LoggerFactory.getLogger(KafkaClient::class.java)

    private val objectMapper = ObjectMapper()

    fun nullstillUtsendingRapport(
        totalMeldingerTilUtsending: Int, KafkaTopicNavn: KafkaTopic
    ) {
        log.info(
            "Gjør utsendingrapport klar før utsending på Kafka topic '{}'. '{}' meldinger vil bli sendt.",
            KafkaTopicNavn.navn,
            totalMeldingerTilUtsending
        )
        legacyKafkaUtsendingRapport.reset(totalMeldingerTilUtsending)
    }

    val antallMeldingerMottattForUtsending: Int
        get() = legacyKafkaUtsendingRapport.antallMeldingerMottattForUtsending.toInt()

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

    @Deprecated("Bruk eksport per kategori. Slett denne etter at alle konsumenter har gått over.")
    fun legacySend(
        årstallOgKvartal: ÅrstallOgKvartal,
        virksomhetSykefravær: VirksomhetSykefravær,
        næring5SifferSykefravær: List<SykefraværMedKategori>,
        næringSykefravær: SykefraværMedKategori,
        sektorSykefravær: SykefraværMedKategori,
        landSykefravær: SykefraværMedKategori
    ) {
        // TODO bytt til Prometheus
        legacyKafkaUtsendingRapport.leggTilMeldingMottattForUtsending()
        if (legacyKafkaUtsendingRapport.antallMeldingerIError.toInt() > 5) {
            throw KafkaUtsendingException(
                String.format(
                    "Antall error:'%d'. Avbryter eksportering. Totalt meldinger som var klar for sending er: '%d'."
                            + " Antall meldinger som har egentlig blitt sendt: '%d'",
                    legacyKafkaUtsendingRapport.antallMeldingerIError,
                    legacyKafkaUtsendingRapport.antallMeldingerSent,
                    legacyKafkaUtsendingRapport.antallMeldingerMottattForUtsending
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
            legacyKafkaUtsendingRapport.leggTilError(
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
                legacyKafkaUtsendingRapport.leggTilUtsendingSuksess(
                    Orgnr(virksomhetSykefravær.orgnr)
                )
                log.debug(
                    "Melding sendt fra service til topic {}. Record.key: {}. Record.offset: {}",
                    KafkaTopic.SYKEFRAVARSSTATISTIKK_V1.navn,
                    res.producerRecord.key(),
                    res.recordMetadata.offset()
                )
                legacyKafkaUtsendingHistorikkRepository.opprettHistorikk(
                    virksomhetSykefravær.orgnr, keyAsJsonString, dataAsJsonString
                )
            }
            .exceptionally { throwable: Throwable ->
                legacyKafkaUtsendingRapport.leggTilError(
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
        get() = legacyKafkaUtsendingRapport.snittTidUtsendingTilKafka
    val snittTidOppdateringIDB: Long
        get() = legacyKafkaUtsendingRapport.snittTidOppdateringIDB
    val råDataVedDetaljertMåling: String
        get() = legacyKafkaUtsendingRapport.råDataVedDetaljertMåling

    fun addUtsendingTilKafkaProcessingTime(
        startUtsendingProcess: Long, stopUtsendingProcess: Long
    ) {
        legacyKafkaUtsendingRapport.addUtsendingTilKafkaProcessingTime(
            startUtsendingProcess, stopUtsendingProcess
        )
    }

    fun addDBOppdateringProcessingTime(
        startDBOppdateringProcess: Long, stopDBOppdateringProcess: Long
    ) {
        legacyKafkaUtsendingRapport.addDBOppdateringProcessingTime(
            startDBOppdateringProcess, stopDBOppdateringProcess
        )
    }
}