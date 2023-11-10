package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.Statistikkfeil
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.UtilstrekkeligData
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.UmaskertSykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.StatistikkJson
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.utils.StatistikkUtils
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.Konstanter
import java.math.BigDecimal
import java.util.function.Supplier
import kotlin.math.max


data class SumAvSykefraværOverFlereKvartaler(
    var muligeDagsverk: BigDecimal,
    var tapteDagsverk: BigDecimal,
    private val høyesteAntallPersonerIEtKvartal: Int,
    private val kvartaler: List<ÅrstallOgKvartal>,
    private val umaskertSykefraværList: List<UmaskertSykefraværForEttKvartal>
) {

    constructor(umaskertSykefravær: UmaskertSykefraværForEttKvartal) : this(
        muligeDagsverk = umaskertSykefravær.dagsverkNevner,
        tapteDagsverk = umaskertSykefravær.dagsverkTeller,
        høyesteAntallPersonerIEtKvartal = umaskertSykefravær.antallPersoner,
        kvartaler = listOf(umaskertSykefravær.årstallOgKvartal),
        umaskertSykefraværList = listOf(umaskertSykefravær)
    )


    fun regnUtProsentOgMapTilDto(
        type: Statistikkategori, label: String
    ): Either<Statistikkfeil, StatistikkJson> {
        return kalkulerFraværsprosentMedMaskering()
            .map { prosent: BigDecimal -> tilStatistikkDto(type = type, label = label, verdi = prosent.toString()) }
    }

    fun regnUtProsentOgMapTilSykefraværForFlereKvartaler(): Either<Statistikkfeil, SykefraværOverFlereKvartaler> {
        if (muligeDagsverk.compareTo(BigDecimal.ZERO) == 0) {
            return UtilstrekkeligData(
                "Kan ikke regne ut sykefraværsprosent når antall mulige dagsverk er null."
            ).left()
        }
        StatistikkUtils.kalkulerSykefraværsprosent(tapteDagsverk, muligeDagsverk)
            .getOrElse { return it.left() }

        val sykefraværForFlereKvartaler = SykefraværOverFlereKvartaler(
            kvartaler = kvartaler,
            tapteDagsverk = tapteDagsverk,
            muligeDagsverk = muligeDagsverk,
            sykefraværList = umaskertSykefraværList.map {
                SykefraværForEttKvartal(
                    årstallOgKvartal = it.årstallOgKvartal,
                    tapteDagsverk = it.dagsverkTeller,
                    muligeDagsverk = it.dagsverkNevner,
                    antallPersoner = it.antallPersoner
                )
            }.toList()
        )
        return sykefraværForFlereKvartaler.right()
    }

    private fun kalkulerFraværsprosentMedMaskering(): Either<Statistikkfeil, BigDecimal> {
        if (datagrunnlagetErTomt()) {
            return UtilstrekkeligData().left()
        }
        if (dataMåMaskeres()) {
            return MaskertDataFeil().left()
        }
        return if (muligeDagsverk.compareTo(BigDecimal.ZERO) == 0) {
            UtilstrekkeligData(
                "Kan ikke regne ut sykefraværsprosent når antall mulige dagsverk er null."
            ).left()
        } else StatistikkUtils.kalkulerSykefraværsprosent(tapteDagsverk, muligeDagsverk)
    }

    fun getTapteDagsverkOgMapTilDto(
        type: Statistikkategori, virksomhetsnavn: String
    ): Either<Statistikkfeil, StatistikkJson> {
        return getAntallDagsverkOgMapTilDto(type, virksomhetsnavn, this::tapteDagsverk)
    }

    fun getMuligeDagsverkOgMapTilDto(
        type: Statistikkategori, virksomhetsnavn: String
    ): Either<Statistikkfeil, StatistikkJson> {
        return getAntallDagsverkOgMapTilDto(type, virksomhetsnavn, this::muligeDagsverk)
    }

    fun leggSammen(other: SumAvSykefraværOverFlereKvartaler): SumAvSykefraværOverFlereKvartaler {
        return SumAvSykefraværOverFlereKvartaler(
            muligeDagsverk.add(other.muligeDagsverk),
            tapteDagsverk.add(other.tapteDagsverk),
            max(høyesteAntallPersonerIEtKvartal, other.høyesteAntallPersonerIEtKvartal),
            (kvartaler + other.kvartaler).distinct(),
            (umaskertSykefraværList + other.umaskertSykefraværList).distinct()
        )
    }

    private fun getAntallDagsverkOgMapTilDto(
        type: Statistikkategori,
        virksomhetsnavn: String,
        tapteEllerMuligeDagsverk: Supplier<BigDecimal>
    ): Either<Statistikkfeil, StatistikkJson> {
        if (datagrunnlagetErTomt()) {
            return UtilstrekkeligData().left()
        }
        return if (dataMåMaskeres()) {
            MaskertDataFeil().left()
        } else tilStatistikkDto(type, virksomhetsnavn, tapteEllerMuligeDagsverk.get().toString()).right()
    }

    private fun datagrunnlagetErTomt(): Boolean {
        return this == NULLPUNKT
    }

    private fun dataMåMaskeres(): Boolean {
        return (høyesteAntallPersonerIEtKvartal
                < Konstanter.MIN_ANTALL_PERS_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER)
    }

    private fun tilStatistikkDto(type: Statistikkategori, label: String, verdi: String): StatistikkJson {
        return StatistikkJson(type, label, verdi, høyesteAntallPersonerIEtKvartal, kvartaler)
    }

    class MaskertDataFeil : Statistikkfeil("Ikke nok personer i datagrunnlaget - data maskeres.")

    companion object {
        var NULLPUNKT =
            SumAvSykefraværOverFlereKvartaler(BigDecimal.ZERO, BigDecimal.ZERO, 0, emptyList(), emptyList())
    }
}
