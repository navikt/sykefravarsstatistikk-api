package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.EksporteringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartalMedOrgNr;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Protected
@Profile({"local","dev"})
@RestController
public class KafkaController {
    final KafkaService kafkaService;
    final EksporteringService eksporteringService;

    public KafkaController(KafkaService kafkaService, EksporteringService eksporteringService) {
        this.kafkaService = kafkaService;
        this.eksporteringService = eksporteringService;
    }

    @GetMapping(value = "/sendkafka")
    public void sendKafka() {
        eksporteringService.eksporterHvisDetFinnesNyStatistikk();
    }
}
