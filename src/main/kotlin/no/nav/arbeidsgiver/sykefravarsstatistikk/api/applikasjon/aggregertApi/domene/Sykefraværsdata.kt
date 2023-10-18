package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertApi.domene

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.UmaskertSykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Statistikkategori

class Sykefraværsdata(
    val sykefravær: MutableMap<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> = mutableMapOf()
) {
    fun filtrerPåKategori(kategori: Statistikkategori): List<UmaskertSykefraværForEttKvartal> =
        sykefravær[kategori] ?: listOf()

    fun filtrerBortVirksomhetsdata() {
        sykefravær.remove(Statistikkategori.VIRKSOMHET)
    }
}