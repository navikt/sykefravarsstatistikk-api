package no.nav.arbeidsgiver.sykefravarsstatistikk.api.varighet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.common.Sykefraværsvarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;

// TODO gjør inheritance mellom UmaskertKvartalsvisSykefravær, UmaskertKvartalsvisSykefraværMedVarighet og KvartalsvisSykefravær
@Data
public class UmaskertKvartalsvisSykefravær implements Comparable<UmaskertKvartalsvisSykefravær>  {

    @JsonIgnore
    private final ÅrstallOgKvartal årstallOgKvartal;
    private final BigDecimal tapteDagsverk;
    private final BigDecimal muligeDagsverk;
    private final int antallPersoner;

    public UmaskertKvartalsvisSykefravær(
            ÅrstallOgKvartal årstallOgKvartal,
            BigDecimal tapteDagsverk,
            BigDecimal muligeDagsverk,
            int antallPersoner) {
        this.årstallOgKvartal = årstallOgKvartal;
        this.tapteDagsverk = tapteDagsverk.setScale(1, RoundingMode.HALF_UP);
        this.muligeDagsverk = muligeDagsverk.setScale(1, RoundingMode.HALF_UP);
        this.antallPersoner = antallPersoner;
    }

    public int getKvartal() {
        return årstallOgKvartal != null ? årstallOgKvartal.getKvartal() : 0;
    }

    public int getÅrstall() {
        return årstallOgKvartal != null ? årstallOgKvartal.getÅrstall() : 0;
    }


    @Override
    public int compareTo(UmaskertKvartalsvisSykefravær kvartalsvisSykefravær) {
        return Comparator
                .comparing(UmaskertKvartalsvisSykefravær::getÅrstallOgKvartal)
                .compare(this, kvartalsvisSykefravær);
    }
}
