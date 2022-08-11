package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Konstanter.*;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@EqualsAndHashCode
public abstract class MaskerbartSykefravær {

    private final BigDecimal prosent;
    private final BigDecimal tapteDagsverk;
    private final BigDecimal muligeDagsverk;
    private final boolean erMaskert;

    public MaskerbartSykefravær(
            BigDecimal tapteDagsverk,
            BigDecimal muligeDagsverk,
            int antallPersoner,
            boolean harSykefraværData
    ) {
        erMaskert =
                harSykefraværData &&
                antallPersoner < MIN_ANTALL_PERS_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER;

        if (!erMaskert && harSykefraværData) {
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
