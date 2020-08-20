package no.nav.arbeidsgiver.sykefravarsstatistikk.api.varighet;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.common.Konstanter;

import java.math.BigDecimal;
import java.math.RoundingMode;


@Setter
@Getter
public class Siste4KvartalerSykefravær {

    private final BigDecimal prosent;
    private final BigDecimal tapteDagsverk;
    private final BigDecimal muligeDagsverk;
    private final boolean erMaskert;

    public Siste4KvartalerSykefravær(
            BigDecimal tapteDagsverk,
            BigDecimal muligeDagsverk,
            int antallPersoner
    ) {

        if (antallPersoner >= Konstanter.MINIMUM_ANTALL_PERSONER_SOM_SKAL_TIL_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER) {
            erMaskert = false;
            prosent = tapteDagsverk
                    .multiply(new BigDecimal(100))
                    .divide(muligeDagsverk, 1, RoundingMode.HALF_UP);
            this.tapteDagsverk = tapteDagsverk.setScale(1, RoundingMode.HALF_UP);
            this.muligeDagsverk = muligeDagsverk.setScale(1, RoundingMode.HALF_UP);
        } else {
            erMaskert = true;
            prosent = null;
            this.tapteDagsverk = null;
            this.muligeDagsverk = null;
        }
    }
    //int getStørsteAntallPersonerSiste4Kvartaler
}
