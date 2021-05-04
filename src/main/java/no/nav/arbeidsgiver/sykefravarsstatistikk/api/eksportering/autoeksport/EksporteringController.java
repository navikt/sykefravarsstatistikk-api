package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.security.token.support.core.api.Protected;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Protected
@RestController
@Slf4j
@RequestMapping(value = "eksportering")
@Profile({"local", "dev", "prod"})
public class EksporteringController {

    private final EksporteringService eksporteringService;

    public EksporteringController(EksporteringService eksporteringService) {
        this.eksporteringService = eksporteringService;
    }

    @PostMapping("/reeksport")
    public ResponseEntity<HttpStatus> reeksportMedKafka(
            @RequestParam int årstall,
            @RequestParam int kvartal,
            @RequestParam(required = false) int begrensningTil
    ) {
        EksporteringBegrensning eksporteringBegrensning = begrensningTil == 0 ?
                EksporteringBegrensning.build().utenBegrensning() :
                EksporteringBegrensning.build().medBegrensning(begrensningTil);

        ÅrstallOgKvartal årstallOgKvartal = new ÅrstallOgKvartal(årstall, kvartal);
        int antallEksportert =
                eksporteringService.eksporter(årstallOgKvartal, eksporteringBegrensning);

        if (antallEksportert >= 0) {
            return ResponseEntity.ok(HttpStatus.CREATED);
        } else {
            return ResponseEntity.ok(HttpStatus.OK);
        }
    }
}
