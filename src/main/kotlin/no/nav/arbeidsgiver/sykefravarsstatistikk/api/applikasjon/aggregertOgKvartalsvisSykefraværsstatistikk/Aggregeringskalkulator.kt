package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk

import arrow.core.Either
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.SumAvSykefraværOverFlereKvartaler
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Sykefraværsdata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Trend
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.BransjeEllerNæring
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.UmaskertSykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal.Companion.sisteFireKvartaler

class Aggregeringskalkulator(
    private var sykefraværsdata: Sykefraværsdata,
    private var sistePubliserteKvartal: ÅrstallOgKvartal
) {
    fun fraværsprosentNorge(): Either<Statistikkfeil, StatistikkJson> {
        return summerOppSisteFireKvartaler(sykefraværsdata.filtrerPåKategori(Statistikkategori.LAND))
            .regnUtProsentOgMapTilDto(Statistikkategori.LAND, "Norge")
    }

    fun fraværsprosentBransjeEllerNæring(
        bransjeEllerNæring: BransjeEllerNæring
    ): Either<Statistikkfeil, StatistikkJson> {
        return summerOppSisteFireKvartaler(
            sykefraværsdata.filtrerPåKategori(bransjeEllerNæring.statistikkategori)
        )
            .regnUtProsentOgMapTilDto(
                bransjeEllerNæring.statistikkategori, bransjeEllerNæring.navn()
            )
    }

    fun tapteDagsverkVirksomhet(bedriftsnavn: String): Either<Statistikkfeil, StatistikkJson> {
        return summerOppSisteFireKvartaler(sykefraværsdata.filtrerPåKategori(Statistikkategori.VIRKSOMHET))
            .getTapteDagsverkOgMapTilDto(Statistikkategori.VIRKSOMHET, bedriftsnavn)
    }

    fun muligeDagsverkVirksomhet(bedriftsnavn: String): Either<Statistikkfeil, StatistikkJson> {
        return summerOppSisteFireKvartaler(sykefraværsdata.filtrerPåKategori(Statistikkategori.VIRKSOMHET))
            .getMuligeDagsverkOgMapTilDto(Statistikkategori.VIRKSOMHET, bedriftsnavn)
    }

    fun fraværsprosentVirksomhet(
        virksomhetsnavn: String
    ): Either<Statistikkfeil, StatistikkJson> {
        return summerOppSisteFireKvartaler(sykefraværsdata.filtrerPåKategori(Statistikkategori.VIRKSOMHET))
            .regnUtProsentOgMapTilDto(Statistikkategori.VIRKSOMHET, virksomhetsnavn)
    }

    fun trendBransjeEllerNæring(
        bransjeEllerNæring: BransjeEllerNæring
    ): Either<UtilstrekkeligData, StatistikkJson> {
        val maybeTrend = Trendkalkulator(
            sykefraværsdata.filtrerPåKategori(bransjeEllerNæring.statistikkategori),
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
        return ekstraherSisteFireKvartaler(statistikk).stream()
            .map { umaskertSykefravær: UmaskertSykefraværForEttKvartal? ->
                SumAvSykefraværOverFlereKvartaler(
                    umaskertSykefravær!!
                )
            }
            .reduce(SumAvSykefraværOverFlereKvartaler.NULLPUNKT) { obj: SumAvSykefraværOverFlereKvartaler, other: SumAvSykefraværOverFlereKvartaler? ->
                obj.leggSammen(
                    other!!
                )
            }
    }

    private fun ekstraherSisteFireKvartaler(
        statistikk: List<UmaskertSykefraværForEttKvartal>
    ): List<UmaskertSykefraværForEttKvartal> {
        return statistikk
            .filter {
                sisteFireKvartaler(sistePubliserteKvartal).contains(it.årstallOgKvartal)
            }.sorted()
    }
}