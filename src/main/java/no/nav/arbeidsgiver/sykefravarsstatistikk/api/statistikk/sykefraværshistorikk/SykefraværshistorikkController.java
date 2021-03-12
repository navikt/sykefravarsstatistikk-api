package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.OverordnetEnhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis.KvartalsvisSykefraværshistorikk;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis.KvartalsvisSykefraværshistorikkService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SummertSykefraværService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SummertSykefraværshistorikk;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.InnloggetBruker;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@Protected
@RestController
public class SykefraværshistorikkController {
    private final KvartalsvisSykefraværshistorikkService kvartalsvisSykefraværshistorikkService;
    private final TilgangskontrollService tilgangskontrollService;
    private final EnhetsregisteretClient enhetsregisteretClient;
    private final SummertSykefraværService summertSykefraværService;

    public SykefraværshistorikkController(
            KvartalsvisSykefraværshistorikkService kvartalsvisSykefraværshistorikkService,
            TilgangskontrollService tilgangskontrollService,
            EnhetsregisteretClient enhetsregisteretClient,
            SummertSykefraværService summertSykefraværService) {
        this.kvartalsvisSykefraværshistorikkService = kvartalsvisSykefraværshistorikkService;
        this.tilgangskontrollService = tilgangskontrollService;
        this.enhetsregisteretClient = enhetsregisteretClient;
        this.summertSykefraværService = summertSykefraværService;
    }

    @GetMapping(value = "/{orgnr}/sykefravarshistorikk/kvartalsvis")
    public List<KvartalsvisSykefraværshistorikk> hentSykefraværshistorikk(
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
        OverordnetEnhet overordnetEnhet = enhetsregisteretClient.hentInformasjonOmEnhet(underenhet.getOverordnetEnhetOrgnr());

        boolean harTilgangTilOverordnetEnhet = tilgangskontrollService.hentTilgangTilOverordnetEnhetOgLoggSikkerhetshendelse(
                bruker,
                overordnetEnhet,
                underenhet,
                request.getMethod(),
                "" + request.getRequestURL()
        );

        if (harTilgangTilOverordnetEnhet) {
            return kvartalsvisSykefraværshistorikkService.hentSykefraværshistorikk(
                    underenhet,
                    overordnetEnhet
            );
        } else {
            return kvartalsvisSykefraværshistorikkService.hentSykefraværshistorikk(
                    underenhet,
                    overordnetEnhet.getInstitusjonellSektorkode()
            );
        }
    }


    @GetMapping(value = "/{orgnr}/sykefravarshistorikk/summert")
    public List<SummertSykefraværshistorikk> hentSummertKorttidsOgLangtidsfraværV2(
            @PathVariable("orgnr") String orgnrStr,
            @RequestParam("antallKvartaler") int antallKvartaler,
            HttpServletRequest request
    ) {

        InnloggetBruker bruker = tilgangskontrollService.hentInnloggetBruker();
        tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
                new Orgnr(orgnrStr),
                bruker,
                request.getMethod(),
                "" + request.getRequestURL()
        );
        Underenhet underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(new Orgnr(orgnrStr));

        if (antallKvartaler != 4) {
            throw new IllegalArgumentException("For øyeblikket støtter vi kun summering av 4 kvartaler.");
        }

        SummertSykefraværshistorikk summertSykefraværshistorikkVirksomhet =
                summertSykefraværService.hentSummertSykefraværshistorikk(
                        underenhet,
                        new ÅrstallOgKvartal(2020, 4),
                        antallKvartaler
                );

        SummertSykefraværshistorikk summertSykefraværshistorikkBransjeEllerNæring =
                summertSykefraværService.hentSummertSykefraværshistorikkForBransjeEllerNæring(
                        underenhet,
                        new ÅrstallOgKvartal(2020, 4),
                        antallKvartaler
                );

        return Arrays.asList(summertSykefraværshistorikkVirksomhet, summertSykefraværshistorikkBransjeEllerNæring);
    }

    // TODO oppsummerrer at vi ikke bruker det og sletter det
    // publiserStkforKvartal. . land, sektor,næring2 siffer, næring5siffer, virksomhet
    // repost // select distinct orgnr from stk_virksomhet
    @PostMapping(value = "/publiser-statistikk-for-kvartal/{arstall}/{kvartal}/")
    public ResponseStatus publiserStatistikkForEtKvartal(
            @PathVariable("arstall") int arstall,
            @PathVariable("kvartal") int kvartal) {
        // for (each virksomhet)
        //  hentSykefraværshistorikk()
        return null;
    }
}
