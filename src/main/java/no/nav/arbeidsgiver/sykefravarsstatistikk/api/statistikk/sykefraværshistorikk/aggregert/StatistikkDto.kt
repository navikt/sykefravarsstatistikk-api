package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori

data class StatistikkDto(
    val statistikkategori: Statistikkategori? = null,
    val label: String? = null,
    val verdi: String? = null,
    val antallPersonerIBeregningen: Int? = null,
    val kvartalerIBeregningen: List<ÅrstallOgKvartal>? = null
)