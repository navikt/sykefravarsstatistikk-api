package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.jetbrains.annotations.NotNull;

public class StatistikkUtils {

    /**
     * Source of trouth for kalkulering av sykefraværsprosent basert på tapte dagsverk og mulige
     * dagsverk. ArithmeticException kastes dersom muligeDagsverk er 0.
     */
    public static BigDecimal kalkulerSykefraværsprosent(
          @NotNull BigDecimal tapteDagsverk, @NotNull BigDecimal muligeDagsverk) {
        final int ANTALL_SIFRE_I_UTREGNING = 3;
        final int ANTALL_SIFRE_I_RESULTAT = 1;
        return tapteDagsverk.divide(
                    muligeDagsverk, ANTALL_SIFRE_I_UTREGNING, RoundingMode.HALF_UP)
              .multiply(new BigDecimal(100))
              .setScale(ANTALL_SIFRE_I_RESULTAT, RoundingMode.HALF_UP);
    }
}
