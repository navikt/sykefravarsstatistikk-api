package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregering

import io.vavr.control.Either
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Trend
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.UmaskertSykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.exceptions.UtilstrekkeligDataException
import java.util.*

data class Trendkalkulator(
    var datagrunnlag: List<UmaskertSykefraværForEttKvartal>,
    var sistePubliserteKvartal: ÅrstallOgKvartal
) {
    fun kalkulerTrend(): Either<UtilstrekkeligDataException, Trend> {
        val ettÅrSiden = sistePubliserteKvartal.minusEttÅr()
        val nyesteSykefravær = hentUtKvartal(datagrunnlag, sistePubliserteKvartal)
        val sykefraværetEttÅrSiden = hentUtKvartal(datagrunnlag, ettÅrSiden)
        if (nyesteSykefravær.isEmpty || sykefraværetEttÅrSiden.isEmpty) {
            return Either.left(
                UtilstrekkeligDataException(
                    "Mangler data for $sistePubliserteKvartal og/eller $ettÅrSiden"
                )
            )
        }
        val nyesteSykefraværsprosent = nyesteSykefravær.get().kalkulerSykefraværsprosent()
        val sykefraværsprosentEttÅrSiden = sykefraværetEttÅrSiden.get().kalkulerSykefraværsprosent()
        if (nyesteSykefraværsprosent.isLeft || sykefraværsprosentEttÅrSiden.isLeft) {
            return Either.left(
                UtilstrekkeligDataException(
                    "Feil i utregningen av sykefraværsprosenten, kan ikke regne ut trendverdi."
                )
            )
        }
        val trendverdi = nyesteSykefraværsprosent.get().subtract(sykefraværsprosentEttÅrSiden.get())
        val antallTilfeller = (nyesteSykefravær.get().antallPersoner
                + sykefraværetEttÅrSiden.get().antallPersoner)
        return Either.right(
            Trend(trendverdi, antallTilfeller, java.util.List.of(sistePubliserteKvartal, ettÅrSiden))
        )
    }
}

fun hentUtKvartal(
    sykefravær: Collection<UmaskertSykefraværForEttKvartal>, kvartal: ÅrstallOgKvartal
): Optional<UmaskertSykefraværForEttKvartal> {
    return Optional.ofNullable(sykefravær.find { it.årstallOgKvartal == kvartal })
}
