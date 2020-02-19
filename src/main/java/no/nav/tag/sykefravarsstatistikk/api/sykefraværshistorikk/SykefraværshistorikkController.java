package no.nav.tag.sykefravarsstatistikk.api.sykefraværshistorikk;

import no.nav.security.oidc.api.Protected;
import no.nav.tag.sykefravarsstatistikk.api.domene.InnloggetBruker;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollException;
import no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Protected
@RestController
public class SykefraværshistorikkController {
    private final SykefraværshistorikkService sykefraværshistorikkService;
    private final TilgangskontrollService tilgangskontrollService;

    public SykefraværshistorikkController(SykefraværshistorikkService sykefraværshistorikkService, TilgangskontrollService tilgangskontrollService) {
        this.sykefraværshistorikkService = sykefraværshistorikkService;
        this.tilgangskontrollService = tilgangskontrollService;
    }

    @GetMapping(value = "/{orgnr}/sykefravarshistorikk")
    public List<Sykefraværshistorikk> hentSykefraværshistorikk(@PathVariable("orgnr") String orgnrStr) {

        Orgnr orgnr = new Orgnr(orgnrStr);
        InnloggetBruker innloggetSelvbetjeningBruker = tilgangskontrollService.hentInnloggetBruker();

        if (!innloggetSelvbetjeningBruker.harTilgang(orgnr)) {
            throw new TilgangskontrollException("Har ikke tilgang til statistikk for denne bedriften.");
        }

        return sykefraværshistorikkService.hentSykefraværshistorikk(orgnr);
    }


}
