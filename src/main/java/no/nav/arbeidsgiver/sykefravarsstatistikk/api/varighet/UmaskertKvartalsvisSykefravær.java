package no.nav.arbeidsgiver.sykefravarsstatistikk.api.varighet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;

import static java.lang.Integer.max;

@Getter
@EqualsAndHashCode
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

    public static UmaskertKvartalsvisSykefravær tomtUmaskertKvartalsvisSykefravær(ÅrstallOgKvartal årstallOgKvartal) {
        return new UmaskertKvartalsvisSykefravær(årstallOgKvartal, new BigDecimal(0), new BigDecimal(0), 0);
    }

    public UmaskertKvartalsvisSykefravær add(
            UmaskertKvartalsvisSykefravær sykefravær
    ) {
        if (!sykefravær.getÅrstallOgKvartal().equals(årstallOgKvartal)) {
            throw new IllegalArgumentException("Kan ikke summere kvartalsvis sykefravær med forskjellige kvartaler");
        }
        return new UmaskertKvartalsvisSykefravær(
                årstallOgKvartal,
                tapteDagsverk.add(sykefravær.getTapteDagsverk()),
                muligeDagsverk.add(sykefravær.getMuligeDagsverk()),
                max(antallPersoner, sykefravær.getAntallPersoner())
        );
    }

    @Override
    public int compareTo(UmaskertKvartalsvisSykefravær kvartalsvisSykefravær) {
        return Comparator
                .comparing(UmaskertKvartalsvisSykefravær::getÅrstallOgKvartal)
                .compare(this, kvartalsvisSykefravær);
    }
}
