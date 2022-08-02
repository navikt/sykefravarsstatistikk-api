package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk;

import java.math.BigDecimal;
import java.math.RoundingMode;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.Aggregeringstype;
import org.jetbrains.annotations.NotNull;

public class StatistikkUtils {

    /**
     * Men å være en felles source of trouth for kalkulering av sykefraværsprosent. Denne metoden
     * bør altså ikke dupliseres, men refaktorer gjerne eksisterende metoder slik at de også bruker
     * denne.
     */
    public static BigDecimal kalkulerSykefraværsprosent(
          @NotNull BigDecimal tapteDagsverk,
          @NotNull BigDecimal muligeDagsverk) {
        final int DEFAULT_DECIMAL_PLACES = 2;
        return tapteDagsverk.divide(muligeDagsverk, DEFAULT_DECIMAL_PLACES, RoundingMode.HALF_UP)
              .multiply(new BigDecimal(100));
    }

    public static Aggregeringstype getProsenttypeFor(BransjeEllerNæring bransjeEllerNæring) {
        return bransjeEllerNæring.isBransje()
              ? Aggregeringstype.PROSENT_SISTE_4_KVARTALER_BRANSJE
              : Aggregeringstype.PROSENT_SISTE_4_KVARTALER_NÆRING;
    }

    public static Aggregeringstype getTrendtypeFor(BransjeEllerNæring bransjeEllerNæring) {
        return bransjeEllerNæring.isBransje()
              ? Aggregeringstype.TREND_BRANSJE
              : Aggregeringstype.TREND_NÆRING;
    }
}
