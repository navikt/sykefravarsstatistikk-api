package no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning;

import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Value
public class Sykefraværprosent {
    private final String label;
    private final BigDecimal prosent;
    private final boolean erMaskert;

    public Sykefraværprosent(String label, BigDecimal tapteDagsverk, BigDecimal muligeDagsverk, int antallPersoner) {
        this.label = label;

        if (antallPersoner > 4) {
            erMaskert = false;
            this.prosent = tapteDagsverk
                    .multiply(new BigDecimal(100))
                    .divide(muligeDagsverk, 1, RoundingMode.HALF_UP);
        } else {
            erMaskert = true;
            prosent = null;
        }
    }
}
