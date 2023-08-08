package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

data class StatistikkDto(
    val statistikkategori: Statistikkategori? = null,
    val label: String? = null,
    val verdi: String? = null,
    val antallPersonerIBeregningen: Int? = null,
    val kvartalerIBeregningen: List<ÅrstallOgKvartal>? = null
)