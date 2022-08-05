package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.jetbrains.annotations.NotNull;

public class StatistikkUtils {

    /**
     * Ment å være en felles source of trouth for kalkulering av sykefraværsprosent basert på tapte
     * dagsverk og mulige dagsverk. Denne metoden bør altså ikke dupliseres, men refaktorer gjerne
     * eksisterende metoder slik at de også bruker denne.
     */
    public static BigDecimal kalkulerSykefraværsprosent(
          @NotNull BigDecimal tapteDagsverk,
          @NotNull BigDecimal muligeDagsverk) {
        final int GJELDENDE_SIFFER = 2;
        return tapteDagsverk.divide(muligeDagsverk, GJELDENDE_SIFFER, RoundingMode.HALF_UP)
              .multiply(new BigDecimal(100));
    }
}
