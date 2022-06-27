package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Kvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartal;

import java.math.BigDecimal;
import java.util.Objects;

public class SykefraværMedKategori extends SykefraværForEttKvartal {
    private final Statistikkategori kategori;
    private final String kode;
    private final int antallPersoner;

    public SykefraværMedKategori(
            Statistikkategori statistikkategori,
            String kode,
            Kvartal kvartal,
            BigDecimal tapteDagsverk,
            BigDecimal muligeDagsverk,
            int antallPersoner) {
        super(kvartal, tapteDagsverk, muligeDagsverk, antallPersoner);
        this.kategori = statistikkategori;
        this.kode = kode;
        this.antallPersoner = antallPersoner;
    }

    // OBS: Constructor bruk i testene (objectMapper)
    @JsonCreator
    public SykefraværMedKategori(
            @JsonProperty("kategori") Statistikkategori kategori,
            @JsonProperty("kode") String kode,
            @JsonProperty("årstall") int årstall,
            @JsonProperty("kvartal") int kvartal,
            @JsonProperty("tapteDagsverk")
            @JsonFormat(shape = JsonFormat.Shape.STRING)
                    BigDecimal tapteDagsverk,
            @JsonProperty("muligeDagsverk") BigDecimal muligeDagsverk,
            @JsonProperty("antallPersoner") int antallPersoner
    ){
        super(new Kvartal(årstall, kvartal), tapteDagsverk, muligeDagsverk, antallPersoner);
        this.kategori = kategori;
        this.kode = kode;
        this.antallPersoner = antallPersoner;
    }

    public Statistikkategori getKategori() {
        return kategori;
    }
    public String getKode() {
        return kode;
    }
    public int getAntallPersoner() {
        return antallPersoner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SykefraværMedKategori)) return false;
        if (!super.equals(o)) return false;
        SykefraværMedKategori that = (SykefraværMedKategori) o;
        return super.equals(that) && antallPersoner == that.antallPersoner && kategori == that.kategori && kode.equals(that.kode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), kategori, kode, antallPersoner);
    }
}

