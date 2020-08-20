package no.nav.arbeidsgiver.sykefravarsstatistikk.api.varighet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.common.Sykefraværsvarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;

@Data
public class UmaskertKvartalsvisSykefraværMedVarighet implements Comparable<UmaskertKvartalsvisSykefraværMedVarighet> {

    @JsonIgnore
    private final ÅrstallOgKvartal årstallOgKvartal;
    private final BigDecimal tapteDagsverk;
    private final BigDecimal muligeDagsverk;
    private final int antallPersoner;
    private final Sykefraværsvarighet varighet;

    public UmaskertKvartalsvisSykefraværMedVarighet(
            ÅrstallOgKvartal årstallOgKvartal,
            BigDecimal tapteDagsverk,
            BigDecimal muligeDagsverk,
            int antallPersoner,
            Sykefraværsvarighet varighet) {
        this.årstallOgKvartal = årstallOgKvartal;
        this.tapteDagsverk = tapteDagsverk.setScale(1, RoundingMode.HALF_UP);
        this.muligeDagsverk = muligeDagsverk.setScale(1, RoundingMode.HALF_UP);
        this.antallPersoner = antallPersoner;
        this.varighet = varighet;
    }

    public int getKvartal() {
        return årstallOgKvartal != null ? årstallOgKvartal.getKvartal() : 0;
    }

    public int getÅrstall() {
        return årstallOgKvartal != null ? årstallOgKvartal.getÅrstall() : 0;
    }


    @Override
    public int compareTo(UmaskertKvartalsvisSykefraværMedVarighet kvartalsvisSykefravær) {
        return Comparator
                .comparing(UmaskertKvartalsvisSykefraværMedVarighet::getÅrstallOgKvartal)
                .compare(this, kvartalsvisSykefravær);
    }
}
