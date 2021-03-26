package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config.KafkaProperties;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartalMedOrgNr;
import org.springframework.data.util.Pair;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

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

    public void send(SykefraværForEttKvartalMedOrgNr sykefraværForEttKvartalMedOrgNr,
                     SykefraværForEttKvartal næringSykefraværForEttKvartal/* næring2siffer, sektor, land*/) throws JsonProcessingException {
        KafkaTopicKey key = new KafkaTopicKey(
                sykefraværForEttKvartalMedOrgNr.getOrgnr(),
                sykefraværForEttKvartalMedOrgNr.getKvartal(),
                sykefraværForEttKvartalMedOrgNr.getÅrstall()
        );
        Pair<Statistikkategori, SykefraværForEttKvartal> næringSkategoriMedSykefravaærForEttKvartal
                = Pair.of(Statistikkategori.NÆRING, næringSykefraværForEttKvartal);
        KafkaTopicValue value = new KafkaTopicValue(
                sykefraværForEttKvartalMedOrgNr.getÅrstall(),
                sykefraværForEttKvartalMedOrgNr.getKvartal(),
                sykefraværForEttKvartalMedOrgNr.getNæringskode5Siffer(),
                sykefraværForEttKvartalMedOrgNr.isErMaskert(),
                sykefraværForEttKvartalMedOrgNr.getProsent(),
                sykefraværForEttKvartalMedOrgNr.getTapteDagsverk(),
                sykefraværForEttKvartalMedOrgNr.getMuligeDagsverk(),
                næringSkategoriMedSykefravaærForEttKvartal);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        ListenableFuture<SendResult<String, String>> futureResult =
                kafkaTemplate.send(kafkaProperties.getTopic(),
                        objectMapper.writeValueAsString(key),
                        objectMapper.writeValueAsString(value)
                );
        futureResult.addCallback(
                (result) -> {
                    try {
                        JsonNode keyJson = objectMapper.readTree(result.getProducerRecord().key());
                        String orgnr = keyJson.hasNonNull("orgnr") ? keyJson.get("orgnr").asText() : null;
                        Integer kvartal = keyJson.hasNonNull("kvartal") ? keyJson.get("kvartal").asInt() : null;
                        Integer årstall = keyJson.hasNonNull("årstall") ? keyJson.get("årstall").asInt() : null;
                        log.info("Sendt følgende object til kafka: key: " + key.toString() + ", value: " + value.toString());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    result.getProducerRecord().value();

                },
                (exception) -> log.error("Feil oppstod ved sending av melding", exception)
        );
    }
}
