package no.nav.arbeidsgiver.sykefravarsstatistikk.api.varighet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;

@Data
public class KvartalsvisSykefraværMedVarighet implements Comparable<KvartalsvisSykefraværMedVarighet> {

    @JsonIgnore
    private final ÅrstallOgKvartal årstallOgKvartal;
    private final BigDecimal tapteDagsverk;
    private final BigDecimal muligeDagsverk;
    private final int antallPersoner;
    private final String typeVarighet;

    public KvartalsvisSykefraværMedVarighet(
            ÅrstallOgKvartal årstallOgKvartal,
            BigDecimal tapteDagsverk,
            BigDecimal muligeDagsverk,
            int antallPersoner,
            String typeVarighet) {
        this.årstallOgKvartal = årstallOgKvartal;
        this.tapteDagsverk = tapteDagsverk.setScale(1, RoundingMode.HALF_UP);
        this.muligeDagsverk = muligeDagsverk.setScale(1, RoundingMode.HALF_UP);
        this.antallPersoner = antallPersoner;
        this.typeVarighet = typeVarighet;
    }

    public int getKvartal() {
        return årstallOgKvartal != null ? årstallOgKvartal.getKvartal() : 0;
    }

    public int getÅrstall() {
        return årstallOgKvartal != null ? årstallOgKvartal.getÅrstall() : 0;
    }


    @Override
    public int compareTo(KvartalsvisSykefraværMedVarighet kvartalsvisSykefravær) {
        return Comparator
                .comparing(KvartalsvisSykefraværMedVarighet::getÅrstallOgKvartal)
                .compare(this, kvartalsvisSykefravær);
    }
}
