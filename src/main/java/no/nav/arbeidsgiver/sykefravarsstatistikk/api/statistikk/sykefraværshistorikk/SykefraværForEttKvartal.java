package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Kvartal;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;

public class SykefraværForEttKvartal extends MaskerbartSykefravær implements Comparable<SykefraværForEttKvartal> {

    @JsonIgnore
    private final Kvartal kvartal;

    public SykefraværForEttKvartal(
            Kvartal kvartal,
            BigDecimal tapteDagsverk,
            BigDecimal muligeDagsverk,
            int antallPersoner
    ) {
        super(
                tapteDagsverk,
                muligeDagsverk,
                antallPersoner,
                kvartal != null
        );
        this.kvartal = kvartal;
    }

    public int getKvartal() {
        return kvartal != null ? kvartal.getKvartal() : 0;
    }

    public int getÅrstall() {
        return kvartal != null ? kvartal.getÅrstall() : 0;
    }


    @Override
    public int compareTo(SykefraværForEttKvartal sykefraværForEttKvartal) {
        return Comparator
                .comparing(SykefraværForEttKvartal::getÅrstallOgKvartal)
                .compare(this, sykefraværForEttKvartal);
    }

    public Kvartal getÅrstallOgKvartal() {
        return kvartal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SykefraværForEttKvartal)) return false;
        if (!super.equals(o)) return false;
        SykefraværForEttKvartal that = (SykefraværForEttKvartal) o;
        return (kvartal.equals(that.kvartal)
                && getProsent().equals(that.getProsent())
                && getTapteDagsverk().equals(that.getTapteDagsverk())
                && getMuligeDagsverk().equals(that.getMuligeDagsverk())
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), kvartal);
    }
}
