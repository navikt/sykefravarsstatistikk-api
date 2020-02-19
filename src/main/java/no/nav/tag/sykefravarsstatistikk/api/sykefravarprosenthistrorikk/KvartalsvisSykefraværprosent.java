package no.nav.tag.sykefravarsstatistikk.api.sykefravarprosenthistrorikk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;

import java.math.BigDecimal;
import java.util.Comparator;

@Data
public class KvartalsvisSykefraværprosent implements Comparable<KvartalsvisSykefraværprosent> {

    @JsonIgnore
    private final ÅrstallOgKvartal årstallOgKvartal;
    @JsonIgnore
    private final Sykefraværprosent sykefraværprosent;

    public KvartalsvisSykefraværprosent(
            ÅrstallOgKvartal årstallOgKvartal,
            Sykefraværprosent sykefraværprosent
    ) {
        this.årstallOgKvartal = årstallOgKvartal;
        this.sykefraværprosent = sykefraværprosent;
    }

    public static KvartalsvisSykefraværprosent tomKvartalsvisSykefraværprosent() {
        return new KvartalsvisSykefraværprosent();
    }

    public int getKvartal() {
        return årstallOgKvartal != null ? årstallOgKvartal.getKvartal() : 0;
    }

    public BigDecimal getProsent() {
        return sykefraværprosent.getProsent();
    }

    public int getÅrstall() {
        return årstallOgKvartal != null ? årstallOgKvartal.getÅrstall() : 0;
    }

    public boolean isErMaskert() {
        return sykefraværprosent.isErMaskert();
    }


    private KvartalsvisSykefraværprosent() {
        this.årstallOgKvartal = null;
        this.sykefraværprosent = null;
    }


    @Override
    public int compareTo(KvartalsvisSykefraværprosent kvartalsvisSykefraværprosent) {
        return Comparator
                .comparing(KvartalsvisSykefraværprosent::getÅrstallOgKvartal)
                .compare(this, kvartalsvisSykefraværprosent);
    }
}
