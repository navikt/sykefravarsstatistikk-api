package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import io.vavr.control.Either;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.utils.EitherUtils;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.api.PubliseringsdatoerService;
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
  private final PubliseringsdatoerService publiseringsdatoerService;

  public AggregertStatistikkService(
      SykefraværRepository sykefraværprosentRepository,
      GraderingRepository graderingRepository,
      VarighetRepository varighetRepository,
      BransjeEllerNæringService bransjeEllerNæringService,
      TilgangskontrollService tilgangskontrollService,
      EnhetsregisteretClient enhetsregisteretClient,
      PubliseringsdatoerService publiseringsdatoerService) {
    this.sykefraværprosentRepository = sykefraværprosentRepository;
    this.graderingRepository = graderingRepository;
    this.varighetRepository = varighetRepository;
    this.bransjeEllerNæringService = bransjeEllerNæringService;
    this.tilgangskontrollService = tilgangskontrollService;
    this.enhetsregisteretClient = enhetsregisteretClient;
    this.publiseringsdatoerService = publiseringsdatoerService;
  }

  public Either<TilgangskontrollException, AggregertStatistikkDto> hentAggregertStatistikk(
      Orgnr orgnr) {

    if (!tilgangskontrollService.brukerRepresentererVirksomheten(orgnr)) {
      return Either.left(
          new TilgangskontrollException("Bruker mangler tilgang til denne virksomheten"));
    }
    Underenhet virksomhet = enhetsregisteretClient.hentUnderenhet(orgnr);
    Sykefraværsdata totalSykefravær = hentTotalfraværSisteFemKvartaler(virksomhet);
    Sykefraværsdata gradertSykefravær = hentGradertSykefravær(virksomhet);
    Sykefraværsdata korttidSykefravær = hentKorttidsfravær(virksomhet);
    Sykefraværsdata langtidsfravær = hentLangtidsfravær(virksomhet);

    if (!tilgangskontrollService.brukerHarIaRettigheterIVirksomheten(orgnr)) {
      totalSykefravær.filtrerBortVirksomhetsdata();
      gradertSykefravær.filtrerBortVirksomhetsdata();
      korttidSykefravær.filtrerBortVirksomhetsdata();
      langtidsfravær.filtrerBortVirksomhetsdata();
    }
    return Either.right(
        aggregerData(
            virksomhet, totalSykefravær, gradertSykefravær, korttidSykefravær, langtidsfravær));
  }

  private AggregertStatistikkDto aggregerData(
      Underenhet virksomhet,
      Sykefraværsdata totalfravær,
      Sykefraværsdata gradertFravær,
      Sykefraværsdata korttidsfravær,
      Sykefraværsdata langtidsfravær) {
    ÅrstallOgKvartal sistePubliserteKvartal =
        publiseringsdatoerService.hentSistePubliserteKvartal();

    Aggregeringskalkulator kalkulatorTotal =
        new Aggregeringskalkulator(totalfravær, sistePubliserteKvartal);
    Aggregeringskalkulator kalkulatorGradert =
        new Aggregeringskalkulator(gradertFravær, sistePubliserteKvartal);
    Aggregeringskalkulator kalkulatorKorttid =
        new Aggregeringskalkulator(korttidsfravær, sistePubliserteKvartal);
    Aggregeringskalkulator kalkulatorLangtid =
        new Aggregeringskalkulator(langtidsfravær, sistePubliserteKvartal);

    BransjeEllerNæring bransjeEllerNæring = bransjeEllerNæringService.finnBransje(virksomhet);

    List<StatistikkDto> prosentSisteFireKvartalerTotalt =
        EitherUtils.getRightsAndLogLefts(
            kalkulatorTotal.fraværsprosentVirksomhet(virksomhet.getNavn()),
            kalkulatorTotal.fraværsprosentBransjeEllerNæring(bransjeEllerNæring),
            kalkulatorTotal.fraværsprosentNorge());
    List<StatistikkDto> prosentSisteFireKvartalerGradert =
        EitherUtils.getRightsAndLogLefts(
            kalkulatorGradert.fraværsprosentVirksomhet(virksomhet.getNavn()),
            kalkulatorGradert.fraværsprosentBransjeEllerNæring(bransjeEllerNæring));
    List<StatistikkDto> prosentSisteFireKvartalerKorttid =
        EitherUtils.getRightsAndLogLefts(
            kalkulatorKorttid.fraværsprosentVirksomhet(virksomhet.getNavn()),
            kalkulatorKorttid.fraværsprosentBransjeEllerNæring(bransjeEllerNæring));
    List<StatistikkDto> prosentSisteFireKvartalerLangtid =
        EitherUtils.getRightsAndLogLefts(
            kalkulatorLangtid.fraværsprosentVirksomhet(virksomhet.getNavn()),
            kalkulatorLangtid.fraværsprosentBransjeEllerNæring(bransjeEllerNæring));

    List<StatistikkDto> trendTotalt =
        EitherUtils.getRightsAndLogLefts(
            kalkulatorTotal.trendBransjeEllerNæring(bransjeEllerNæring));

    List<StatistikkDto> tapteDagsverkTotalt =
        EitherUtils.getRightsAndLogLefts(
            kalkulatorTotal.tapteDagsverkVirksomhet(virksomhet.getNavn()));

    List<StatistikkDto> muligeDagsverkTotalt =
        EitherUtils.getRightsAndLogLefts(
            kalkulatorTotal.muligeDagsverkVirksomhet(virksomhet.getNavn()));

    return new AggregertStatistikkDto(
        prosentSisteFireKvartalerTotalt,
        prosentSisteFireKvartalerGradert,
        prosentSisteFireKvartalerKorttid,
        prosentSisteFireKvartalerLangtid,
        trendTotalt,
        tapteDagsverkTotalt,
        muligeDagsverkTotalt);
  }

  private Sykefraværsdata hentTotalfraværSisteFemKvartaler(Underenhet forBedrift) {
    return sykefraværprosentRepository.hentUmaskertSykefraværAlleKategorier(
        forBedrift, publiseringsdatoerService.hentSistePubliserteKvartal().minusKvartaler(4));
  }

  private Sykefraværsdata hentGradertSykefravær(Underenhet virksomhet) {
    return graderingRepository.hentGradertSykefraværAlleKategorier(virksomhet);
  }

  private Sykefraværsdata hentKorttidsfravær(Underenhet virksomhet) {
    return hentLangtidsEllerKorttidsfravær(
        virksomhet,
        datapunkt ->
            datapunkt.getVarighet().erKorttidVarighet()
                || datapunkt.getVarighet().erTotalvarighet());
  }

  private Sykefraværsdata hentLangtidsfravær(Underenhet virksomhet) {
    return hentLangtidsEllerKorttidsfravær(
        virksomhet,
        datapunkt ->
            datapunkt.getVarighet().erLangtidVarighet()
                || datapunkt.getVarighet().erTotalvarighet());
  }

  private Sykefraværsdata hentLangtidsEllerKorttidsfravær(
      Underenhet virksomhet,
      Predicate<UmaskertSykefraværForEttKvartalMedVarighet> entenLangtidEllerKorttid) {

    Map<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> filtrertFravær = new HashMap<>();

    varighetRepository
        .hentUmaskertSykefraværMedVarighetAlleKategorier(virksomhet)
        .forEach(
            (statistikkategori, fravær) ->
                filtrertFravær.put(
                    statistikkategori,
                    fravær.stream()
                        .filter(entenLangtidEllerKorttid)
                        .collect(
                            Collectors.groupingBy(
                                UmaskertSykefraværForEttKvartal::getÅrstallOgKvartal,
                                Collectors.reducing(UmaskertSykefraværForEttKvartal::add)))
                        .values()
                        .stream()
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList())));

    return new Sykefraværsdata(filtrertFravær);
  }
}
