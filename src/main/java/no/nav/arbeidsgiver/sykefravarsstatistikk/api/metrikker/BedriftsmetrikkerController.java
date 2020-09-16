package no.nav.arbeidsgiver.sykefravarsstatistikk.api.metrikker;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.domene.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.domene.bransjeprogram.Bransjeprogram;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.enhetsregisteret.Underenhet;
import no.nav.security.token.support.core.api.Unprotected;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.math.BigDecimal;
import java.util.Optional;

@Unprotected
@RestController
public class BedriftsmetrikkerController {
    private final EnhetsregisteretClient enhetsregisteretClient;
    private final Bransjeprogram bransjeprogram;

    private static Logger logger = LoggerFactory.getLogger(ResponseEntityExceptionHandler.class);

    public BedriftsmetrikkerController(
            EnhetsregisteretClient enhetsregisteretClient,
            Bransjeprogram bransjeprogram) {
        this.enhetsregisteretClient = enhetsregisteretClient;
        this.bransjeprogram = bransjeprogram;
    }

    @GetMapping(value = "/{orgnr}/bedriftsmetrikker")
    public ResponseEntity<Bedriftsmetrikker> hentBedriftsmetrikker(
            @PathVariable("orgnr") String orgnrStr) {

        Orgnr orgnr = new Orgnr(orgnrStr);
        Underenhet underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(orgnr);
        Optional<Bransje> bransje = bransjeprogram.finnBransje(underenhet);
        Bedriftsmetrikker bedriftsmetrikker = Bedriftsmetrikker.builder()
                .antallAnsatte(new BigDecimal(underenhet.getAntallAnsatte()))
                .næringskode5Siffer(underenhet.getNæringskode())
                .bransje(bransje.isPresent() ? bransje.get().getType() : null)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(bedriftsmetrikker);
    }
}
