package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.api;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.PostImporteringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importering.SykefraværsstatistikkImporteringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.statistikk.Importeringsobjekt;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Protected
@RestController
@Slf4j
@RequestMapping(value = "importering")
@Profile({"local", "dev", "prod"})
public class ImporteringController {

    private final SykefraværsstatistikkImporteringService importeringService;
    private final PostImporteringService postImporteringService;

    public ImporteringController(
            SykefraværsstatistikkImporteringService importeringService,
            PostImporteringService postImporteringService) {
        this.importeringService = importeringService;
        this.postImporteringService = postImporteringService;
    }

    @PostMapping("/reimport")
    public ResponseEntity<HttpStatus> reimporter(
            @RequestParam int fraÅrstall,
            @RequestParam int fraKvartal,
            @RequestParam int tilÅrstall,
            @RequestParam int tilKvartal,
            @RequestParam(required = false) List<Importeringsobjekt> importeringsobjekter) {
        if (importeringsobjekter == null || importeringsobjekter.isEmpty()) {
            importeringService.reimporterSykefraværsstatistikk(
                    new ÅrstallOgKvartal(fraÅrstall, fraKvartal),
                    new ÅrstallOgKvartal(tilÅrstall, tilKvartal));
        } else {
            importeringService.reimporterSykefraværsstatistikk(
                    new ÅrstallOgKvartal(fraÅrstall, fraKvartal),
                    new ÅrstallOgKvartal(tilÅrstall, tilKvartal),
                    importeringsobjekter);
        }
        return ResponseEntity.ok(HttpStatus.CREATED);
    }

    @PostMapping("/reimportVirksomhetMetaData")
    public ResponseEntity<HttpStatus> reimportVirksomhetMetdata(
            @RequestParam int årstall, @RequestParam int kvartal) {
        ÅrstallOgKvartal årstallOgKvartal = new ÅrstallOgKvartal(årstall, kvartal);
        postImporteringService.overskrivMetadataForVirksomheter(
                årstallOgKvartal);
        postImporteringService.overskrivNæringskoderForVirksomheter(årstallOgKvartal);

        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping("/forberedNesteEksport")
    public ResponseEntity<HttpStatus> forberedNesteEksport(
            @RequestParam int årstall,
            @RequestParam int kvartal,
            @RequestParam(required = false) boolean slettHistorikk) {
        ÅrstallOgKvartal årstallOgKvartal = new ÅrstallOgKvartal(årstall, kvartal);
        Integer antallOpprettet =
                postImporteringService.forberedNesteEksport(årstallOgKvartal, slettHistorikk)
                        .getOrNull();

        if (antallOpprettet != null) {
            return ResponseEntity.ok(HttpStatus.CREATED);
        } else {
            return ResponseEntity.ok(HttpStatus.OK);
        }
    }
}
