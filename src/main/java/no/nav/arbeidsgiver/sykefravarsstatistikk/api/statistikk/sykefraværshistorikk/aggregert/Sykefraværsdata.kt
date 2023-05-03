package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal

class Sykefraværsdata(
    val sykefravær: MutableMap<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> = mutableMapOf()
) {
    fun filtrerPåKategori(kategori: Statistikkategori): List<UmaskertSykefraværForEttKvartal> {
        return if (sykefravær.containsKey(kategori)) {
            sykefravær[kategori]!!
        } else listOf()
    }

    fun filtrerBortVirksomhetsdata() {
        sykefravær.remove(Statistikkategori.VIRKSOMHET)
    }
}