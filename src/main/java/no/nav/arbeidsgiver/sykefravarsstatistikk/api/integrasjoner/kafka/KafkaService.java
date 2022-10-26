package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config.KafkaProperties;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.VirksomhetSykefravær;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.StatistikkDto;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class KafkaService {
    private final static ObjectMapper objectMapper = new ObjectMapper();

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

    public void nullstillUtsendingRapport(int totalMeldingerTilUtsending) {
        log.info("Gjør utsendingrapport klar før utsending på Kafka topic '{}'. '{}' meldinger vil bli sendt.",
                kafkaProperties.getTopic(),
                totalMeldingerTilUtsending
        );
        kafkaUtsendingRapport.reset(totalMeldingerTilUtsending);
    }

    public int getAntallMeldingerMottattForUtsending() {
        return kafkaUtsendingRapport.getAntallMeldingerMottattForUtsending();
    }

    public void send(
          ÅrstallOgKvartal årstallOgKvartal,
          VirksomhetSykefravær virksomhetSykefravær,
          List<SykefraværMedKategori> næring5SifferSykefravær,
          SykefraværMedKategori næringSykefravær,
          SykefraværMedKategori sektorSykefravær,
          SykefraværMedKategori landSykefravær,
          List<StatistikkDto> landSykefraværSiste4Kvartaler
          // TODO: Legge til prosent her???
    ) {
        // TODO bytt til Prometheus
        kafkaUtsendingRapport.leggTilMeldingMottattForUtsending();

        if (kafkaUtsendingRapport.getAntallMeldingerIError() > 5) {
            throw new KafkaUtsendingException(
                    String.format(
                            "Antall error:'%d'. Avbryter eksportering. Totalt meldinger som var klar for sending er: '%d'." +
                                    " Antall meldinger som har egentlig blitt sendt: '%d'",
                            kafkaUtsendingRapport.getAntallMeldingerIError(),
                            kafkaUtsendingRapport.getAntallMeldingerSent(),
                            kafkaUtsendingRapport.getAntallMeldingerMottattForUtsending()
                    )
            );
        }

        KafkaTopicKey key = new KafkaTopicKey(
                virksomhetSykefravær.getOrgnr(),
                årstallOgKvartal.getKvartal(),
                årstallOgKvartal.getÅrstall()
        );
        KafkaTopicValue value = new KafkaTopicValue(
                virksomhetSykefravær,
                næring5SifferSykefravær,
                næringSykefravær,
                sektorSykefravær,
                landSykefravær,
                landSykefraværSiste4Kvartaler
                );
        KafkaTopicLandValue valueLand = new KafkaTopicLandValue(
                landSykefravær,
                landSykefraværSiste4Kvartaler
                );

        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        String keyAsJsonString;
        String dataAsJsonString;
        String dataLandAsJsonString;
        try {
            keyAsJsonString = objectMapper.writeValueAsString(key);
            dataAsJsonString = objectMapper.writeValueAsString(value);
            dataLandAsJsonString = objectMapper.writeValueAsString(valueLand);
        } catch (JsonProcessingException e) {
            kafkaUtsendingRapport.leggTilError(
                    String.format(
                            "Kunne ikke parse orgnr '%s' til Json. Statistikk ikke sent for virksomheten.",
                            virksomhetSykefravær.getOrgnr()
                    ),
                    new Orgnr(virksomhetSykefravær.getOrgnr())
            );
            return;
        }

        ListenableFuture<SendResult<String, String>> futureResult = kafkaTemplate.send(
                kafkaProperties.getTopic().get(0),
                keyAsJsonString,
                dataAsJsonString
        );

        futureResult.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable throwable) {
                kafkaUtsendingRapport.leggTilError(
                        String.format(
                                "Utsending feilet for orgnr '%s' med melding '%s'",
                                virksomhetSykefravær.getOrgnr(),
                                throwable.getMessage()
                        ),
                        new Orgnr(virksomhetSykefravær.getOrgnr())
                );
            }

            @Override
            public void onSuccess(SendResult<String, String> res) {
                kafkaUtsendingRapport.leggTilUtsendingSuksess(new Orgnr(virksomhetSykefravær.getOrgnr()));
                log.debug(
                        "Melding sendt fra service til topic {}. Record.key: {}. Record.offset: {}",
                        kafkaProperties.getTopic(),
                        res.getProducerRecord().key(),
                        res.getRecordMetadata().offset()
                );
                kafkaUtsendingHistorikkRepository.opprettHistorikk(
                        virksomhetSykefravær.getOrgnr(),
                        keyAsJsonString,
                        dataAsJsonString
                );
            }
        });

        ListenableFuture<SendResult<String, String>> futureResultLand = kafkaTemplate.send(
                kafkaProperties.getTopic().get(1),
                keyAsJsonString,
                dataLandAsJsonString
        );

        futureResultLand.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable throwable) {
                kafkaUtsendingRapport.leggTilError(
                        String.format(
                                "Utsending feilet for orgnr '%s' med melding '%s'",
                                virksomhetSykefravær.getOrgnr(),
                                throwable.getMessage()
                        ),
                        new Orgnr(virksomhetSykefravær.getOrgnr())
                );
            }

            @Override
            public void onSuccess(SendResult<String, String> res) {
                kafkaUtsendingRapport.leggTilUtsendingSuksess(new Orgnr(virksomhetSykefravær.getOrgnr()));
                log.debug(
                        "Melding sendt fra service til topic {}. Record.key: {}. Record.offset: {}",
                        kafkaProperties.getTopic(),
                        res.getProducerRecord().key(),
                        res.getRecordMetadata().offset()
                );
                kafkaUtsendingHistorikkRepository.opprettHistorikk(
                        virksomhetSykefravær.getOrgnr(),
                        keyAsJsonString,
                        dataAsJsonString
                );
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

    public void addUtsendingTilKafkaProcessingTime(long startUtsendingProcess, long stopUtsendingProcess) {
        kafkaUtsendingRapport.addUtsendingTilKafkaProcessingTime(startUtsendingProcess, stopUtsendingProcess);
    }

    public void addDBOppdateringProcessingTime(long startDBOppdateringProcess, long stopDBOppdateringProcess) {
        kafkaUtsendingRapport.addDBOppdateringProcessingTime(startDBOppdateringProcess, stopDBOppdateringProcess);
    }


    public int sendTilSykefraværsstatistikkLandTopic(
            ÅrstallOgKvartal årstallOgKvartal,
            SykefraværMedKategori landSykefraværSisteKvartal,
            List<StatistikkDto> statistikkDtosSiste12Måneder
    ) {
        return 1;
    }
}
