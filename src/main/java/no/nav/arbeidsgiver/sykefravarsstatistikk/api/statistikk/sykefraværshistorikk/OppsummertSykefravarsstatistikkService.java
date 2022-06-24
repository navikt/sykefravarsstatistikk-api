package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import io.vavr.collection.Tree;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.InnloggetBruker;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Konstanter.SISTE_PUBLISERTE_ÅRSTALL_OG_KVARTAL;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Konstanter.antallKvartalerSomSkalSummeres;

@Service
public class OppsummertSykefravarsstatistikkService {
    private final SykefraværRepository sykefraværprosentRepository;
    private final BransjeEllerNæringService bransjeEllerNæringService;
    private final TilgangskontrollService tilgangskontrollService;
    private final EnhetsregisteretClient enhetsregisteretClient;

    public OppsummertSykefravarsstatistikkService(
            SykefraværRepository sykefraværprosentRepository,
            BransjeEllerNæringService bransjeEllerNæringService,
            TilgangskontrollService tilgangskontrollService, EnhetsregisteretClient enhetsregisteretClient) {
        this.sykefraværprosentRepository = sykefraværprosentRepository;
        this.bransjeEllerNæringService = bransjeEllerNæringService;
        this.tilgangskontrollService = tilgangskontrollService;
        this.enhetsregisteretClient = enhetsregisteretClient;
    }

    public OppsummertSykefraværsstatistikk hentNoeGreier(String orgnr) {
        InnloggetBruker innloggetBruker = tilgangskontrollService.hentInnloggetBrukerForAlleRettigheter();
        tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
                new Orgnr(orgnr),
                innloggetBruker,
                "",
                ""
        );

        Underenhet underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(new Orgnr(orgnr));

        BransjeEllerNæring bransjeEllerNæring =
                bransjeEllerNæringService.getBransjeEllerNæring(underenhet.getNæringskode());
        try {
            InnloggetBruker innloggetBrukerMedIARettigheter =
                    tilgangskontrollService.hentInnloggetBruker();
            tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
                    new Orgnr(orgnr),
                    innloggetBrukerMedIARettigheter,
                    "",
                    ""
            );

        } catch (TilgangskontrollException tilgangskontrollException) {

        }
        return null;
    }

    Optional<GenerellStatistikk> hentGenerellStatistikk(Underenhet underenhet) {
        ÅrstallOgKvartal eldsteÅrstallOgKvartal =
                SISTE_PUBLISERTE_ÅRSTALL_OG_KVARTAL.minusKvartaler(antallKvartalerSomSkalSummeres - 1);

        List<UmaskertSykefraværForEttKvartal> sykefraværForEttKvartalListe =
                sykefraværprosentRepository.hentUmaskertSykefraværForEttKvartalListe(
                        underenhet,
                        eldsteÅrstallOgKvartal
                );

        SummertSykefravær summertSykefravær =
                SummertSykefravær.getSummertSykefravær(sykefraværForEttKvartalListe);

        boolean erMaskert = summertSykefravær.isErMaskert();
        boolean harData = !(summertSykefravær.getKvartaler() == null || summertSykefravær.getKvartaler().isEmpty());

        if (harData && !erMaskert) {
            return Optional.of(new GenerellStatistikk(
                    Statistikkategori.VIRKSOMHET,
                    underenhet.getNavn(),
                    summertSykefravær.getProsent()
            ));
        }
        return Optional.empty();
    }
}
