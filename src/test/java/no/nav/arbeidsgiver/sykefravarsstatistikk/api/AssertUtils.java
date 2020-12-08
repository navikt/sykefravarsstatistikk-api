package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

public class AssertUtils {

    public static void assertBigDecimalIsEqual(BigDecimal actual, float expected) {
        assertThat(actual.setScale(6, RoundingMode.HALF_UP))
                .isEqualTo(BigDecimal.valueOf(expected).setScale(6, RoundingMode.HALF_UP));
    }


}
