package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.SykefraværFlereKvartalerForEksport
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config.KafkaTopicNavn
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config.KafkaTopicNavn.Companion.from
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.VirksomhetSykefravær
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import org.springframework.util.concurrent.ListenableFutureCallback

@Service
open class KafkaService internal constructor(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val kafkaUtsendingRapport: KafkaUtsendingRapport,
    private val kafkaUtsendingHistorikkRepository: KafkaUtsendingHistorikkRepository
) {
    private val log = LoggerFactory.getLogger(KafkaService::class.java)
    private val objectMapper = ObjectMapper()

    val antallMeldingerMottattForUtsending: Int
        get() = kafkaUtsendingRapport.antallMeldingerMottattForUtsending
    val snittTidUtsendingTilKafka: Long
        get() = kafkaUtsendingRapport.snittTidUtsendingTilKafka
    val snittTidOppdateringIDB: Long
        get() = kafkaUtsendingRapport.snittTidOppdateringIDB
    val råDataVedDetaljertMåling: String
        get() = kafkaUtsendingRapport.råDataVedDetaljertMåling

    open fun nullstillUtsendingRapport(
        totalMeldingerTilUtsending: Int, kafkaTopic: KafkaTopicNavn
    ) {
        log.info(
            "Gjør utsendingrapport klar før utsending på Kafka topic '{}'. '{}' meldinger vil bli sendt.",
            kafkaTopic.topic,
            totalMeldingerTilUtsending
        )
        kafkaUtsendingRapport.reset(totalMeldingerTilUtsending)
    }

    fun send(
        melding: Kafkamelding, kafkaTopic: KafkaTopicNavn
    ) {
        kafkaTemplate.send(kafkaTopic.topic, melding.nøkkel, melding.innhold)
            .addCallback(KafkamelindSendtCallback(kafkaUtsendingRapport, kafkaTopic, melding))
    }

    open fun sendTilStatistikkKategoriTopic(
        årstallOgKvartal: ÅrstallOgKvartal,
        statistikkategori: Statistikkategori,
        identifikator: String,
        sykefraværMedKategori: SykefraværMedKategori,
        sykefraværOverFlereKvartaler: SykefraværFlereKvartalerForEksport?
    ): Boolean {
        kafkaUtsendingRapport.leggTilMeldingMottattForUtsending()
        val topicNavn = from(statistikkategori).topic
        val key = KafkaStatistikkategoriTopicKey(
            statistikkategori,
            identifikator,
            årstallOgKvartal.kvartal,
            årstallOgKvartal.årstall
        )
        val value = KafkaStatistikkKategoriTopicValue(sykefraværMedKategori, sykefraværOverFlereKvartaler)
        val keyAsJsonString: String
        val dataAsJsonString: String
        try {
            keyAsJsonString = objectMapper.writeValueAsString(key)
            dataAsJsonString = objectMapper.writeValueAsString(value)
        } catch (e: JsonProcessingException) {
            kafkaUtsendingRapport.leggTilError(
                String.format(
                    "Kunne ikke parse statistikk '%s' til Json. Statistikk ikke sendt",
                    statistikkategori.name
                )
            )
            return false
        }
        val futureResult = kafkaTemplate.send(topicNavn, keyAsJsonString, dataAsJsonString)
        futureResult.addCallback(
            object : ListenableFutureCallback<SendResult<String?, String?>?> {
                override fun onFailure(throwable: Throwable) {
                    kafkaUtsendingRapport.leggTilError(
                        String.format(
                            "Utsending feilet for statistikk kategori '%s' og kode '%s',  med melding '%s'",
                            sykefraværMedKategori.kategori.name,
                            sykefraværMedKategori.kode,
                            throwable.message
                        )
                    )
                }

                override fun onSuccess(res: SendResult<String?, String?>?) {
                    kafkaUtsendingRapport.leggTilUtsendingSuksess()
                    log.debug(
                        "Melding sendt fra service til topic {}. Record.key: {}. Record.offset: {}",
                        topicNavn,
                        res?.producerRecord?.key(),
                        res?.recordMetadata?.offset()
                    )
                    if (Statistikkategori.VIRKSOMHET == statistikkategori) {
                        kafkaUtsendingHistorikkRepository.opprettHistorikk(
                            identifikator,  // for Statistikkategori.VIRKSOMHET er identifikator et orgnr
                            keyAsJsonString,
                            dataAsJsonString
                        )
                    }
                }
            })
        return true
    }

    open fun send(
        årstallOgKvartal: ÅrstallOgKvartal,
        virksomhetSykefravær: VirksomhetSykefravær,
        næring5SifferSykefravær: List<SykefraværMedKategori?>,
        næringSykefravær: SykefraværMedKategori,
        sektorSykefravær: SykefraværMedKategori,
        landSykefravær: SykefraværMedKategori
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
        val futureResult = kafkaTemplate.send(
            KafkaTopicNavn.SYKEFRAVARSSTATISTIKK_V1.topic, keyAsJsonString, dataAsJsonString
        )
        futureResult.addCallback(
            object : ListenableFutureCallback<SendResult<String?, String?>?> {
                override fun onFailure(throwable: Throwable) {
                    kafkaUtsendingRapport.leggTilError(
                        String.format(
                            "Utsending feilet for orgnr '%s' med melding '%s'",
                            virksomhetSykefravær.orgnr, throwable.message
                        ),
                        Orgnr(virksomhetSykefravær.orgnr)
                    )
                }

                override fun onSuccess(res: SendResult<String?, String?>?) {
                    kafkaUtsendingRapport.leggTilUtsendingSuksess(
                        Orgnr(virksomhetSykefravær.orgnr)
                    )
                    log.debug(
                        "Melding sendt fra service til topic {}. Record.key: {}. Record.offset: {}",
                        KafkaTopicNavn.SYKEFRAVARSSTATISTIKK_V1.topic,
                        res?.producerRecord?.key(),
                        res?.recordMetadata?.offset()
                    )
                    kafkaUtsendingHistorikkRepository.opprettHistorikk(
                        virksomhetSykefravær.orgnr, keyAsJsonString, dataAsJsonString
                    )
                }
            })
    }

    open fun addUtsendingTilKafkaProcessingTime(
        startUtsendingProcess: Long, stopUtsendingProcess: Long
    ) {
        kafkaUtsendingRapport.addUtsendingTilKafkaProcessingTime(
            startUtsendingProcess, stopUtsendingProcess
        )
    }

    open fun addDBOppdateringProcessingTime(
        startDBOppdateringProcess: Long, stopDBOppdateringProcess: Long
    ) {
        kafkaUtsendingRapport.addDBOppdateringProcessingTime(
            startDBOppdateringProcess, stopDBOppdateringProcess
        )
    }
}