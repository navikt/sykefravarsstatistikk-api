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
                        "Melding sendt på topic {}. Record.key: {}. Record.offset: {}",
                        kafkaProperties.getTopic(),
                        res.getProducerRecord().key(),
                        res.getRecordMetadata().offset()
                );
            }
        });
    }
}
