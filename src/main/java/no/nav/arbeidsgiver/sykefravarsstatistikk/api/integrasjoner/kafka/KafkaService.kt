package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.SykefraværFlereKvartalerForEksport;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config.KafkaTopicName;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto.KafkaStatistikkKategoriTopicValue;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto.KafkaStatistikkategoriTopicKey;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto.KafkaTopicKey;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto.KafkaTopicValue;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.VirksomhetSykefravær;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

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
        totalMeldingerTilUtsending: Int, kafkaTopicName: KafkaTopicName
    ) {
        log.info(
            "Gjør utsendingrapport klar før utsending på Kafka topic '{}'. '{}' meldinger vil bli sendt.",
            kafkaTopicName.topic,
            totalMeldingerTilUtsending
        )
        kafkaUtsendingRapport.reset(totalMeldingerTilUtsending)
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
            årstallOgKvartal.getKvartal(),
            årstallOgKvartal.getÅrstall());

    KafkaStatistikkKategoriTopicValue value =
        new KafkaStatistikkKategoriTopicValue(sykefraværMedKategori, sykefraværOverFlereKvartaler);

    String keyAsJsonString;
    String dataAsJsonString;

    try {
      keyAsJsonString = objectMapper.writeValueAsString(key);
      dataAsJsonString = objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      kafkaUtsendingRapport.leggTilError(
          String.format(
              "Kunne ikke parse statistikk '%s' til Json. Statistikk ikke sendt",
              statistikkategori.name()));
      return false;
    }

    String topicNavn = KafkaTopicName.Companion.from(statistikkategori).getTopic();

    CompletableFuture<SendResult<String, String>> futureResult =
        kafkaTemplate.send(topicNavn, keyAsJsonString, dataAsJsonString);

    futureResult
        .thenAcceptAsync(
            res -> {
              kafkaUtsendingRapport.leggTilUtsendingSuksess();
              log.debug(
                  "Melding sendt fra service til topic {}. Record.key: {}. Record.offset: {}",
                  topicNavn,
                  res.getProducerRecord().key(),
                  res.getRecordMetadata().offset());

              if (Statistikkategori.VIRKSOMHET == statistikkategori) {
                kafkaUtsendingHistorikkRepository.opprettHistorikk(
                    identifikator, // for Statistikkategori.VIRKSOMHET er identifikator et orgnr
                    keyAsJsonString,
                    dataAsJsonString);
              }
            })
        .exceptionally(
            throwable -> {
              kafkaUtsendingRapport.leggTilError(
                  String.format(
                      "Utsending feilet for statistikk kategori '%s' og kode '%s', med melding '%s'",
                      sykefraværMedKategori.getKategori().name(),
                      sykefraværMedKategori.getKode(),
                      throwable.getMessage()));
              return null;
            });

    return true;
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
            landSykefravær);

    objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    String keyAsJsonString;
    String dataAsJsonString;
    try {
      keyAsJsonString = objectMapper.writeValueAsString(key);
      dataAsJsonString = objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      kafkaUtsendingRapport.leggTilError(
          String.format(
              "Kunne ikke parse orgnr '%s' til Json. Statistikk ikke sent for virksomheten.",
              virksomhetSykefravær.getOrgnr()),
          new Orgnr(virksomhetSykefravær.getOrgnr()));
      return;
    }

    CompletableFuture<SendResult<String, String>> futureResult =
        kafkaTemplate.send(
            KafkaTopicName.SYKEFRAVARSSTATISTIKK_V1.getTopic(), keyAsJsonString, dataAsJsonString);

    futureResult
        .thenAcceptAsync(
            res -> {
              kafkaUtsendingRapport.leggTilUtsendingSuksess(
                  new Orgnr(virksomhetSykefravær.getOrgnr()));
              log.debug(
                  "Melding sendt fra service til topic {}. Record.key: {}. Record.offset: {}",
                  KafkaTopicName.SYKEFRAVARSSTATISTIKK_V1.getTopic(),
                  res.getProducerRecord().key(),
                  res.getRecordMetadata().offset());
              kafkaUtsendingHistorikkRepository.opprettHistorikk(
                  virksomhetSykefravær.getOrgnr(), keyAsJsonString, dataAsJsonString);
            })
        .exceptionally(
            throwable -> {
              kafkaUtsendingRapport.leggTilError(
                  String.format(
                      "Utsending feilet for orgnr '%s' med melding '%s'",
                      virksomhetSykefravær.getOrgnr(), throwable.getMessage()),
                  new Orgnr(virksomhetSykefravær.getOrgnr()));
              return null;
            });
  }

  public long getSnittTidUtsendingTilKafka() {
    return kafkaUtsendingRapport.getSnittTidUtsendingTilKafka();
  }

  public long getSnittTidOppdateringIDB() {
    return kafkaUtsendingRapport.getSnittTidOppdateringIDB();
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