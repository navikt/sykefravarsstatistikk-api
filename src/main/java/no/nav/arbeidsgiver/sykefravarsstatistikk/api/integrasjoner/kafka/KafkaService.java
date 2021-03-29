package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config.KafkaProperties;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.VirksomhetSykefravær;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartalMedOrgNr;
import org.springframework.data.util.Pair;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class KafkaService {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    private KafkaTemplate<String, String> kafkaTemplate;
    private KafkaProperties kafkaProperties;

    KafkaService(KafkaTemplate<String, String> kafkaTemplate, KafkaProperties kafkaProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaProperties = kafkaProperties;
    }

    public void send(
            ÅrstallOgKvartal årstallOgKvartal,
            VirksomhetSykefravær virksomhetSykefravær,
            SykefraværMedKategori næring5SifferSykefravær,
            SykefraværMedKategori næringSykefravær,
            SykefraværMedKategori sektorSykefravær,
            SykefraværMedKategori landSykefravær) throws JsonProcessingException {

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

        AtomicInteger counter = new AtomicInteger();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        ListenableFuture<SendResult<String, String>> futureResult =
                kafkaTemplate.send(kafkaProperties.getTopic(),
                        objectMapper.writeValueAsString(key),
                        objectMapper.writeValueAsString(value)
                );

        futureResult.addCallback(
                (result) -> {
                    counter.getAndIncrement();
                    result.getProducerRecord().value();
                },
                (exception) -> log.error("Feil oppstod ved sending av melding", exception)
        );

        log.info("Sendt {} meldinger til Kafka", counter.get());
    }
}
