package no.nav.arbeidsgiver.sykefravarsstatistikk.api.varighet;

import lombok.Getter;
import lombok.Setter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.common.Konstanter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


@Setter
@Getter
public class SykefraværSiste4Kvartaler {

    private final BigDecimal prosent;
    private final BigDecimal tapteDagsverk;
    private final BigDecimal muligeDagsverk;
    private final boolean erMaskert;
    private final List<ÅrstallOgKvartal> kvartaler;


    public SykefraværSiste4Kvartaler(
            BigDecimal tapteDagsverk,
            BigDecimal muligeDagsverk,
            int maksAntallPersonerOverPerioden,
            List<ÅrstallOgKvartal> kvartaler
    ) {
        // TODO: IKKE dupliser logikk for maskering (abstract|interface)
        boolean harSykefraværData = !kvartaler.isEmpty();

        erMaskert = harSykefraværData &&
                maksAntallPersonerOverPerioden <
                        Konstanter.MINIMUM_ANTALL_PERSONER_SOM_SKAL_TIL_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER;
        this.kvartaler = kvartaler;

        if (harSykefraværData && !erMaskert) {
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
