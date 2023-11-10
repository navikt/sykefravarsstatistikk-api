package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.UmaskertSykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Statistikkategori

class Sykefraværsdata(
    val sykefravær: Map<Statistikkategori, List<UmaskertSykefraværForEttKvartal>>
) {
    fun filtrerPåKategori(kategori: Statistikkategori): List<UmaskertSykefraværForEttKvartal> =
        sykefravær[kategori] ?: listOf()
}