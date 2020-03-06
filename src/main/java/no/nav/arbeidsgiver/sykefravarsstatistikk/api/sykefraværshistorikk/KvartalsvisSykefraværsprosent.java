package no.nav.arbeidsgiver.sykefravarsstatistikk.api.sykefraværshistorikk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.common.Konstanter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;

@Data
public class KvartalsvisSykefraværsprosent implements Comparable<KvartalsvisSykefraværsprosent> {

    @JsonIgnore
    private final ÅrstallOgKvartal årstallOgKvartal;

    private final BigDecimal prosent;
    private final BigDecimal tapteDagsverk;
    private final BigDecimal muligeDagsverk;
    private final boolean erMaskert;

    public KvartalsvisSykefraværsprosent(
            ÅrstallOgKvartal årstallOgKvartal,
            BigDecimal tapteDagsverk,
            BigDecimal muligeDagsverk,
            int antallPersoner
    ) {
        this.årstallOgKvartal = årstallOgKvartal;

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
    public int getKvartal() {
        return årstallOgKvartal != null ? årstallOgKvartal.getKvartal() : 0;
    }

    public int getÅrstall() {
        return årstallOgKvartal != null ? årstallOgKvartal.getÅrstall() : 0;
    }


    @Override
    public int compareTo(KvartalsvisSykefraværsprosent kvartalsvisSykefraværsprosent) {
        return Comparator
                .comparing(KvartalsvisSykefraværsprosent::getÅrstallOgKvartal)
                .compare(this, kvartalsvisSykefraværsprosent);
    }
}
