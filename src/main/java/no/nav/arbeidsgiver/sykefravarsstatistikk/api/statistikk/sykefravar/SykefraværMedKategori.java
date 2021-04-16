package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartal;

import java.math.BigDecimal;

public class SykefraværMedKategori extends SykefraværForEttKvartal {
    private final Statistikkategori kategori;
    private final String kode;

    public SykefraværMedKategori(
            Statistikkategori statistikkategori,
            String kode,
            ÅrstallOgKvartal årstallOgKvartal,
            BigDecimal tapteDagsverk,
            BigDecimal muligeDagsverk,
            int antallPersoner) {
        super(årstallOgKvartal, tapteDagsverk, muligeDagsverk, antallPersoner);
        this.kategori = statistikkategori;
        this.kode = kode;
    }

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
            @JsonProperty("antallPersoner") int antallPersoner) {
        //TODO finne ut hvordan kan vi kvitte oss å bruke antall personer
        // for den gjør at vi mister tapte og mulige-dagsverk, AntallPersoner fins ikke i Message fra Kafka
        // Dette medfører at testen ikke funker som det skal.
        super(new ÅrstallOgKvartal(årstall, kvartal), tapteDagsverk, muligeDagsverk, 100);
        this.kategori = kategori;
        this.kode = kode;
    }


    public Statistikkategori getKategori() {
        return kategori;
    }

    public String getKode() {
        return kode;
    }
}

