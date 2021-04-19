package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartal;

import java.math.BigDecimal;
import java.util.Objects;

public class VirksomhetSykefravær extends SykefraværForEttKvartal {

    @JsonProperty("kategori")
    private final Statistikkategori kategori;
    private final String orgnr;
    private final String navn;
    private final int antallPersoner;


    public VirksomhetSykefravær(
            String orgnr,
            String navn,
            ÅrstallOgKvartal årstallOgKvartal,
            BigDecimal tapteDagsverk,
            BigDecimal mulige_dagsverk,
            int antallPersoner
    ) {
        super(årstallOgKvartal, tapteDagsverk, mulige_dagsverk, antallPersoner);
        this.kategori = Statistikkategori.VIRKSOMHET;
        this.orgnr = orgnr;
        this.navn = navn;
        this.antallPersoner=antallPersoner;
    }

    @JsonCreator
    public VirksomhetSykefravær(
            @JsonProperty("orgnr")
                    String orgnr,
            @JsonProperty("navn")
                    String navn,
            @JsonProperty("årstall")
                    int årstall,
            @JsonProperty("kvartal")
                    int kvartal,
            @JsonProperty("tapteDagsverk")
                    BigDecimal tapteDagsverk,
            @JsonProperty("muligeDagsverk")
                    BigDecimal muligeDagsverk,
            @JsonProperty("antallPersoner")
                    int antallPersoner
    ) {
        super(new ÅrstallOgKvartal(årstall, kvartal), tapteDagsverk, muligeDagsverk, antallPersoner);
        this.kategori = Statistikkategori.VIRKSOMHET;
        this.orgnr = orgnr;
        this.navn = navn;
        this.antallPersoner=antallPersoner;
    }

    public String getOrgnr() { return orgnr; }
    public String getNavn() { return navn; }
    public int getAntallPersoner() { return antallPersoner; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VirksomhetSykefravær)) return false;
        if (!super.equals(o)) return false;
        VirksomhetSykefravær that = (VirksomhetSykefravær) o;
        return super.equals(that)
                && antallPersoner == that.antallPersoner
                && kategori == that.kategori
                && orgnr.equals(that.orgnr)
                && navn.equals(that.navn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), kategori, orgnr, navn, antallPersoner);
    }
}

