package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;

@Getter
@AllArgsConstructor
public class Historikkdata {

    private Map<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> sykefravær;

    List<UmaskertSykefraværForEttKvartal> hentFor(Statistikkategori kategori) {
        return sykefravær.get(kategori);
    }
}
