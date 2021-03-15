package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.EksporteringService;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Unprotected
@RestController
public class KafkaController {
    final EksporteringService eksporteringService;

    public KafkaController(EksporteringService eksporteringService) {
        this.eksporteringService = eksporteringService;
    }

    @GetMapping(value = "/sendkafka")
    public void sendKafka() {
        eksporteringService.eksporterHvisDetFinnesNyStatistikk();

    }


}
