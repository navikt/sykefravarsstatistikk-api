package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.SISTE_PUBLISERTE_KVARTAL;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregering.Trendkalkulator;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Trend;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.exceptions.UtilstrekkeligDataException;
import org.junit.jupiter.api.Test;

class TrendkalkulatorTest {

  @Test
  void kalkulerTrend_returnererManglendeDataException_nårEtKvartalMangler() {
    assertThat(
            new Trendkalkulator(
                    List.of(
                        umaskertSykefravær(new ÅrstallOgKvartal(2021, 2), 11, 1),
                        umaskertSykefravær(new ÅrstallOgKvartal(2021, 1), 10, 3)),
                    SISTE_PUBLISERTE_KVARTAL)
                .kalkulerTrend()
                .getLeft())
        .isExactlyInstanceOf(UtilstrekkeligDataException.class);
  }

  @Test
  void kalkulerTrend_returnererPositivTrend_dersomSykefraværetØker() {
    ÅrstallOgKvartal k1 = SISTE_PUBLISERTE_KVARTAL;
    ÅrstallOgKvartal k2 = SISTE_PUBLISERTE_KVARTAL.minusEttÅr();
    assertThat(
            new Trendkalkulator(
                    List.of(umaskertSykefravær(k1, 3, 10), umaskertSykefravær(k2, 2, 10)),
                    SISTE_PUBLISERTE_KVARTAL)
                .kalkulerTrend()
                .get())
        .isEqualTo(new Trend(new BigDecimal("1.0"), 20, List.of(k1, k2)));
  }

  @Test
  void kalkulerTrend_returnereNegativTrend_dersomSykefraværetMinker() {
    List<UmaskertSykefraværForEttKvartal> kvartalstall =
        List.of(
            umaskertSykefravær(SISTE_PUBLISERTE_KVARTAL, 8, 1),
            umaskertSykefravær(new ÅrstallOgKvartal(2020, 2), 13, 2),
            umaskertSykefravær(SISTE_PUBLISERTE_KVARTAL.minusEttÅr(), 10, 3));
    Trend forventetTrend =
        new Trend(
            new BigDecimal("-2.0"),
            4,
            List.of(SISTE_PUBLISERTE_KVARTAL, SISTE_PUBLISERTE_KVARTAL.minusEttÅr()));

    assertThat(new Trendkalkulator(kvartalstall, SISTE_PUBLISERTE_KVARTAL).kalkulerTrend().get())
        .isEqualTo(forventetTrend);
  }

  @Test
  void kalkulerTrend_girUtrilstrekkeligDataException_vedTomtDatagrunnlag() {
    assertThat(new Trendkalkulator(List.of(), SISTE_PUBLISERTE_KVARTAL).kalkulerTrend().getLeft())
        .isExactlyInstanceOf(UtilstrekkeligDataException.class);
  }

  private static UmaskertSykefraværForEttKvartal umaskertSykefravær(
      ÅrstallOgKvartal årstallOgKvartal, double tapteDagsverk, int antallPersoner) {
    return new UmaskertSykefraværForEttKvartal(
        årstallOgKvartal, new BigDecimal(tapteDagsverk), new BigDecimal(100), antallPersoner);
  }
}
