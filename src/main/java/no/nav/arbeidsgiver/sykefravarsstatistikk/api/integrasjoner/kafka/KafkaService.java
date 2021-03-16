package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config.KafkaProperties;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartalMedOrgNr;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@Service
public class KafkaService {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    private KafkaTemplate<String, String> kafkaTemplate;
    private KafkaProperties kafkaProperties;
    private EnhetsregisteretClient enhetsregisteretClient;

    KafkaService(KafkaTemplate<String, String> kafkaTemplate, KafkaProperties kafkaProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaProperties = kafkaProperties;
    }

    public void send(SykefraværForEttKvartalMedOrgNr sykefraværForEttKvartalMedOrgNr) throws JsonProcessingException {
        KafkaTopicKey key = new KafkaTopicKey(
                sykefraværForEttKvartalMedOrgNr.getOrgnr(),
                sykefraværForEttKvartalMedOrgNr.getKvartal(),
                sykefraværForEttKvartalMedOrgNr.getÅrstall()
        );
        Næringskode5Siffer næringskode = null;
        try {
            næringskode = enhetsregisteretClient.hentInformasjonOmVirksomhet(
                    new Orgnr("925028509")
            ).getNæringskode();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                log.error("OrgNr finnes ikke i Enhetsregisteret", e);
            } else {
                log.error("Det skjedde en feil ved henting av informasjon av enhet", e);
            }
            return;
        } catch (Exception ex) {
            log.error("Det skjedde en feil ved henting av informasjon av enhet", ex);
            return;
        }
        KafkaTopicValue value = new KafkaTopicValue(
                næringskode.getKode(),
                sykefraværForEttKvartalMedOrgNr.getÅrstall(),
                sykefraværForEttKvartalMedOrgNr.getKvartal(),
                sykefraværForEttKvartalMedOrgNr.isErMaskert(),
                sykefraværForEttKvartalMedOrgNr.getProsent(),
                sykefraværForEttKvartalMedOrgNr.getTapteDagsverk(),
                sykefraværForEttKvartalMedOrgNr.getMuligeDagsverk()
        );
        ListenableFuture<SendResult<String, String>> futureResult =
                kafkaTemplate.send(kafkaProperties.getTopic(),
                        objectMapper.writeValueAsString(key),
                        objectMapper.writeValueAsString(value)
                );
        futureResult.addCallback(
                (result) -> {
                    log.info("Melding sendt på topic");
                    try {
                        JsonNode keyJson = objectMapper.readTree(result.getProducerRecord().key());
                        String orgnr = keyJson.hasNonNull("orgnr") ? keyJson.get("orgnr").asText() : null;
                        Integer kvartal = keyJson.hasNonNull("kvartal") ? keyJson.get("kvartal").asInt() : null;
                        Integer årstall = keyJson.hasNonNull("årstall") ? keyJson.get("årstall").asInt() : null;

                        log.info("Sendt melding med key={orgnr:${}, kvartal:${}, årstall:${}}", orgnr, kvartal, årstall);

                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    result.getProducerRecord().value();

                },
                (exception) -> log.error("Feil oppstod ved sending av melding", exception)
        );
    }
}
