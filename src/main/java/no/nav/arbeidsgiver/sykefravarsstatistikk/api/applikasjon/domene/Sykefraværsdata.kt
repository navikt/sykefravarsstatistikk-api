package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal

class Sykefraværsdata(
    val sykefravær: MutableMap<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> = mutableMapOf()
) {
    fun filtrerPåKategori(kategori: Statistikkategori): List<UmaskertSykefraværForEttKvartal> =
        sykefravær[kategori] ?: listOf()

    fun filtrerBortVirksomhetsdata() {
        sykefravær.remove(Statistikkategori.VIRKSOMHET)
    }
}