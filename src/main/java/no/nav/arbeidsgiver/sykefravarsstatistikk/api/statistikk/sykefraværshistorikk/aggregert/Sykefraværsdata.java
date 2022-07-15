package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;

@AllArgsConstructor
public class Sykefraværsdata {

    private Map<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> sykefravær;

    List<UmaskertSykefraværForEttKvartal> hentUtFor(Statistikkategori kategori) {
        return sykefravær.get(kategori);
    }

    void filtrerBortDataFor(Statistikkategori kategori) {
        sykefravær.remove(kategori);
    }
}
