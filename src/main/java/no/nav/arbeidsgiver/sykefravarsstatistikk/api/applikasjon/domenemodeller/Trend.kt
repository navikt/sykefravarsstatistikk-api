package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import java.math.BigDecimal

data class Trend(
    var trendverdi: BigDecimal? = null,
    var antallPersonerIBeregningen: Int = 0,
    var kvartalerIBeregningen: List<ÅrstallOgKvartal>? = null
) {
    fun tilAggregertHistorikkDto(type: Statistikkategori?, label: String?): StatistikkDto {
        return StatistikkDto(
            statistikkategori = type,
            label = label,
            verdi = trendverdi.toString(),
            antallPersonerIBeregningen = antallPersonerIBeregningen,
            kvartalerIBeregningen = kvartalerIBeregningen
        )
    }
}