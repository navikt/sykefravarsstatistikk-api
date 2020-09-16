package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.domene.sykefravær;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Konstanter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@EqualsAndHashCode
public abstract class MaskerbarSykefravær {

    private final BigDecimal prosent;
    private final BigDecimal tapteDagsverk;
    private final BigDecimal muligeDagsverk;
    private final boolean erMaskert;

    public MaskerbarSykefravær(
            BigDecimal tapteDagsverk,
            BigDecimal muligeDagsverk,
            int antallPersoner,
            boolean harSykefraværData
    ) {
        erMaskert =
                harSykefraværData &&
                antallPersoner
                        < Konstanter.MINIMUM_ANTALL_PERSONER_SOM_SKAL_TIL_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER;
        boolean kanKalkuleres = harSykefraværData;

        if (!erMaskert && kanKalkuleres) {
            prosent = tapteDagsverk
                    .multiply(new BigDecimal(100))
                    .divide(muligeDagsverk, 1, RoundingMode.HALF_UP);
            this.tapteDagsverk = tapteDagsverk.setScale(1, RoundingMode.HALF_UP);
            this.muligeDagsverk = muligeDagsverk.setScale(1, RoundingMode.HALF_UP);
        } else {
            prosent = null;
            this.tapteDagsverk = null;
            this.muligeDagsverk = null;
        }
    }
}
