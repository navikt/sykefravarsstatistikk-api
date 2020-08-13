package no.nav.arbeidsgiver.sykefravarsstatistikk.api.sykefraværshistorikk;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.altinn.AltinnClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.InnloggetBruker;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret.OverordnetEnhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Protected
@RestController
public class SykefraværshistorikkController {
    private final SykefraværshistorikkService sykefraværshistorikkService;
    private final TilgangskontrollService tilgangskontrollService;
    private final EnhetsregisteretClient enhetsregisteretClient;
    private final AltinnClient altinnClient;

    public SykefraværshistorikkController(
            SykefraværshistorikkService sykefraværshistorikkService,
            TilgangskontrollService tilgangskontrollService,
            EnhetsregisteretClient enhetsregisteretClient,
            AltinnClient altinnClient) {
        this.sykefraværshistorikkService = sykefraværshistorikkService;
        this.tilgangskontrollService = tilgangskontrollService;
        this.enhetsregisteretClient = enhetsregisteretClient;
        this.altinnClient = altinnClient;
    }

        @GetMapping(value = "/{orgnr}/sykefravarshistorikk")
    public List<Sykefraværshistorikk> hentSykefraværshistorikk(
            @PathVariable("orgnr") String orgnrStr,
            HttpServletRequest request
    ) {
        Orgnr orgnr = new Orgnr(orgnrStr);
        InnloggetBruker bruker = tilgangskontrollService.hentInnloggetBruker();
        altinnClient.hentRoller( bruker.getFnr(), orgnr);

        tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
                orgnr,
                bruker,
                request.getMethod(),
                "" + request.getRequestURL()
        );

        Underenhet underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(orgnr);
        OverordnetEnhet overordnetEnhet = enhetsregisteretClient.hentInformasjonOmEnhet(underenhet.getOverordnetEnhetOrgnr());

        boolean harTilgangTilOverordnetEnhet = tilgangskontrollService.hentTilgangTilOverordnetEnhetOgLoggSikkerhetshendelse(
                bruker,
                overordnetEnhet,
                underenhet,
                request.getMethod(),
                "" + request.getRequestURL()
        );

        if (harTilgangTilOverordnetEnhet) {
            return sykefraværshistorikkService.hentSykefraværshistorikk(
                    underenhet,
                    overordnetEnhet
            );
        } else {
            return sykefraværshistorikkService.hentSykefraværshistorikk(
                    underenhet,
                    overordnetEnhet.getInstitusjonellSektorkode()
            );
        }
    }
}
