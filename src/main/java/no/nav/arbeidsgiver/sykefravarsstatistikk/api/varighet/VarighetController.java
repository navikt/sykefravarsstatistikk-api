package no.nav.arbeidsgiver.sykefravarsstatistikk.api.varighet;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.InnloggetBruker;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Protected
@RestController
public class VarighetController {
    private final VarighetService varighetService;
    private final EnhetsregisteretClient enhetsregisteretClient;
    private final TilgangskontrollService tilgangskontrollService;

    public VarighetController(
            VarighetService varighetService,
            TilgangskontrollService tilgangskontrollService,
            EnhetsregisteretClient enhetsregisteretClient
            ) {
        this.varighetService = varighetService;
        this.tilgangskontrollService = tilgangskontrollService;
        this.enhetsregisteretClient = enhetsregisteretClient;

    }

    // TODO avklare navn til endepunkt
    @GetMapping(value = "/{orgnr}/varighet")
    public KorttidsOgLangtidsfraværSiste4Kvartaler hentVarighet(
            @PathVariable("orgnr") String orgnrStr,
            HttpServletRequest request
    ) {
        Orgnr orgnr = new Orgnr(orgnrStr);
        Underenhet underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(orgnr);
        InnloggetBruker bruker = tilgangskontrollService.hentInnloggetBruker();

        tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
                orgnr,
                bruker,
                request.getMethod(),
                "" + request.getRequestURL()
        );

        return varighetService.hentLangtidOgKorttidsSykefraværshistorikk(underenhet);
    }
}
