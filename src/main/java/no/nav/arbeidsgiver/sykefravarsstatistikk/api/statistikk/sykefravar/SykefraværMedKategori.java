package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartal;

import java.math.BigDecimal;

public class SykefraværMedKategori extends SykefraværForEttKvartal {
    Statistikkategori kategori;
    String kode;
    String beskrivelse;

    public SykefraværMedKategori(
            Statistikkategori statistikkategori,
            String kode,
            String beskrivelse,
            ÅrstallOgKvartal årstallOgKvartal,
            BigDecimal tapte_dagsverk,
            BigDecimal mulige_dagsverk,
            int antall_personer) {
        super(årstallOgKvartal, tapte_dagsverk, mulige_dagsverk, antall_personer);
        this.kategori = statistikkategori;
        this.kode = kode;
        this.beskrivelse = beskrivelse;
    }
}

