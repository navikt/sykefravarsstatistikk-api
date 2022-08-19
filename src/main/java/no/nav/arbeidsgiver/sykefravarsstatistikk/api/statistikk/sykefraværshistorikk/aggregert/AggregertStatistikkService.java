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
        Sykefraværsdata totalSykefravær = hentTotalfraværSisteFemKvartaler(virksomhet);
        Sykefraværsdata gradertSykefravær = hentGradertSykefravær(virksomhet);
        Sykefraværsdata korttidSykefravær = hentKorttidsfravær(virksomhet);
        Sykefraværsdata langtidsfravær = hentLangtidsfravær(virksomhet);

        if (!tilgangskontrollService.brukerHarIaRettigheter(orgnr)) {
            totalSykefravær.filtrerBortVirksomhetsdata();
        }
        return Either.right(aggregerData(
              virksomhet,
              totalSykefravær,
              gradertSykefravær,
              korttidSykefravær,
              langtidsfravær
        ));
    }


    private AggregertStatistikkDto aggregerData(
          Underenhet virksomhet,
          Sykefraværsdata totalfravær,
          Sykefraværsdata gradertFravær,
          Sykefraværsdata korttidsfravær,
          Sykefraværsdata langtidsfravær
    ) {
        Aggregeringskalkulator kalkulatorTotal = new Aggregeringskalkulator(totalfravær);
        Aggregeringskalkulator kalkulatorGradert = new Aggregeringskalkulator(gradertFravær);
        Aggregeringskalkulator kalkulatorKorttid = new Aggregeringskalkulator(korttidsfravær);
        Aggregeringskalkulator kalkulatorLangtid = new Aggregeringskalkulator(langtidsfravær);

        BransjeEllerNæring bransjeEllerNæring = bransjeEllerNæringService.finnBransje(virksomhet);

        // TODO: Jeg er identisk med prosentSisteFireKvartalerTotalt.
        //  Fjern meg når Forebygge fravær har tatt prosentSisteFireKvartalerTotalt i bruk.
        List<StatistikkDto> prosentSisteFireKvartaler = EitherUtils.filterRights(
              kalkulatorTotal.fraværsprosentVirksomhet(virksomhet.getNavn()),
              kalkulatorTotal.fraværsprosentBransjeEllerNæring(bransjeEllerNæring),
              kalkulatorTotal.fraværsprosentNorge()
        );
        List<StatistikkDto> prosentSisteFireKvartalerTotalt = EitherUtils.filterRights(
              kalkulatorTotal.fraværsprosentVirksomhet(virksomhet.getNavn()),
              kalkulatorTotal.fraværsprosentBransjeEllerNæring(bransjeEllerNæring),
              kalkulatorTotal.fraværsprosentNorge()
        );
        List<StatistikkDto> prosentSisteFireKvartalerGradert = EitherUtils.filterRights(
              kalkulatorGradert.fraværsprosentVirksomhet(virksomhet.getNavn()),
              kalkulatorGradert.fraværsprosentBransjeEllerNæring(bransjeEllerNæring)
        );
        List<StatistikkDto> prosentSisteFireKvartalerKorttid = EitherUtils.filterRights(
              kalkulatorKorttid.fraværsprosentVirksomhet(virksomhet.getNavn()),
              kalkulatorKorttid.fraværsprosentBransjeEllerNæring(bransjeEllerNæring)
        );
        List<StatistikkDto> prosentSisteFireKvartalerLangtid = EitherUtils.filterRights(
              kalkulatorLangtid.fraværsprosentVirksomhet(virksomhet.getNavn()),
              kalkulatorLangtid.fraværsprosentBransjeEllerNæring(bransjeEllerNæring)
        );

        // TODO: Jeg er identisk med trendTotalt.
        //  Fjern meg når Forebygge fravær har tatt trendTotalt i bruk.
        List<StatistikkDto> trend = EitherUtils.filterRights(
              kalkulatorTotal.trendBransjeEllerNæring(bransjeEllerNæring)
        );

        List<StatistikkDto> trendTotalt = EitherUtils.filterRights(
              kalkulatorTotal.trendBransjeEllerNæring(bransjeEllerNæring)
        );
        return new AggregertStatistikkDto(
              prosentSisteFireKvartaler,
              prosentSisteFireKvartalerTotalt,
              prosentSisteFireKvartalerGradert,
              prosentSisteFireKvartalerKorttid,
              prosentSisteFireKvartalerLangtid,
              trend,
              trendTotalt);
    }


    private Sykefraværsdata hentTotalfraværSisteFemKvartaler(Underenhet forBedrift) {
        return sykefraværprosentRepository
              .hentUmaskertSykefraværAlleKategorier(
                    forBedrift, SISTE_PUBLISERTE_KVARTAL.minusKvartaler(4)
              );
    }


    private Sykefraværsdata hentGradertSykefravær(Underenhet virksomhet) {
        return graderingRepository.hentGradertSykefraværAlleKategorier(virksomhet);
    }


    private Sykefraværsdata hentKorttidsfravær(Underenhet virksomhet) {
        return hentLangtidsEllerKorttidsfravær(
              virksomhet, datapunkt -> datapunkt.getVarighet().erKorttidVarighet());
    }


    private Sykefraværsdata hentLangtidsfravær(Underenhet virksomhet) {
        return hentLangtidsEllerKorttidsfravær(
              virksomhet, datapunkt -> datapunkt.getVarighet().erLangtidVarighet());
    }


    private Sykefraværsdata hentLangtidsEllerKorttidsfravær(
          Underenhet virksomhet,
          Predicate<UmaskertSykefraværForEttKvartalMedVarighet> entenLangtidEllerKorttid) {

        Map<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> filtrertFravær
              = new HashMap<>();

        varighetRepository.hentUmaskertSykefraværMedVarighetAlleKategorier(virksomhet)
              .forEach((statistikkategori, fravær) ->
                    filtrertFravær.put(statistikkategori, fravær.stream()
                          .filter(entenLangtidEllerKorttid)
                          .map(UmaskertSykefraværForEttKvartalMedVarighet::tilUmaskertSykefraværForEttKvartal)
                          .collect(Collectors.toList())));

        return new Sykefraværsdata(filtrertFravær);
    }
}
