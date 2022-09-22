package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.Importeringsobjekt;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.context.annotation.Profile;
import org.springframework.data.util.Pair;
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

    private final SykefraværsstatistikkImporteringService importeringService;
    private final ImporteringKvalitetssjekkService importeringKvalitetssjekkService;
    private final PostImporteringService postImporteringService;

    public ImporteringController(
            SykefraværsstatistikkImporteringService importeringService,
            ImporteringKvalitetssjekkService importeringKvalitetssjekkService,
            PostImporteringService postImporteringService
    ) {
        this.importeringService = importeringService;
        this.importeringKvalitetssjekkService = importeringKvalitetssjekkService;
        this.postImporteringService = postImporteringService;
    }

    @PostMapping("/reimport")
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

    @PostMapping("/reimportVirksomhetMetaData")
    public ResponseEntity<HttpStatus> reimportVirksomhetMetdata(
            @RequestParam int årstall,
            @RequestParam int kvartal
    ) {
        ÅrstallOgKvartal årstallOgKvartal = new ÅrstallOgKvartal(årstall, kvartal);
        Pair<Integer, Integer> antallImportert =
                postImporteringService.importVirksomhetMetadataOgVirksomhetNæringskode5sifferMapping(årstallOgKvartal);

        if (antallImportert.getFirst() >= 0) {
            return ResponseEntity.ok(HttpStatus.CREATED);
        } else {
            return ResponseEntity.ok(HttpStatus.OK);
        }
    }

    @PostMapping("/forberedNesteEksport")
    public ResponseEntity<HttpStatus> forberedNesteEksport(
            @RequestParam int årstall,
            @RequestParam int kvartal
    ) {
        ÅrstallOgKvartal årstallOgKvartal = new ÅrstallOgKvartal(årstall, kvartal);
        int antallOpprettet = postImporteringService.forberedNesteEksport(årstallOgKvartal);

        if (antallOpprettet >= 0) {
            return ResponseEntity.ok(HttpStatus.CREATED);
        } else {
            return ResponseEntity.ok(HttpStatus.OK);
        }
    }


    @GetMapping("/kvalitetssjekk")
    public ResponseEntity<List<String>> testAvNæringMedVarighetOgGradering() {
        return ResponseEntity.ok(
                importeringKvalitetssjekkService.kvalitetssjekkNæringMedVarighetOgMedGraderingMotNæringstabell()
        );
    }
}
