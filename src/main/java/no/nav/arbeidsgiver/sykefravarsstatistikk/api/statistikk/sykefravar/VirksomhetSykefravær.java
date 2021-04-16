package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartal;

import java.math.BigDecimal;

public class VirksomhetSykefravær extends SykefraværForEttKvartal {
    @JsonProperty("kategori")
    Statistikkategori kategori;

    String orgnr;
    String navn;

    public VirksomhetSykefravær(
            String orgnr,
            String navn,
            ÅrstallOgKvartal årstallOgKvartal,
            BigDecimal tapte_dagsverk,
            BigDecimal mulige_dagsverk,
            int antall_personer) {
        super(årstallOgKvartal, tapte_dagsverk, mulige_dagsverk, antall_personer);
        this.kategori = Statistikkategori.VIRKSOMHET;
        this.orgnr = orgnr;
        this.navn = navn;
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
                    BigDecimal tapte_dagsverk,
            @JsonProperty("muligeDagsverk")
                    BigDecimal mulige_dagsverk,
            int antall_personer) {
        super(new ÅrstallOgKvartal(årstall, kvartal), tapte_dagsverk, mulige_dagsverk, antall_personer);
        this.kategori = Statistikkategori.VIRKSOMHET;
        this.orgnr = orgnr;
        this.navn = navn;
    }

    public String getOrgnr() {
        return orgnr;
    }

}

