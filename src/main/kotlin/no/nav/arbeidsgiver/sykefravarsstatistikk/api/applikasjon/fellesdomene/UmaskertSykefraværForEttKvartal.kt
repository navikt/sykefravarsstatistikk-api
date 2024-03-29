package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene

import arrow.core.Either
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.Statistikkfeil
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.SykefraværMedKategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.utils.StatistikkUtils
import java.math.BigDecimal
import java.math.RoundingMode

open class UmaskertSykefraværForEttKvartal(
    open val årstallOgKvartal: ÅrstallOgKvartal,
    dagsverkTeller: BigDecimal,
    dagsverkNevner: BigDecimal,
    open val antallPersoner: Int
) : Comparable<UmaskertSykefraværForEttKvartal> {

    val dagsverkTeller: BigDecimal = dagsverkTeller.setScale(1, RoundingMode.HALF_UP)
    val dagsverkNevner: BigDecimal = dagsverkNevner.setScale(1, RoundingMode.HALF_UP)

    constructor(statistikk: Sykefraværsstatistikk) : this(
        ÅrstallOgKvartal(statistikk.årstall, statistikk.kvartal),
        statistikk.tapteDagsverk!!,
        statistikk.muligeDagsverk!!,
        statistikk.antallPersoner
    )

    fun tilSykefraværMedKategori(kategori: Statistikkategori, kode: String): SykefraværMedKategori {
        return SykefraværMedKategori(
            statistikkategori = kategori,
            kode = kode,
            årstallOgKvartal = årstallOgKvartal,
            tapteDagsverk = dagsverkTeller,
            muligeDagsverk = dagsverkNevner,
            antallPersoner = antallPersoner
        )
    }

    fun kalkulerSykefraværsprosent(): Either<Statistikkfeil, BigDecimal> {
        return StatistikkUtils.kalkulerSykefraværsprosent(dagsverkTeller, dagsverkNevner)
    }

    override fun compareTo(other: UmaskertSykefraværForEttKvartal) =
        compareValuesBy(this, other) { it.årstallOgKvartal }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UmaskertSykefraværForEttKvartal

        if (dagsverkTeller != other.dagsverkTeller) return false
        if (dagsverkNevner != other.dagsverkNevner) return false
        if (antallPersoner != other.antallPersoner) return false
        if (årstallOgKvartal != other.årstallOgKvartal) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dagsverkTeller.hashCode()
        result = 31 * result + dagsverkNevner.hashCode()
        result = 31 * result + antallPersoner
        result = 31 * result + årstallOgKvartal.hashCode()
        return result
    }
}

