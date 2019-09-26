package no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning;

import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Value
public class Sykefraværprosent {
    private final String label;
    private final BigDecimal prosent;

    public Sykefraværprosent(String label, BigDecimal tapteDagsverk, BigDecimal muligeDagsverk) {
        this.label = label;
        this.prosent = tapteDagsverk
                .multiply(new BigDecimal(100))
                .divide(muligeDagsverk, 1, RoundingMode.HALF_UP);
    }
}
