package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.utils.StatistikkUtils.kalkulerSykefraværsprosent;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.exceptions.StatistikkException;
import org.junit.jupiter.api.Test;

class StatistikkUtilsTest {

  @Test
  public void kalkulerSykefraværsprosent_girÉnDesimalISvaret() {
    assertThat(kalkulerSykefraværsprosent(BigDecimal.ONE, BigDecimal.ONE).getOrNull())
        .isEqualTo(new BigDecimal("100.0"));

    assertThat(kalkulerSykefraværsprosent(BigDecimal.ONE, new BigDecimal("1.00")).getOrNull())
        .isEqualTo(new BigDecimal("100.0"));

    assertThat(kalkulerSykefraværsprosent(new BigDecimal("1.000"), new BigDecimal("1.0")).getOrNull())
        .isEqualTo(new BigDecimal("100.0"));
  }

  @Test
  public void kalkulerSykefraværsprosent_regnerUtKorrektProsent() {
    assertThat(kalkulerSykefraværsprosent(new BigDecimal("5"), new BigDecimal("10")).getOrNull())
        .isEqualTo(new BigDecimal("50.0"));

    assertThat(kalkulerSykefraværsprosent(BigDecimal.ZERO, new BigDecimal("100.00")).getOrNull())
        .isEqualTo(new BigDecimal("0.0"));

    assertThat(kalkulerSykefraværsprosent(new BigDecimal("0.01"), new BigDecimal("1000")).getOrNull())
        .isEqualTo(new BigDecimal("0.0"));

    assertThat(kalkulerSykefraværsprosent(new BigDecimal("1.01"), new BigDecimal("1.02")).getOrNull())
        .isEqualTo(new BigDecimal("99.0"));
  }

  @Test
  public void kalkulerSykefraværsprosent_returnererStatistikkException_dersomNevnerErZero() {
    assertThat(kalkulerSykefraværsprosent(new BigDecimal("5"), BigDecimal.ZERO).swap().getOrNull())
        .isInstanceOf(StatistikkException.class);

    assertThat(kalkulerSykefraværsprosent(new BigDecimal("5"), new BigDecimal("0.0")).swap().getOrNull())
        .isInstanceOf(StatistikkException.class);
  }

  @Test
  public void kalkulerSykefraværsprosent_runderOppKorrekt() {
    assertThat(kalkulerSykefraværsprosent(new BigDecimal("0.55"), BigDecimal.ONE).getOrNull())
        .isEqualTo(new BigDecimal("55.0"));

    assertThat(kalkulerSykefraværsprosent(new BigDecimal("0.555"), BigDecimal.ONE).getOrNull())
        .isEqualTo(new BigDecimal("55.5"));

    assertThat(kalkulerSykefraværsprosent(new BigDecimal("0.5555"), BigDecimal.ONE).getOrNull())
        .isEqualTo(new BigDecimal("55.6"));

    assertThat(kalkulerSykefraværsprosent(new BigDecimal("0.05"), BigDecimal.ONE).getOrNull())
        .isEqualTo(new BigDecimal("5.0"));

    assertThat(kalkulerSykefraværsprosent(new BigDecimal("0.005"), BigDecimal.ONE).getOrNull())
        .isEqualTo(new BigDecimal("0.5"));

    assertThat(kalkulerSykefraværsprosent(new BigDecimal("0.0005"), BigDecimal.ONE).getOrNull())
        .isEqualTo(new BigDecimal("0.1"));

    assertThat(kalkulerSykefraværsprosent(new BigDecimal("0.00005"), BigDecimal.ONE).getOrNull())
        .isEqualTo(new BigDecimal("0.0"));
  }

  @Test
  public void kalkulerSykefraværsprosent_runderNedKorrekt() {
    assertThat(kalkulerSykefraværsprosent(new BigDecimal("0.44"), BigDecimal.ONE).getOrNull())
        .isEqualTo(new BigDecimal("44.0"));

    assertThat(kalkulerSykefraværsprosent(new BigDecimal("0.444"), BigDecimal.ONE).getOrNull())
        .isEqualTo(new BigDecimal("44.4"));

    assertThat(kalkulerSykefraværsprosent(new BigDecimal("0.4444"), BigDecimal.ONE).getOrNull())
        .isEqualTo(new BigDecimal("44.4"));

    assertThat(kalkulerSykefraværsprosent(new BigDecimal("0.04"), BigDecimal.ONE).getOrNull())
        .isEqualTo(new BigDecimal("4.0"));

    assertThat(kalkulerSykefraværsprosent(new BigDecimal("0.004"), BigDecimal.ONE).getOrNull())
        .isEqualTo(new BigDecimal("0.4"));

    assertThat(kalkulerSykefraværsprosent(new BigDecimal("0.0004"), BigDecimal.ONE).getOrNull())
        .isEqualTo(new BigDecimal("0.0"));
  }
}
