package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;

@AllArgsConstructor
public class Sykefraværsdata {

    private Map<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> sykefravær;

    List<UmaskertSykefraværForEttKvartal> filtrerPåKategori(Statistikkategori kategori) {
        return sykefravær.get(kategori);
    }

    void filtrerBortKategori(Statistikkategori kategori) {
        sykefravær.remove(kategori);
    }
}
