package no.nav.tag.sykefravarsstatistikk.api.sykefravarprosenthistrorikk;

import no.nav.security.oidc.api.Protected;
import no.nav.tag.sykefravarsstatistikk.api.domene.InnloggetBruker;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollException;
import no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Protected
@RestController
public class SykefraværprosentHistorikkController {
    private final SykefraværprosentHistorikkService sykefraværprosentHistorikkService;
    private final TilgangskontrollService tilgangskontrollService;

    public SykefraværprosentHistorikkController(SykefraværprosentHistorikkService sykefraværprosentHistorikkService, TilgangskontrollService tilgangskontrollService) {
        this.sykefraværprosentHistorikkService = sykefraværprosentHistorikkService;
        this.tilgangskontrollService = tilgangskontrollService;
    }

    @GetMapping(value = "/sykefravarprosenthistorikk/land")
    public KvartalsvisSykefraværprosentHistorikk hentStatistikkLand() {
        return sykefraværprosentHistorikkService.hentKvartalsvisSykefraværprosentHistorikkLand();
    }

    @GetMapping(value = "/{orgnr}/sykefravarprosenthistorikk/sektor")
    public KvartalsvisSykefraværprosentHistorikk hentStatistikkSektor(@PathVariable("orgnr") String orgnrStr) {
        Orgnr orgnr = new Orgnr(orgnrStr);
        InnloggetBruker innloggetSelvbetjeningBruker = tilgangskontrollService.hentInnloggetBruker();

        if (!innloggetSelvbetjeningBruker.harTilgang(orgnr)) {
            throw new TilgangskontrollException("Har ikke tilgang til statistikk for denne bedriften.");
        }

        return sykefraværprosentHistorikkService.hentKvartalsvisSykefraværprosentHistorikkSektor(orgnr);
    }

}
