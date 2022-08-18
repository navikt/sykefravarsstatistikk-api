package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal.SISTE_PUBLISERTE_KVARTAL;

import io.vavr.control.Either;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.utils.EitherUtils;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartalMedVarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.GraderingRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.VarighetRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import org.springframework.stereotype.Service;

@Service
public class AggregertStatistikkService {

    private final SykefraværRepository sykefraværprosentRepository;
    private final GraderingRepository graderingRepository;
    private final VarighetRepository varighetRepository;
    private final BransjeEllerNæringService bransjeEllerNæringService;
    private final TilgangskontrollService tilgangskontrollService;
    private final EnhetsregisteretClient enhetsregisteretClient;


    public AggregertStatistikkService(
            SykefraværRepository sykefraværprosentRepository,
            GraderingRepository graderingRepository,
            VarighetRepository varighetRepository,
            BransjeEllerNæringService bransjeEllerNæringService,
            TilgangskontrollService tilgangskontrollService,
            EnhetsregisteretClient enhetsregisteretClient) {
        this.sykefraværprosentRepository = sykefraværprosentRepository;
        this.graderingRepository = graderingRepository;
        this.varighetRepository = varighetRepository;
        this.bransjeEllerNæringService = bransjeEllerNæringService;
        this.tilgangskontrollService = tilgangskontrollService;
        this.enhetsregisteretClient = enhetsregisteretClient;
    }


    public Either<TilgangskontrollException, AggregertStatistikkDto> hentAggregertStatistikk(
            Orgnr orgnr) {

        if (!tilgangskontrollService.brukerRepresentererVirksomheten(orgnr)) {
            return Either.left(
                    new TilgangskontrollException("Bruker mangler tilgang til denne virksomheten"));
        }
        Underenhet virksomhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(orgnr);
        Sykefraværsdata totalSykefravær = hentTotalsykefraværForSisteFemKvartaler(virksomhet);
        Sykefraværsdata gradertSykefravær = hentGradertSykefravær(virksomhet);
        Sykefraværsdata korttidSykefravær = hentSykefraværForEttKvartalMedVarighet(virksomhet);

        if (!tilgangskontrollService.brukerHarIaRettigheter(orgnr)) {
            totalSykefravær.filtrerBortVirksomhetsdata();
        }
        return Either.right(aggregerData(virksomhet, totalSykefravær, gradertSykefravær));
    }


    private AggregertStatistikkDto aggregerData(Underenhet virksomhet,
            Sykefraværsdata totalsykefravær, Sykefraværsdata gradertSykefravær) {

        Aggregeringskalkulator kalkulatorTotal = new Aggregeringskalkulator(totalsykefravær);
        Aggregeringskalkulator kalkulatorGradert = new Aggregeringskalkulator(gradertSykefravær);

        BransjeEllerNæring bransjeEllerNæring = bransjeEllerNæringService.finnBransje(virksomhet);

        List<StatistikkDto> prosentSisteFireKvartaler = EitherUtils.filterRights(
                kalkulatorTotal.fraværsprosentVirksomhet(virksomhet.getNavn()),
                kalkulatorTotal.fraværsprosentBransjeEllerNæring(bransjeEllerNæring),
                kalkulatorTotal.fraværsprosentNorge()
        );
        List<StatistikkDto> gradertProsentSisteFireKvartaler = EitherUtils.filterRights(
                kalkulatorGradert.fraværsprosentVirksomhet(virksomhet.getNavn()),
                kalkulatorGradert.fraværsprosentBransjeEllerNæring(bransjeEllerNæring)
        );
        List<StatistikkDto> trend = EitherUtils.filterRights(
                kalkulatorTotal.trendBransjeEllerNæring(bransjeEllerNæring)
        );
        return new AggregertStatistikkDto(
                prosentSisteFireKvartaler,
                gradertProsentSisteFireKvartaler,
                trend);
    }


    private Sykefraværsdata hentTotalsykefraværForSisteFemKvartaler(Underenhet forBedrift) {
        return sykefraværprosentRepository
                .hentUmaskertSykefraværAlleKategorier(
                        forBedrift, SISTE_PUBLISERTE_KVARTAL.minusKvartaler(4)
                );
    }


    private Sykefraværsdata hentKorttidSykefraværMedVarighet(Underenhet virksomhet) {
        return hentLangtidsEllerKorttidsfravær(
                virksomhet, datapunkt -> datapunkt.getVarighet().erKorttidVarighet());
    }


    private Sykefraværsdata hentLangtidsSykefraværMedVarighet(Underenhet virksomhet) {
        return hentLangtidsEllerKorttidsfravær(
                virksomhet, datapunkt -> datapunkt.getVarighet().erLangtidVarighet());
    }


    private Sykefraværsdata hentLangtidsEllerKorttidsfravær(Underenhet virksomhet,
            Predicate<UmaskertSykefraværForEttKvartalMedVarighet> langtidEllerKorttid) {

        Map<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> fravær = new HashMap<>();

        varighetRepository.hentUmaskertSykefraværMedVarighetAlleKategorier(virksomhet)
                .forEach((k, v) ->
                        fravær.put(k, v.stream()
                                .filter(langtidEllerKorttid)
                                .map(UmaskertSykefraværForEttKvartalMedVarighet::tilUmaskertSykefraværForEttKvartal)
                                .collect(Collectors.toList())));

        return new Sykefraværsdata(fravær);
    }
}
