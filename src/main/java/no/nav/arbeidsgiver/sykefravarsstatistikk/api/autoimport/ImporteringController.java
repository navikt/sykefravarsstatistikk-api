package no.nav.arbeidsgiver.sykefravarsstatistikk.api.autoimport;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<HttpStatus> reimporter(
            @RequestParam int fraÅrstall,
            @RequestParam int fraKvartal,
            @RequestParam int tilÅrstall,
            @RequestParam int tilKvartal
    ) {
        importeringService.reimporterSykefraværsstatistikk(
                new ÅrstallOgKvartal(fraÅrstall, fraKvartal),
                new ÅrstallOgKvartal(tilÅrstall, tilKvartal)
        );
        return ResponseEntity.ok(HttpStatus.CREATED);
    }
}
