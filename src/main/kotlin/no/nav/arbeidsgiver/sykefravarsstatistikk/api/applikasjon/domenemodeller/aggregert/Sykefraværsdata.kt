package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.aggregert

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.UmaskertSykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.Statistikkategori

class Sykefraværsdata(
    val sykefravær: MutableMap<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> = mutableMapOf()
) {
    fun filtrerPåKategori(kategori: Statistikkategori): List<UmaskertSykefraværForEttKvartal> =
        sykefravær[kategori] ?: listOf()

    fun filtrerBortVirksomhetsdata() {
        sykefravær.remove(Statistikkategori.VIRKSOMHET)
    }
}