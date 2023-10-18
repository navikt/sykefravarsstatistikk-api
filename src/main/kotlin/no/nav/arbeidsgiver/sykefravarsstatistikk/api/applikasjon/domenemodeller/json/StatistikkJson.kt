package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.json

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.ÅrstallOgKvartal

data class StatistikkJson(
    val statistikkategori: Statistikkategori? = null,
    val label: String? = null,
    val verdi: String? = null,
    val antallPersonerIBeregningen: Int? = null,
    val kvartalerIBeregningen: List<ÅrstallOgKvartal>? = null
)