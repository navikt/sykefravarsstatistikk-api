package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertApi

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal

data class StatistikkJson(
    val statistikkategori: Statistikkategori? = null,
    val label: String? = null,
    val verdi: String? = null,
    val antallPersonerIBeregningen: Int? = null,
    val kvartalerIBeregningen: List<ÅrstallOgKvartal>? = null
)