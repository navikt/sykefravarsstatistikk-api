package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.IngenNæringException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis.KvartalsvisSykefraværshistorikk;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis.OffentligKvartalsvisSykefraværshistorikkService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.InnloggetBruker;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@Protected
@RestController
public class OffentligSykefraværshistorikkController {

    private final OffentligKvartalsvisSykefraværshistorikkService offentligKvartalsvisSykefraværshistorikkService;
    private final TilgangskontrollService tilgangskontrollService;
    private final EnhetsregisteretClient enhetsregisteretClient;

    public OffentligSykefraværshistorikkController(OffentligKvartalsvisSykefraværshistorikkService offentligKvartalsvisSykefraværshistorikkService, TilgangskontrollService tilgangskontrollService, EnhetsregisteretClient enhetsregisteretClient) {
        this.offentligKvartalsvisSykefraværshistorikkService = offentligKvartalsvisSykefraværshistorikkService;
        this.tilgangskontrollService = tilgangskontrollService;
        this.enhetsregisteretClient = enhetsregisteretClient;
    }


    @GetMapping(value = "/{orgnr}/v1/offentlig/sykefravarshistorikk/kvartalsvis")
    public List<KvartalsvisSykefraværshistorikk> hentOffentligeSykefraværsprosenter(
            @PathVariable("orgnr") String orgnrStr,
            HttpServletRequest request
    ) {
        InnloggetBruker bruker = tilgangskontrollService.hentInnloggetBrukerForAlleRettigheter();

        tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
                new Orgnr(orgnrStr),
                bruker,
                request.getMethod(),
                "" + request.getRequestURL()
        );

        Underenhet underenhet;
        try {
            underenhet = enhetsregisteretClient.hentUnderenhet(new Orgnr(orgnrStr));
        } catch (IngenNæringException e) {
            log.info("Underenhet har ingen næring. Returnerer 204 - No Content");
            return null;
        }

        return offentligKvartalsvisSykefraværshistorikkService.hentSykefraværshistorikkV1Offentlig(
                underenhet
        );
    }
}