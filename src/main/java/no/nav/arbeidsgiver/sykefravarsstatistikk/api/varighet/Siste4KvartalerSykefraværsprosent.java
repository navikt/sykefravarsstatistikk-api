package no.nav.arbeidsgiver.sykefravarsstatistikk.api.varighet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.common.Konstanter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Data
public class Siste4KvartalerSykefraværsprosent {

    @JsonIgnore
    private final List<ÅrstallOgKvartal> årstallerOgKvartaler;

    private final BigDecimal prosent;
    private final BigDecimal tapteDagsverk;
    private final BigDecimal muligeDagsverk;
    private final boolean erMaskert;

    public Siste4KvartalerSykefraværsprosent(
            List<UmaskertKvartalsvisSykefraværMedVarighet> umaskertKvartalsvisSykefraværMedVarighets,
           List< ÅrstallOgKvartal> årstallerOgKvartaler,
            BigDecimal tapteDagsverk,
            BigDecimal muligeDagsverk,
            int antallPersoner
    ) {
     /*   antallPersoner: 1
                ant:2,
                        ant:4,
                ant:3
                */
        this.årstallerOgKvartaler = årstallerOgKvartaler;
     //   umaskertKvartalsvisSykefraværMedVarighets
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
