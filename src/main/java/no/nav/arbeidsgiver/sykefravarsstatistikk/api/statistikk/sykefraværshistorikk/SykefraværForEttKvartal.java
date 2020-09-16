package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;

import java.math.BigDecimal;
import java.util.Comparator;

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
}
