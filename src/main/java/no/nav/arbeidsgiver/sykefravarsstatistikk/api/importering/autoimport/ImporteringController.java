package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.Importeringsobjekt;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Protected
@RestController
@Slf4j
@RequestMapping(value = "importering")
@Profile({"local", "dev", "prod"})
public class ImporteringController {

    private final ImporteringService importeringService;

    public ImporteringController(ImporteringService importeringService) {
        this.importeringService = importeringService;
    }

    @PostMapping(value = "/reimport")
    public ResponseEntity<HttpStatus> reimporter(
            @RequestParam int fraÅrstall,
            @RequestParam int fraKvartal,
            @RequestParam int tilÅrstall,
            @RequestParam int tilKvartal,
            @RequestParam(required = false) List<Importeringsobjekt> importeringsobjekter
    ) {
        if (importeringsobjekter == null || importeringsobjekter.isEmpty()) {
            importeringService.reimporterSykefraværsstatistikk(
                    new ÅrstallOgKvartal(fraÅrstall, fraKvartal),
                    new ÅrstallOgKvartal(tilÅrstall, tilKvartal)
            );
        } else {
            importeringService.reimporterSykefraværsstatistikk(
                    new ÅrstallOgKvartal(fraÅrstall, fraKvartal),
                    new ÅrstallOgKvartal(tilÅrstall, tilKvartal),
                    importeringsobjekter
            );
        }
        return ResponseEntity.ok(HttpStatus.CREATED);
    }
}
