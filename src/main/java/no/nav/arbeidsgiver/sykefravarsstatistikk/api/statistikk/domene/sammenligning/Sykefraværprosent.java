package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.domene.sammenligning;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Value;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Konstanter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Deprecated
@Value
public class Sykefraværprosent {

    private final String label;
    private final BigDecimal prosent;
    private final boolean erMaskert;

    @JsonIgnore
    private final BigDecimal tapteDagsverk;
    @JsonIgnore
    private final BigDecimal muligeDagsverk;

    @JsonIgnore
    private final Integer antallPersoner;

    public Sykefraværprosent(String label, BigDecimal tapteDagsverk, BigDecimal muligeDagsverk, int antallPersoner) {
        this.label = label;

        if (antallPersoner >= Konstanter.MINIMUM_ANTALL_PERSONER_SOM_SKAL_TIL_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER) {
            this.antallPersoner = antallPersoner;
            erMaskert = false;
            prosent = tapteDagsverk
                    .multiply(new BigDecimal(100))
                    .divide(muligeDagsverk, 1, RoundingMode.HALF_UP);
            this.tapteDagsverk = tapteDagsverk.setScale(1, RoundingMode.HALF_UP);
            this.muligeDagsverk = muligeDagsverk.setScale(1, RoundingMode.HALF_UP);
        } else {
            this.antallPersoner = null;
            erMaskert = true;
            prosent = null;
            this.tapteDagsverk = null;
            this.muligeDagsverk = null;
        }
    }


    private Sykefraværprosent(String label) {
        this.label = label;
        erMaskert = false;
        prosent = null;
        antallPersoner = null;
        tapteDagsverk = null;
        muligeDagsverk = null;
    }


    public static Sykefraværprosent tomSykefraværprosent(String sykefravaærprosentLabel) {
        return new Sykefraværprosent(sykefravaærprosentLabel);
    }
}
