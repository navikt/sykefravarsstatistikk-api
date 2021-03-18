package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.EksporteringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartalMedOrgNr;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Unprotected
@RestController
public class KafkaController {
    final KafkaService kafkaService;
    final EksporteringService eksporteringService;

    public KafkaController(KafkaService kafkaService, EksporteringService eksporteringService) {
        this.kafkaService = kafkaService;
        this.eksporteringService = eksporteringService;
    }

    SykefraværForEttKvartalMedOrgNr dummy = new SykefraværForEttKvartalMedOrgNr(
            new ÅrstallOgKvartal(2020, 2),
            "999999999",
            new BigDecimal(10),
            new BigDecimal(100),
            51, "08500"
    );

    @GetMapping(value = "/sendkafka")
    public void sendKafka() {
        eksporteringService.eksporterHvisDetFinnesNyStatistikk();
        //kafkaService.send(dummy);
    }
}
