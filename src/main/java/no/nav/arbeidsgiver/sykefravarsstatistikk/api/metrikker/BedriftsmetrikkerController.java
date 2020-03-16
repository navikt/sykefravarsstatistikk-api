package no.nav.arbeidsgiver.sykefravarsstatistikk.api.metrikker;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import no.nav.security.oidc.api.Unprotected;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

@Unprotected
@RestController
public class BedriftsmetrikkerController {
    private final EnhetsregisteretClient enhetsregisteretClient;

    public BedriftsmetrikkerController(
            EnhetsregisteretClient enhetsregisteretClient
    ) {
        this.enhetsregisteretClient = enhetsregisteretClient;
    }

    @GetMapping(value = "/{orgnr}/bedriftsmetrikker")
    public Bedriftsmetrikker hentBedriftsmetrikker(
            @PathVariable("orgnr") String orgnrStr,
            HttpServletRequest request
    ) {

        Orgnr orgnr = new Orgnr(orgnrStr);
        Underenhet underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(orgnr);

        return Bedriftsmetrikker.builder()
                .antallAnsatte(new BigDecimal(underenhet.getAntallAnsatte()))
                .næringskode5Siffer(underenhet.getNæringskode())
                .build();
    }
}
