package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.SykefraværFlereKvartalerForEksport;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config.KafkaProperties;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto.KafkaStatistikkKategoriTopicValue;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto.KafkaStatistikkategoriTopicKey;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto.KafkaTopicKey;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto.KafkaTopicValue;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.VirksomhetSykefravær;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Slf4j
@Service
public class KafkaService {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final KafkaProperties kafkaProperties;
  private final KafkaUtsendingRapport kafkaUtsendingRapport;
  private final KafkaUtsendingHistorikkRepository kafkaUtsendingHistorikkRepository;

  KafkaService(
      KafkaTemplate<String, String> kafkaTemplate,
      KafkaProperties kafkaProperties,
      KafkaUtsendingRapport kafkaUtsendingRapport,
      KafkaUtsendingHistorikkRepository kafkaUtsendingHistorikkRepository) {
    this.kafkaTemplate = kafkaTemplate;
    this.kafkaProperties = kafkaProperties;
    this.kafkaUtsendingRapport = kafkaUtsendingRapport;
    this.kafkaUtsendingHistorikkRepository = kafkaUtsendingHistorikkRepository;
  }

  public void nullstillUtsendingRapport(int totalMeldingerTilUtsending, String eksportNavn) {
    log.info(
        "Gjør utsendingrapport klar før utsending på Kafka topic '{}'. '{}' meldinger vil bli sendt.",
        kafkaProperties.getTopicNavn(eksportNavn),
        totalMeldingerTilUtsending);
    kafkaUtsendingRapport.reset(totalMeldingerTilUtsending);
  }

  public int getAntallMeldingerMottattForUtsending() {
    return kafkaUtsendingRapport.getAntallMeldingerMottattForUtsending();
  }

  public boolean sendTilStatistikkKategoriTopic(
      ÅrstallOgKvartal årstallOgKvartal,
      Statistikkategori statistikkategori,
      String identifikator,
      SykefraværMedKategori sykefraværMedKategori,
      SykefraværFlereKvartalerForEksport sykefraværOverFlereKvartaler) {
    kafkaUtsendingRapport.leggTilMeldingMottattForUtsending();
    KafkaStatistikkategoriTopicKey key =
        new KafkaStatistikkategoriTopicKey(
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
              "Kunne ikke parse statistikk '%s' til Json. Statistikk ikke sent",
              statistikkategori.name()));
      return false;
    }

    ListenableFuture<SendResult<String, String>> futureResult =
        kafkaTemplate.send(
            kafkaProperties.getTopicNavn(statistikkategori.name()),
            keyAsJsonString,
            dataAsJsonString);

    futureResult.addCallback(
        new ListenableFutureCallback<>() {
          @Override
          public void onFailure(Throwable throwable) {
            kafkaUtsendingRapport.leggTilError(
                String.format(
                    "Utsending feilet for statistikk kategori '%s' og kode '%s',  med melding '%s'",
                    sykefraværMedKategori.getKategori().name(),
                    sykefraværMedKategori.getKode(),
                    throwable.getMessage()));
          }

          @Override
          public void onSuccess(SendResult<String, String> res) {
            kafkaUtsendingRapport.leggTilUtsendingSuksess();
            log.debug(
                "Melding sendt fra service til topic {}. Record.key: {}. Record.offset: {}",
                kafkaProperties.getTopicNavn(statistikkategori.name()),
                res.getProducerRecord().key(),
                res.getRecordMetadata().offset());

            if (Statistikkategori.VIRKSOMHET == statistikkategori) {
              kafkaUtsendingHistorikkRepository.opprettHistorikk(
                  identifikator, // for Statistikkategori.VIRKSOMHET er identifikator et orgnr
                  keyAsJsonString,
                  dataAsJsonString);
            }
          }
        });

    return true;
  }

  public void send(
      ÅrstallOgKvartal årstallOgKvartal,
      VirksomhetSykefravær virksomhetSykefravær,
      List<SykefraværMedKategori> næring5SifferSykefravær,
      SykefraværMedKategori næringSykefravær,
      SykefraværMedKategori sektorSykefravær,
      SykefraværMedKategori landSykefravær) {
    // TODO bytt til Prometheus
    kafkaUtsendingRapport.leggTilMeldingMottattForUtsending();

    if (kafkaUtsendingRapport.getAntallMeldingerIError() > 5) {
      throw new KafkaUtsendingException(
          String.format(
              "Antall error:'%d'. Avbryter eksportering. Totalt meldinger som var klar for sending er: '%d'."
                  + " Antall meldinger som har egentlig blitt sendt: '%d'",
              kafkaUtsendingRapport.getAntallMeldingerIError(),
              kafkaUtsendingRapport.getAntallMeldingerSent(),
              kafkaUtsendingRapport.getAntallMeldingerMottattForUtsending()));
    }

    KafkaTopicKey key =
        new KafkaTopicKey(
            virksomhetSykefravær.getOrgnr(),
            årstallOgKvartal.getKvartal(),
            årstallOgKvartal.getÅrstall());
    KafkaTopicValue value =
        new KafkaTopicValue(
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

    ListenableFuture<SendResult<String, String>> futureResult =
        kafkaTemplate.send(
            kafkaProperties.getTopicNavn(KafkaProperties.EKSPORT_ALLE_KATEGORIER),
            keyAsJsonString,
            dataAsJsonString);

    futureResult.addCallback(
        new ListenableFutureCallback<>() {
          @Override
          public void onFailure(Throwable throwable) {
            kafkaUtsendingRapport.leggTilError(
                String.format(
                    "Utsending feilet for orgnr '%s' med melding '%s'",
                    virksomhetSykefravær.getOrgnr(), throwable.getMessage()),
                new Orgnr(virksomhetSykefravær.getOrgnr()));
          }

          @Override
          public void onSuccess(SendResult<String, String> res) {
            kafkaUtsendingRapport.leggTilUtsendingSuksess(
                new Orgnr(virksomhetSykefravær.getOrgnr()));
            log.debug(
                "Melding sendt fra service til topic {}. Record.key: {}. Record.offset: {}",
                kafkaProperties.getTopicNavn(KafkaProperties.EKSPORT_ALLE_KATEGORIER),
                res.getProducerRecord().key(),
                res.getRecordMetadata().offset());
            kafkaUtsendingHistorikkRepository.opprettHistorikk(
                virksomhetSykefravær.getOrgnr(), keyAsJsonString, dataAsJsonString);
          }
        });
  }

  public long getSnittTidUtsendingTilKafka() {
    return kafkaUtsendingRapport.getSnittTidUtsendingTilKafka();
  }

  public long getSnittTidOppdateringIDB() {
    return kafkaUtsendingRapport.getSnittTidOppdateringIDB();
  }

  public String getRåDataVedDetaljertMåling() {
    return kafkaUtsendingRapport.getRåDataVedDetaljertMåling();
  }

  public void addUtsendingTilKafkaProcessingTime(
      long startUtsendingProcess, long stopUtsendingProcess) {
    kafkaUtsendingRapport.addUtsendingTilKafkaProcessingTime(
        startUtsendingProcess, stopUtsendingProcess);
  }

  public void addDBOppdateringProcessingTime(
      long startDBOppdateringProcess, long stopDBOppdateringProcess) {
    kafkaUtsendingRapport.addDBOppdateringProcessingTime(
        startDBOppdateringProcess, stopDBOppdateringProcess);
  }
}
