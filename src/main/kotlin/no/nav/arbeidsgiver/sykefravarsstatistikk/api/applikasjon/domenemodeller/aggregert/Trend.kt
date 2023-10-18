package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.aggregert

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.json.StatistikkJson
import java.math.BigDecimal

data class Trend(
    var trendverdi: BigDecimal? = null,
    var antallPersonerIBeregningen: Int = 0,
    var kvartalerIBeregningen: List<ÅrstallOgKvartal>? = null
) {
    fun tilAggregertHistorikkDto(type: Statistikkategori?, label: String?): StatistikkJson {
        return StatistikkJson(
            statistikkategori = type,
            label = label,
            verdi = trendverdi.toString(),
            antallPersonerIBeregningen = antallPersonerIBeregningen,
            kvartalerIBeregningen = kvartalerIBeregningen
        )
    }
}