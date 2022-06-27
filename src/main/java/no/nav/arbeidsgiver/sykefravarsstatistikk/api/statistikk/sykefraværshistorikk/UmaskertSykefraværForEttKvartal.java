package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Kvartal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;

import static java.lang.Integer.max;

@Getter
@EqualsAndHashCode
@ToString
public class UmaskertSykefraværForEttKvartal implements Comparable<UmaskertSykefraværForEttKvartal>  {

    @JsonIgnore
    private final Kvartal kvartal;
    private final BigDecimal tapteDagsverk;
    private final BigDecimal muligeDagsverk;
    private final int antallPersoner;

    public UmaskertSykefraværForEttKvartal(
            Kvartal kvartal,
            BigDecimal tapteDagsverk,
            BigDecimal muligeDagsverk,
            int antallPersoner) {
        this.kvartal = kvartal;
        this.tapteDagsverk = tapteDagsverk.setScale(1, RoundingMode.HALF_UP);
        this.muligeDagsverk = muligeDagsverk.setScale(1, RoundingMode.HALF_UP);
        this.antallPersoner = antallPersoner;
    }

    public int getKvartalsverdi() {
        return kvartal != null ? kvartal.getKvartal() : 0;
    }

    public int getÅrstall() {
        return kvartal != null ? kvartal.getÅrstall() : 0;
    }

    public BigDecimal getProsent() {
        return getTapteDagsverk().divide(getMuligeDagsverk()).multiply(new BigDecimal(100));
    }

    public static UmaskertSykefraværForEttKvartal tomtUmaskertKvartalsvisSykefravær(Kvartal kvartal) {
        return new UmaskertSykefraværForEttKvartal(kvartal, new BigDecimal(0), new BigDecimal(0), 0);
    }

    public UmaskertSykefraværForEttKvartal add(
            UmaskertSykefraværForEttKvartal sykefravær
    ) {
        if (!sykefravær.getKvartal().equals(kvartal)) {
            throw new IllegalArgumentException("Kan ikke summere kvartalsvis sykefravær med forskjellige kvartaler");
        }
        return new UmaskertSykefraværForEttKvartal(
                kvartal,
                tapteDagsverk.add(sykefravær.getTapteDagsverk()),
                muligeDagsverk.add(sykefravær.getMuligeDagsverk()),
                max(antallPersoner, sykefravær.getAntallPersoner())
        );
    }

    @Override
    public int compareTo(UmaskertSykefraværForEttKvartal kvartalsvisSykefravær) {
        return Comparator
                .comparing(UmaskertSykefraværForEttKvartal::getKvartalsverdi)
                .compare(this, kvartalsvisSykefravær);
    }
}
