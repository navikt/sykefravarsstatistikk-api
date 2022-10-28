package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværOverFlereKvartaler;

import java.math.BigDecimal;
import java.util.List;

public class KafkaStatistikkKategoriTopicValue {

    private final Statistikkategori kategori;
    private final String kode;
    private final SistePubliserteKvartal sistePubliserteKvartal;
    private final Siste4Kvartaler siste4Kvartal;

    public KafkaStatistikkKategoriTopicValue(
            SykefraværMedKategori sisteKvartal,
            SykefraværOverFlereKvartaler siste4Kvartal
    ) {
        this.kategori = sisteKvartal.getKategori();
        this.kode = "NO";
        sistePubliserteKvartal = new SistePubliserteKvartal(
                sisteKvartal.getÅrstall(),
                sisteKvartal.getKvartal(),
                sisteKvartal.getProsent(),
                sisteKvartal.getTapteDagsverk(),
                sisteKvartal.getMuligeDagsverk(),
                sisteKvartal.getAntallPersoner(),
                sisteKvartal.isErMaskert()
        );
        this.siste4Kvartal = new Siste4Kvartaler(
                siste4Kvartal.getProsent(),
                siste4Kvartal.getTapteDagsverk(),
                siste4Kvartal.getMuligeDagsverk(),
                siste4Kvartal.isErMaskert(),
                siste4Kvartal.getKvartaler()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KafkaStatistikkKategoriTopicValue that = (KafkaStatistikkKategoriTopicValue) o;

        if(!siste4Kvartal.equals(that.siste4Kvartal)) return false;
        return sistePubliserteKvartal.equals(that.sistePubliserteKvartal);
    }

    @Override
    public int hashCode() {
        int result = sistePubliserteKvartal.hashCode();
        result = 31 * result + ( siste4Kvartal != null ? siste4Kvartal.hashCode() : 0);
        return result;
    }


    /*
    public SykefraværMedKategori getSisteKvartal() { return sistePubliserteKvartal; }
    public SykefraværOverFlereKvartaler getSiste4Kvartal() { return siste4Kvartal; }*/
}

class SistePubliserteKvartal {
    private int årstall;
    private int kvartal;
    private final BigDecimal prosent;
    private final BigDecimal tapteDagsverk;
    private final BigDecimal muligeDagsverk;
    private int antallPersoner;
    private boolean erMaskert;

    public SistePubliserteKvartal(
            int årstall,
            int kvartal,
            BigDecimal prosent,
            BigDecimal tapteDagsverk,
            BigDecimal muligeDagsverk,
            int antallPersoner,
            boolean erMaskert
    ) {
        this.årstall = årstall;
        this.kvartal = kvartal;
        this.prosent = prosent;
        this.tapteDagsverk = tapteDagsverk;
        this.muligeDagsverk = muligeDagsverk;
        this.antallPersoner = antallPersoner;
        this.erMaskert = erMaskert;
    }
}

class Siste4Kvartaler {
    private final BigDecimal prosent;
    private final BigDecimal tapteDagsverk;
    private final BigDecimal muligeDagsverk;
    private boolean erMaskert;
    private final List<ÅrstallOgKvartal> kvartaler;

    public Siste4Kvartaler(
            BigDecimal prosent,
            BigDecimal tapteDagsverk,
            BigDecimal muligeDagsverk,
            boolean erMaskert,
            List<ÅrstallOgKvartal> kvartaler
    ) {
        this.prosent = prosent;
        this.tapteDagsverk = tapteDagsverk;
        this.muligeDagsverk = muligeDagsverk;
        this.erMaskert = erMaskert;
        this.kvartaler = kvartaler;
    }
}
