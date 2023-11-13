package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk

import arrow.core.Either
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.SumAvSykefraværOverFlereKvartaler
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.SumAvSykefraværOverFlereKvartaler.Companion.NULLPUNKT
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Sykefraværsdata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Trend
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.BransjeEllerNæring
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.UmaskertSykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal

class Aggregeringskalkulator(
    private var sykefraværsdata: Sykefraværsdata,
    private var sistePubliserteKvartal: ÅrstallOgKvartal
) {
    fun fraværsprosentNorge(): Either<Statistikkfeil, StatistikkJson> {
        return summerOppSisteFireKvartaler(sykefraværsdata.sykefravær[Aggregeringskategorier.Land] ?: listOf())
            .regnUtProsentOgMapTilDto(Statistikkategori.LAND, "Norge")
    }

    fun fraværsprosentBransjeEllerNæring(
        bransjeEllerNæring: BransjeEllerNæring
    ): Either<Statistikkfeil, StatistikkJson> {
        val statistikk = sykefraværsdata.sykefravær.entries.find { it.key is Aggregeringskategorier.Bransje }
            ?: sykefraværsdata.sykefravær.entries.find { it.key is Aggregeringskategorier.Næring }
        return summerOppSisteFireKvartaler(statistikk?.value ?: emptyList())
            .regnUtProsentOgMapTilDto(
                bransjeEllerNæring.statistikkategori, bransjeEllerNæring.navn()
            )
    }

    fun tapteDagsverkVirksomhet(bedriftsnavn: String): Either<Statistikkfeil, StatistikkJson> {
        return summerOppSisteFireKvartaler(
            sykefraværsdata.sykefravær.entries.find { it.key is Aggregeringskategorier.Virksomhet }?.value
                ?: emptyList()
        ).getTapteDagsverkOgMapTilDto(Statistikkategori.VIRKSOMHET, bedriftsnavn)
    }

    fun muligeDagsverkVirksomhet(bedriftsnavn: String): Either<Statistikkfeil, StatistikkJson> {
        return summerOppSisteFireKvartaler(
            sykefraværsdata.sykefravær.entries.find { it.key is Aggregeringskategorier.Virksomhet }?.value
                ?: emptyList()
        ).getMuligeDagsverkOgMapTilDto(Statistikkategori.VIRKSOMHET, bedriftsnavn)
    }

    fun fraværsprosentVirksomhet(
        virksomhetsnavn: String
    ): Either<Statistikkfeil, StatistikkJson> {
        return summerOppSisteFireKvartaler(
            sykefraværsdata.sykefravær.entries.find { it.key is Aggregeringskategorier.Virksomhet }?.value
                ?: emptyList()
        ).regnUtProsentOgMapTilDto(Statistikkategori.VIRKSOMHET, virksomhetsnavn)
    }

    fun trendBransjeEllerNæring(
        bransjeEllerNæring: BransjeEllerNæring
    ): Either<UtilstrekkeligData, StatistikkJson> {
        val statistikk = sykefraværsdata.sykefravær.entries.find { it.key is Aggregeringskategorier.Bransje }?.value
            ?: sykefraværsdata.sykefravær.entries.find { it.key is Aggregeringskategorier.Næring }?.value
            ?: emptyList()
        val maybeTrend = Trendkalkulator(
            statistikk,
            sistePubliserteKvartal
        )
            .kalkulerTrend()
        return maybeTrend.map { r: Trend ->
            r.tilAggregertHistorikkDto(
                bransjeEllerNæring.statistikkategori, bransjeEllerNæring.navn()
            )
        }
    }

    fun summerOppSisteFireKvartaler(
        statistikk: List<UmaskertSykefraværForEttKvartal>
    ): SumAvSykefraværOverFlereKvartaler {
        return ekstraherSisteFireKvartaler(statistikk)
            .map { SumAvSykefraværOverFlereKvartaler(it) }
            .reduceOrNull { it, other -> it.leggSammen(other) } ?: NULLPUNKT
    }

    private fun ekstraherSisteFireKvartaler(
        statistikk: List<UmaskertSykefraværForEttKvartal>
    ): List<UmaskertSykefraværForEttKvartal> {
        return statistikk
            .filter {
                (sistePubliserteKvartal inkludertTidligere 3).contains(it.årstallOgKvartal)
            }.sorted()
    }
}