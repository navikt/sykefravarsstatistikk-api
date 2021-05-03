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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;

@Slf4j
@Service
public class KafkaService {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    private KafkaTemplate<String, String> kafkaTemplate;
    private KafkaProperties kafkaProperties;
    private KafkaUtsendingRapport kafkaUtsendingRapport;
    private int antallMålet;
    private long totaltTidUtsendingTilKafka;
    private long totaltTidOppdaterDB;

    KafkaService(
            KafkaTemplate<String, String> kafkaTemplate,
            KafkaProperties kafkaProperties,
            KafkaUtsendingRapport kafkaUtsendingRapport
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaProperties = kafkaProperties;
        this.kafkaUtsendingRapport = kafkaUtsendingRapport;
    }

    public void nullstillUtsendingRapport() {
        log.info("Gjør utsendingrapport klar før utsending på Kafka topic {}", kafkaProperties.getTopic());
        kafkaUtsendingRapport.reset();
    }

    public int getAntallMeldingerMottattForUtsending() {
        return kafkaUtsendingRapport.getAntallMeldingerMottattForUtsending();
    }

    public int getAntallMeldingerSent() {
        return kafkaUtsendingRapport.getAntallMeldingerSent();
    }

    public int getAntallMeldingerIError() {
        return kafkaUtsendingRapport.getAntallMeldingerIError();
    }


    public void send(
            ÅrstallOgKvartal årstallOgKvartal,
            VirksomhetSykefravær virksomhetSykefravær,
            List<SykefraværMedKategori> næring5SifferSykefravær,
            SykefraværMedKategori næringSykefravær,
            SykefraværMedKategori sektorSykefravær,
            SykefraværMedKategori landSykefravær
    ) {
        // TODO sjek om den gir effekt--testet ikke stor gevinst
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
        // TODO prøve å bruke Prometheus eller droppe den--
        //  sett den tilbake hvis den ikke gir effekt.-- vi testet den, var ikke stor gevinst
        kafkaUtsendingRapport.leggTilMeldingMottattForUtsending();

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
                landSykefravær);

        //TODO vurdere om vikan gå vekk fra å bruke en STATIC object mapper til hele klassen,--
        // Lage en ny objectMapper for å ha flere samtidig.
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
                            virksomhetSykefravær.getOrgnr()
                    ),
                    new Orgnr(virksomhetSykefravær.getOrgnr())
            );
            return;
        }
        // TODO vurdere å sende Object og la Kafka tolke det til JSON??
        ListenableFuture<SendResult<String, String>> futureResult = kafkaTemplate.send(
                kafkaProperties.getTopic(),
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
                kafkaUtsendingRapport.leggTilUtsending(new Orgnr(virksomhetSykefravær.getOrgnr()));
                log.debug(
                        "Melding sendt fra service til topic {}. Record.key: {}. Record.offset: {}",
                        kafkaProperties.getTopic(),
                        res.getProducerRecord().key(),
                        res.getRecordMetadata().offset()
                );
            }
        });
    }

    public long getSnittTidUtsendingTilKafka() {
        if (antallMålet == 0) {
            return 0;
        }

        return totaltTidUtsendingTilKafka / antallMålet;
    }

    public long getSnittTidOppdateringIDB() {
        if (antallMålet == 0) {
            return 0;
        }

        return totaltTidOppdaterDB / antallMålet;
    }

    public String getRåDataVedDetaljertMåling() {
        return String.format(
                "Antall målet er: '%d', totaltTidUtsendingTilKafka er '%d', totaltTidOppdaterDB er '%d'",
                antallMålet,
                totaltTidUtsendingTilKafka,
                totaltTidOppdaterDB
        );
    }

    public void addProcessingTime(
            long startUtsendingProcess,
            long stopUtsendingProcess,
            long startWriteToDb,
            long stoptWriteToDb
    ) {
        antallMålet++;
        totaltTidUtsendingTilKafka = totaltTidUtsendingTilKafka + (stopUtsendingProcess - startUtsendingProcess);
        totaltTidOppdaterDB = totaltTidOppdaterDB + (stoptWriteToDb - startWriteToDb);
    }
}
