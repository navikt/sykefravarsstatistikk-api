package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import io.vavr.control.Either;
import lombok.AllArgsConstructor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.StatistikkException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UtilstrekkeligDataException;

import java.util.List;
import java.util.stream.Collectors;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal.SISTE_PUBLISERTE_KVARTAL;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal.sisteFireKvartaler;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.LAND;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.VIRKSOMHET;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.SumAvSykefraværOverFlereKvartaler.NULLPUNKT;

@AllArgsConstructor
public class Aggregeringskalkulator {

    public Aggregeringskalkulator(
          Sykefraværsdata sykefraværsdata
    ) {
        this.sykefraværsdata = sykefraværsdata;
    }
    public Sykefraværsdata sykefraværsdata;
    public ÅrstallOgKvartal årstallOgKvartal=SISTE_PUBLISERTE_KVARTAL;

    public Either<StatistikkException, StatistikkDto> fraværsprosentNorge() {
        return summerOppSisteFireKvartaler(sykefraværsdata.filtrerPåKategori(LAND))
              .regnUtProsentOgMapTilDto(LAND, "Norge");
    }


    public Either<StatistikkException, StatistikkDto> fraværsprosentBransjeEllerNæring(
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


   public Either<StatistikkException, StatistikkDto> fraværsprosentVirksomhet(String virksomhetsnavn) {
        return summerOppSisteFireKvartaler(sykefraværsdata.filtrerPåKategori(VIRKSOMHET))
              .regnUtProsentOgMapTilDto(VIRKSOMHET, virksomhetsnavn);
    }


    Either<UtilstrekkeligDataException, StatistikkDto> trendBransjeEllerNæring(
          BransjeEllerNæring bransjeEllerNæring) {
        Either<UtilstrekkeligDataException, Trend> maybeTrend =
              new Trendkalkulator(
                    sykefraværsdata.filtrerPåKategori(
                          bransjeEllerNæring.getStatistikkategori()))
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
                .filter(datapunkt -> sisteFireKvartaler(this.årstallOgKvartal).contains(datapunkt.getÅrstallOgKvartal()))
                .collect(Collectors.toList());
    }
}
