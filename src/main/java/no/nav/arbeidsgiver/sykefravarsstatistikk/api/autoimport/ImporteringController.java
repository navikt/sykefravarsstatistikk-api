package no.nav.arbeidsgiver.sykefravarsstatistikk.api.autoimport;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Protected
@RestController
@Slf4j
@RequestMapping(value = "importering")
@Profile({"local", "dev"})
public class ImporteringController {

    private final ImporteringService importeringService;

    public ImporteringController(ImporteringService importeringService) {
        this.importeringService = importeringService;
    }

    @PostMapping(value = "/statistikk")
    public void importStatistikk() {
        importeringService.importerHvisDetFinnesNyStatistikk();
    }

    @PostMapping(value = "/reimport")
    public void reimporter() {
        importeringService.importerHvisDetFinnesNyStatistikk();
    }
}
