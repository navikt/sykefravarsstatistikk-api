package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class StatistikkUtils {

    /**
     * Felles source of trouth for kalkulering av sykefraværsprosent.
     */
 public static BigDecimal kalkulerSykefraværsprosent(BigDecimal tapteDagsverk, BigDecimal muligeDagsverk) {
     final int defaultDecimalPlaces = 2;
     return tapteDagsverk.divide(muligeDagsverk, defaultDecimalPlaces, RoundingMode.HALF_UP);
 }
}
