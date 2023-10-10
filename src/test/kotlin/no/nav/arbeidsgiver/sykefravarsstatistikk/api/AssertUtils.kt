package no.nav.arbeidsgiver.sykefravarsstatistikk.api

import org.assertj.core.api.Assertions
import java.math.BigDecimal
import java.math.RoundingMode

object AssertUtils {
    const val SCALE = 6
    @JvmStatic
    fun assertBigDecimalIsEqual(actual: BigDecimal, expected: Float) {
        Assertions.assertThat(actual.setScale(SCALE, RoundingMode.HALF_UP))
            .isEqualTo(BigDecimal.valueOf(expected.toDouble()).setScale(SCALE, RoundingMode.HALF_UP))
    }

    @JvmStatic
    fun assertBigDecimalIsEqual(actual: BigDecimal?, expected: BigDecimal?) {
        if (actual == null || expected == null) {
            Assertions.assertThat(actual).isEqualTo(expected)
            return
        }
        Assertions.assertThat(actual.setScale(SCALE, RoundingMode.HALF_UP))
            .isEqualTo(expected.setScale(SCALE, RoundingMode.HALF_UP))
    }
}
