package no.nav.tag.sykefravarsstatistikk.api.sykefraværshistorikk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;

import java.math.BigDecimal;
import java.util.Comparator;

@Data
public class KvartalsvisSykefraværsprosent implements Comparable<KvartalsvisSykefraværsprosent> {

    @JsonIgnore
    private final ÅrstallOgKvartal årstallOgKvartal;
    @JsonIgnore
    private final Sykefraværprosent sykefraværprosent;

    private final BigDecimal tapteDagsverk;
    private final BigDecimal muligeDagsverk;

    public KvartalsvisSykefraværsprosent(
            ÅrstallOgKvartal årstallOgKvartal,
            Sykefraværprosent sykefraværprosent
    ) {
        this.årstallOgKvartal = årstallOgKvartal;
        this.sykefraværprosent = sykefraværprosent;
        this.tapteDagsverk = sykefraværprosent.getTapteDagsverk();
        this.muligeDagsverk = sykefraværprosent.getMuligeDagsverk();
    }

    public static KvartalsvisSykefraværsprosent tomKvartalsvisSykefraværprosent() {
        return new KvartalsvisSykefraværsprosent();
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


    private KvartalsvisSykefraværsprosent() {
        this.årstallOgKvartal = null;
        this.sykefraværprosent = null;
        this.tapteDagsverk = null;
        this.muligeDagsverk = null;
    }


    @Override
    public int compareTo(KvartalsvisSykefraværsprosent kvartalsvisSykefraværsprosent) {
        return Comparator
                .comparing(KvartalsvisSykefraværsprosent::getÅrstallOgKvartal)
                .compare(this, kvartalsvisSykefraværsprosent);
    }
}
