package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.varighet;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.domene.InnloggetBruker;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.domene.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.enhetsregisteret.Underenhet;
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

    @GetMapping(value = "/{orgnr}/varighetsiste4kvartaler")
    public KorttidsOgLangtidsfraværSiste4Kvartaler hentVarighet(
            @PathVariable("orgnr") String orgnrStr,
            HttpServletRequest request
    ) {
        Orgnr orgnr = new Orgnr(orgnrStr);

        InnloggetBruker bruker = tilgangskontrollService.hentInnloggetBruker();
        tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
                orgnr,
                bruker,
                request.getMethod(),
                "" + request.getRequestURL()
        );
        Underenhet underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(orgnr);


        return varighetService.hentKorttidsOgLangtidsfraværSiste4Kvartaler(underenhet, new ÅrstallOgKvartal(2020, 2));
    }
}
