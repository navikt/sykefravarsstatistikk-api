package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

public class AssertUtils {

  public static final int SCALE = 6;

  public static void assertBigDecimalIsEqual(BigDecimal actual, float expected) {
    assertThat(actual.setScale(SCALE, RoundingMode.HALF_UP))
        .isEqualTo(BigDecimal.valueOf(expected).setScale(SCALE, RoundingMode.HALF_UP));
  }

  public static void assertBigDecimalIsEqual(BigDecimal actual, BigDecimal expected) {
    if (actual == null || expected == null) {
      assertThat(actual).isEqualTo(expected);
      return;
    }

    assertThat(actual.setScale(SCALE, RoundingMode.HALF_UP))
        .isEqualTo(expected.setScale(SCALE, RoundingMode.HALF_UP));
  }
}
