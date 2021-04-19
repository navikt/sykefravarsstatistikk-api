package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;

public class SykefraværForEttKvartal extends MaskerbartSykefravær implements Comparable<SykefraværForEttKvartal> {

    @JsonIgnore
    private final ÅrstallOgKvartal årstallOgKvartal;

    public SykefraværForEttKvartal(
            ÅrstallOgKvartal årstallOgKvartal,
            BigDecimal tapteDagsverk,
            BigDecimal muligeDagsverk,
            int antallPersoner
    ) {
        super(
                tapteDagsverk,
                muligeDagsverk,
                antallPersoner,
                årstallOgKvartal != null
        );
        this.årstallOgKvartal = årstallOgKvartal;
    }

    public int getKvartal() {
        return årstallOgKvartal != null ? årstallOgKvartal.getKvartal() : 0;
    }

    public int getÅrstall() {
        return årstallOgKvartal != null ? årstallOgKvartal.getÅrstall() : 0;
    }


    @Override
    public int compareTo(SykefraværForEttKvartal sykefraværForEttKvartal) {
        return Comparator
                .comparing(SykefraværForEttKvartal::getÅrstallOgKvartal)
                .compare(this, sykefraværForEttKvartal);
    }

    public ÅrstallOgKvartal getÅrstallOgKvartal() {
        return årstallOgKvartal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SykefraværForEttKvartal)) return false;
        if (!super.equals(o)) return false;
        SykefraværForEttKvartal that = (SykefraværForEttKvartal) o;
        return (årstallOgKvartal.equals(that.årstallOgKvartal)
                && getProsent().equals(that.getProsent())
                && getTapteDagsverk().equals(that.getTapteDagsverk())
                && getMuligeDagsverk().equals(that.getMuligeDagsverk())
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), årstallOgKvartal);
    }
}
