package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.oppsummert;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal.SISTE_PUBLISERTE_KVARTAL;

import io.vavr.control.Either;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import org.springframework.stereotype.Service;

@Service
public class OppsummertSykefravarsstatistikkService {

    private final SykefraværRepository sykefraværprosentRepository;
    private final BransjeEllerNæringService bransjeEllerNæringService;
    private final TilgangskontrollService tilgangskontrollService;
    private final EnhetsregisteretClient enhetsregisteretClient;

    public OppsummertSykefravarsstatistikkService(
            SykefraværRepository sykefraværprosentRepository,
            BransjeEllerNæringService bransjeEllerNæringService,
            TilgangskontrollService tilgangskontrollService,
            EnhetsregisteretClient enhetsregisteretClient) {
        this.sykefraværprosentRepository = sykefraværprosentRepository;
        this.bransjeEllerNæringService = bransjeEllerNæringService;
        this.tilgangskontrollService = tilgangskontrollService;
        this.enhetsregisteretClient = enhetsregisteretClient;
    }

    public Either<TilgangskontrollException, List<OppsummertStatistikkDto>> hentOppsummertStatistikk(
            Orgnr orgnr) {

        if (!tilgangskontrollService.brukerRepresentererVirksomheten(orgnr)) {
            return Either.left(
                    new TilgangskontrollException("Bruker mangler tilgang til denne virksomheten"));
        }

        Underenhet virksomhet = enhetsregisteretClient.hentUnderenhet(orgnr);
        if (!tilgangskontrollService.brukerHarIaRettigheter(orgnr)) {
            // return hentStatistikkUtenTallFraVirksomheten(virksomhet);
        }

        return Either.right(hentOgBearbeidStatistikk(virksomhet));
    }

    List<OppsummertStatistikkDto> hentOgBearbeidStatistikk(Underenhet virksomhet) {
        Map<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> sykefraværsdata =
                hentForSisteFemKvartaler(virksomhet);

        BransjeEllerNæring bransjeEllerNæring =
                bransjeEllerNæringService.skalHenteDataPåBransjeEllerNæringsnivå(
                        virksomhet.getNæringskode());

        Prosentkalkulator kalkulator = new Prosentkalkulator(sykefraværsdata);

        return Stream.of(
                        kalkulator.sykefraværVirksomhet(virksomhet.getNavn()),
                        kalkulator.fraværsprosentBransjeEllerNæring(bransjeEllerNæring),
                        kalkulator.fraværsprosentNorge(),
                        kalkulator.trendBransjeEllerNæring(bransjeEllerNæring))
                .filter(Either::isRight)
                .map(Either::get)
                .collect(Collectors.toList());
    }


    private Map<Statistikkategori, List<UmaskertSykefraværForEttKvartal>>
    hentForSisteFemKvartaler(Underenhet forBedrift) {
        ÅrstallOgKvartal fraKvartal = SISTE_PUBLISERTE_KVARTAL.minusKvartaler(4);
        return sykefraværprosentRepository
                .hentUmaskertSykefraværAlleKategorier(forBedrift, fraKvartal);
    }
}
