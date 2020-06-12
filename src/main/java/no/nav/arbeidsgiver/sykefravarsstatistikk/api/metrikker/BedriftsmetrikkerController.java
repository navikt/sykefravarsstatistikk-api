package no.nav.arbeidsgiver.sykefravarsstatistikk.api.metrikker;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.bransjeprogram.Bransjeprogram;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import no.nav.security.oidc.api.Unprotected;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Optional;

@Unprotected
@RestController
public class BedriftsmetrikkerController {
    private final EnhetsregisteretClient enhetsregisteretClient;
    private final Bransjeprogram bransjeprogram;

    public BedriftsmetrikkerController(
            EnhetsregisteretClient enhetsregisteretClient,
            Bransjeprogram bransjeprogram) {
        this.enhetsregisteretClient = enhetsregisteretClient;
        this.bransjeprogram = bransjeprogram;
    }

    @GetMapping(value = "/{orgnr}/bedriftsmetrikker")
    public Bedriftsmetrikker hentBedriftsmetrikker(
            @PathVariable("orgnr") String orgnrStr,
            HttpServletRequest request
    ) {

        Orgnr orgnr = new Orgnr(orgnrStr);
        Underenhet underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(orgnr);
        Optional<Bransje> bransje = bransjeprogram.finnBransje(underenhet);

        return Bedriftsmetrikker.builder()
                .antallAnsatte(new BigDecimal(underenhet.getAntallAnsatte()))
                .næringskode5Siffer(underenhet.getNæringskode())
                .bransje(bransje.isPresent() ? bransje.get().getType() : null)
                .build();
    }
}
