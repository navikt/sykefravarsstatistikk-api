package no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning;

import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Value
public class Sykefraværprosent {
    public static final int MINIMUM_ANTALL_PERSONER_SOM_SKAL_TIL_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER = 5;

    private final String label;
    private final BigDecimal prosent;
    private final boolean erMaskert;

    public Sykefraværprosent(String label, BigDecimal tapteDagsverk, BigDecimal muligeDagsverk, int antallPersoner) {
        this.label = label;

        if (antallPersoner >= MINIMUM_ANTALL_PERSONER_SOM_SKAL_TIL_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER) {
            erMaskert = false;
            this.prosent = tapteDagsverk
                    .multiply(new BigDecimal(100))
                    .divide(muligeDagsverk, 1, RoundingMode.HALF_UP);
        } else {
            erMaskert = true;
            prosent = null;
        }
    }


    private Sykefraværprosent(String label) {
        this.label = label;
        this.erMaskert = false;
        this.prosent = null;
    }


    public static Sykefraværprosent tomSykefraværprosent(String sykefravaærprosentLabel) {
        return new Sykefraværprosent(sykefravaærprosentLabel);
    }
}
