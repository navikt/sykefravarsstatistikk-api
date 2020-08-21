package no.nav.arbeidsgiver.sykefravarsstatistikk.api.varighet;

import lombok.Getter;
import lombok.Setter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.common.Konstanter;

import java.math.BigDecimal;
import java.math.RoundingMode;


@Setter
@Getter
public class SykefraværSiste4Kvartaler {

    private final BigDecimal prosent;
    private final BigDecimal tapteDagsverk;
    private final BigDecimal muligeDagsverk;
    private final boolean erMaskert;

    // TODO: vi vil ha en liste av de kvartalene som gjelder

    public SykefraværSiste4Kvartaler(
            BigDecimal tapteDagsverk,
            BigDecimal muligeDagsverk,
            int maksAntallPersonerOverPerioden
    ) {
        // TODO: IKKE dupliser logikk for maskering (abstract|interface)
        erMaskert =
                maksAntallPersonerOverPerioden <
                        Konstanter.MINIMUM_ANTALL_PERSONER_SOM_SKAL_TIL_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER;

        if (!erMaskert) {
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
