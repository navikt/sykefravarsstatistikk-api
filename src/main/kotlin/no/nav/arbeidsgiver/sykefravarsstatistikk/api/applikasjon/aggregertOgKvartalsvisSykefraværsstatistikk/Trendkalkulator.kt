package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Trend
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.UmaskertSykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal

data class Trendkalkulator(
    var datagrunnlag: List<UmaskertSykefraværForEttKvartal>,
    var sistePubliserteKvartal: ÅrstallOgKvartal
) {
    fun kalkulerTrend(): Either<UtilstrekkeligData, Trend> {
        val ettÅrSiden = sistePubliserteKvartal.minusEttÅr()
        val nyesteSykefravær = hentUtKvartal(datagrunnlag, sistePubliserteKvartal)
        val sykefraværetEttÅrSiden = hentUtKvartal(datagrunnlag, ettÅrSiden)
        if (nyesteSykefravær == null || sykefraværetEttÅrSiden == null) {
            return UtilstrekkeligData(
                "Mangler data for $sistePubliserteKvartal og/eller $ettÅrSiden"
            ).left()
        }
        val nyesteSykefraværsprosent = nyesteSykefravær.kalkulerSykefraværsprosent().getOrNull()
        val sykefraværsprosentEttÅrSiden = sykefraværetEttÅrSiden.kalkulerSykefraværsprosent().getOrNull()
        if (nyesteSykefraværsprosent == null || sykefraværsprosentEttÅrSiden == null) {
            return UtilstrekkeligData("Feil i utregningen av sykefraværsprosenten, kan ikke regne ut trendverdi.").left()
        }
        val trendverdi = nyesteSykefraværsprosent.subtract(sykefraværsprosentEttÅrSiden)
        val antallTilfeller = (nyesteSykefravær.antallPersoner
                + sykefraværetEttÅrSiden.antallPersoner)
        return Trend(trendverdi, antallTilfeller, listOf(sistePubliserteKvartal, ettÅrSiden)).right()
    }
}

fun hentUtKvartal(
    sykefravær: Collection<UmaskertSykefraværForEttKvartal>, kvartal: ÅrstallOgKvartal
): UmaskertSykefraværForEttKvartal? {
    return sykefravær.find { it.årstallOgKvartal == kvartal }
}
