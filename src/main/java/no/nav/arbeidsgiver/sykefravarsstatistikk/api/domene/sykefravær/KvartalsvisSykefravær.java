package no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.sykefravær;

import com.fasterxml.jackson.annotation.JsonIgnore;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;

import java.math.BigDecimal;
import java.util.Comparator;

public class KvartalsvisSykefravær extends MaskerbarSykefravær implements Comparable<KvartalsvisSykefravær> {


    @JsonIgnore
    private final ÅrstallOgKvartal årstallOgKvartal;

    public KvartalsvisSykefravær(
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
    public int compareTo(KvartalsvisSykefravær kvartalsvisSykefravær) {
        return Comparator
                .comparing(KvartalsvisSykefravær::getÅrstallOgKvartal)
                .compare(this, kvartalsvisSykefravær);
    }

    public ÅrstallOgKvartal getÅrstallOgKvartal() {
        return årstallOgKvartal;
    }
}
