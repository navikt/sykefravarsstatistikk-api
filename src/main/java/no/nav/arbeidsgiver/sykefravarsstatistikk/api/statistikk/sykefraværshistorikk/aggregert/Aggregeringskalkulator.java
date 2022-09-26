package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.LAND;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.VIRKSOMHET;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.SumAvSykefraværOverFlereKvartaler.NULLPUNKT;

import io.vavr.control.Either;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.StatistikkException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UtilstrekkeligDataException;

@AllArgsConstructor
public class Aggregeringskalkulator {

  private Sykefraværsdata sykefraværsdata;
  private ÅrstallOgKvartal sistePubliserteKvartal;


  Either<StatistikkException, StatistikkDto> fraværsprosentNorge() {
    return summerOppSisteFireKvartaler(sykefraværsdata.filtrerPåKategori(LAND))
        .regnUtProsentOgMapTilDto(LAND, "Norge");
  }


  Either<StatistikkException, StatistikkDto> fraværsprosentBransjeEllerNæring(
      BransjeEllerNæring bransjeEllerNæring) {
    return summerOppSisteFireKvartaler(
        sykefraværsdata.filtrerPåKategori(bransjeEllerNæring.getStatistikkategori()))
        .regnUtProsentOgMapTilDto(
            bransjeEllerNæring.getStatistikkategori(),
            bransjeEllerNæring.navn()
        );
  }


  Either<StatistikkException, StatistikkDto> tapteDagsverkVirksomhet(String bedriftsnavn) {
    return summerOppSisteFireKvartaler(sykefraværsdata.filtrerPåKategori(VIRKSOMHET))
        .getTapteDagsverkOgMapTilDto(VIRKSOMHET, bedriftsnavn);
  }


  Either<StatistikkException, StatistikkDto> muligeDagsverkVirksomhet(String bedriftsnavn) {
    return summerOppSisteFireKvartaler(sykefraværsdata.filtrerPåKategori(VIRKSOMHET))
        .getMuligeDagsverkOgMapTilDto(VIRKSOMHET, bedriftsnavn);

  }


  Either<StatistikkException, StatistikkDto> fraværsprosentVirksomhet(String virksomhetsnavn) {
    return summerOppSisteFireKvartaler(sykefraværsdata.filtrerPåKategori(VIRKSOMHET))
        .regnUtProsentOgMapTilDto(VIRKSOMHET, virksomhetsnavn);
  }


  Either<UtilstrekkeligDataException, StatistikkDto> trendBransjeEllerNæring(
      BransjeEllerNæring bransjeEllerNæring) {
    Either<UtilstrekkeligDataException, Trend> maybeTrend =
        new Trendkalkulator(
            sykefraværsdata.filtrerPåKategori(
                bransjeEllerNæring.getStatistikkategori()), sistePubliserteKvartal)
            .kalkulerTrend();

    return maybeTrend.map(r -> r.tilAggregertHistorikkDto(
        bransjeEllerNæring.getStatistikkategori(),
        bransjeEllerNæring.navn())
    );
  }


  private SumAvSykefraværOverFlereKvartaler summerOppSisteFireKvartaler(
      List<UmaskertSykefraværForEttKvartal> statistikk) {

    return ekstraherSisteFireKvartaler(statistikk).stream()
        .map(SumAvSykefraværOverFlereKvartaler::new)
        .reduce(NULLPUNKT, SumAvSykefraværOverFlereKvartaler::leggSammen);
  }


  private List<UmaskertSykefraværForEttKvartal> ekstraherSisteFireKvartaler(
      List<UmaskertSykefraværForEttKvartal> statistikk) {
    if (statistikk == null) {
      return List.of();
    }
    return statistikk.stream()
        .filter(datapunkt -> sisteFireKvartaler().contains(datapunkt.getÅrstallOgKvartal()))
        .collect(Collectors.toList());
  }


  private List<ÅrstallOgKvartal> sisteFireKvartaler() {
    return IntStream.range(0, 4)
        .mapToObj(sistePubliserteKvartal::minusKvartaler)
        .collect(Collectors.toList());
  }
}
