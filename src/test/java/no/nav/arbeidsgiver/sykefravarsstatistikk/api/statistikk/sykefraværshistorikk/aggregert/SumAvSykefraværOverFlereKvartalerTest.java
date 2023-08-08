package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.VIRKSOMHET;

import io.vavr.control.Either;
import java.math.BigDecimal;
import java.util.List;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.MaskerteDataException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.StatistikkException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværOverFlereKvartaler;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class SumAvSykefraværOverFlereKvartalerTest {

  @Test
  void leggSammenToKvartaler() {

    UmaskertSykefraværForEttKvartal umaskertSykefravær_Q1 =
        new UmaskertSykefraværForEttKvartal(
            new ÅrstallOgKvartal(2022, 1), new BigDecimal(100), new BigDecimal(200), 5);
    UmaskertSykefraværForEttKvartal umaskertSykefravær_Q2 =
        new UmaskertSykefraværForEttKvartal(
            new ÅrstallOgKvartal(2022, 2), new BigDecimal(10), new BigDecimal(230), 5);

    SykefraværOverFlereKvartaler expected =
        new SykefraværOverFlereKvartaler(
            List.of(
                umaskertSykefravær_Q1.getÅrstallOgKvartal(),
                umaskertSykefravær_Q2.getÅrstallOgKvartal()),
            new BigDecimal(110),
            new BigDecimal(430),
            List.of(
                new SykefraværForEttKvartal(
                    umaskertSykefravær_Q1.getÅrstallOgKvartal(),
                    umaskertSykefravær_Q1.getDagsverkTeller(),
                    umaskertSykefravær_Q1.getDagsverkNevner(),
                    umaskertSykefravær_Q1.getAntallPersoner()),
                new SykefraværForEttKvartal(
                    umaskertSykefravær_Q2.getÅrstallOgKvartal(),
                    umaskertSykefravær_Q2.getDagsverkTeller(),
                    umaskertSykefravær_Q2.getDagsverkNevner(),
                    umaskertSykefravær_Q2.getAntallPersoner())));

    SumAvSykefraværOverFlereKvartaler kvartal1 =
        new SumAvSykefraværOverFlereKvartaler(umaskertSykefravær_Q1);
    SumAvSykefraværOverFlereKvartaler kvartal2 =
        new SumAvSykefraværOverFlereKvartaler(umaskertSykefravær_Q2);

    SumAvSykefraværOverFlereKvartaler result = kvartal1.leggSammen(kvartal2);
    SykefraværOverFlereKvartaler resultSykefraværOverFlereKvartaler =
        result.regnUtProsentOgMapTilSykefraværForFlereKvartaler().get();

    Assertions.assertThat(result.muligeDagsverk).isEqualByComparingTo(new BigDecimal(430));
    Assertions.assertThat(result.tapteDagsverk).isEqualByComparingTo(new BigDecimal(110));
    Assertions.assertThat(resultSykefraværOverFlereKvartaler.getProsent())
        .isEqualByComparingTo(expected.getProsent());
    Assertions.assertThat(resultSykefraværOverFlereKvartaler.getKvartaler())
        .isEqualTo(expected.getKvartaler());
  }

  @Test
  void kalkulerFraværsprosentMedMaskering_maskererDataHvisAntallTilfellerErUnderFem() {
    Either<StatistikkException, StatistikkDto> maskertSykefravær =
        new SumAvSykefraværOverFlereKvartaler(
                new UmaskertSykefraværForEttKvartal(
                    new ÅrstallOgKvartal(2022, 1), new BigDecimal(100), new BigDecimal(200), 4))
            .regnUtProsentOgMapTilDto(VIRKSOMHET, "");

    Assertions.assertThat(maskertSykefravær.getLeft())
        .isExactlyInstanceOf(MaskerteDataException.class);
  }

  @Test
  void kalkulerFraværsprosentMedMaskering_returnerProsentHvisAntallTilfellerErFemEllerMer() {
    Either<StatistikkException, StatistikkDto> sykefraværFemTilfeller =
        new SumAvSykefraværOverFlereKvartaler(
                new UmaskertSykefraværForEttKvartal(
                    new ÅrstallOgKvartal(2022, 1), new BigDecimal(100), new BigDecimal(200), 5))
            .regnUtProsentOgMapTilDto(VIRKSOMHET, "");

    Either<StatistikkException, StatistikkDto> sykefraværTiTilfeller =
        new SumAvSykefraværOverFlereKvartaler(
                new UmaskertSykefraværForEttKvartal(
                    new ÅrstallOgKvartal(2022, 1), new BigDecimal(100), new BigDecimal(200), 10))
            .regnUtProsentOgMapTilDto(VIRKSOMHET, "");

    Assertions.assertThat(sykefraværFemTilfeller.get().getVerdi()).isEqualTo("50.0");
    Assertions.assertThat(sykefraværTiTilfeller.get().getVerdi()).isEqualTo("50.0");
  }
}
