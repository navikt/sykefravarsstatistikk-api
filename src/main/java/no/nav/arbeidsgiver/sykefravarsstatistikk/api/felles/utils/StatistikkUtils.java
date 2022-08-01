package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.Statistikktype;

public class StatistikkUtils {

    /**
     * Men å være en felles source of trouth for kalkulering av sykefraværsprosent. Denne metoden
     * bør altså ikke dupliseres. // TODO: Ta meg i bruuk
     */
    public static BigDecimal kalkulerSykefraværsprosent(BigDecimal tapteDagsverk,
          BigDecimal muligeDagsverk) {
        final int defaultDecimalPlaces = 2;
        return tapteDagsverk.divide(muligeDagsverk, defaultDecimalPlaces, RoundingMode.HALF_UP);
    }

    public static Statistikktype getProsenttypeFor(BransjeEllerNæring bransjeEllerNæring) {
        return bransjeEllerNæring.isBransje()
              ? Statistikktype.PROSENT_SISTE_4_KVARTALER_BRANSJE
              : Statistikktype.PROSENT_SISTE_4_KVARTALER_NÆRING;
    }

    public static Statistikktype getTrendtypeFor(BransjeEllerNæring bransjeEllerNæring) {
        return bransjeEllerNæring.isBransje()
              ? Statistikktype.TREND_BRANSJE
              : Statistikktype.TREND_NÆRING;
    }
}
