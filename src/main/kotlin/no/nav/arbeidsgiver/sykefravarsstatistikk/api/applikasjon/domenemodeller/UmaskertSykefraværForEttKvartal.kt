package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import arrow.core.Either
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregering.Statistikkfeil
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.utils.StatistikkUtils
import java.math.BigDecimal
import java.math.RoundingMode

open class UmaskertSykefraværForEttKvartal(
    open val årstallOgKvartal: ÅrstallOgKvartal,
    dagsverkTeller: BigDecimal,
    dagsverkNevner: BigDecimal,
    open val antallPersoner: Int
) : Comparable<UmaskertSykefraværForEttKvartal> {

    val dagsverkTeller: BigDecimal
    val dagsverkNevner: BigDecimal

    init {
        this.dagsverkTeller = dagsverkTeller.setScale(1, RoundingMode.HALF_UP)
        this.dagsverkNevner = dagsverkNevner.setScale(1, RoundingMode.HALF_UP)
    }

    constructor(statistikk: Sykefraværsstatistikk) : this(
        ÅrstallOgKvartal(statistikk.årstall, statistikk.kvartal),
        statistikk.tapteDagsverk!!,
        statistikk.muligeDagsverk!!,
        statistikk.antallPersoner
    )

    fun tilSykefraværMedKategori(kategori: Statistikkategori, kode: String): SykefraværMedKategori {
        return SykefraværMedKategori(
            kategori,
            kode,
            årstallOgKvartal,
            dagsverkTeller,
            dagsverkNevner,
            antallPersoner
        )
    }

    fun tilSykefraværsstatistikkLand(): SykefraværsstatistikkLand {
        return SykefraværsstatistikkLand(
            årstallOgKvartal.årstall,
            årstallOgKvartal.kvartal,
            antallPersoner,
            dagsverkTeller,
            dagsverkNevner
        )
    }

    val kvartal: Int
        get() = årstallOgKvartal.kvartal
    val Årstall: Int
        get() = årstallOgKvartal.årstall

    fun kalkulerSykefraværsprosent(): Either<Statistikkfeil, BigDecimal> {
        return StatistikkUtils.kalkulerSykefraværsprosent(dagsverkTeller, dagsverkNevner)
    }

    fun add(other: UmaskertSykefraværForEttKvartal): UmaskertSykefraværForEttKvartal {
        require(
            other.årstallOgKvartal == årstallOgKvartal
        ) { "Kan ikke summere kvartalsvis sykefravær med forskjellige kvartaler" }
        return UmaskertSykefraværForEttKvartal(
            årstallOgKvartal,
            dagsverkTeller.add(other.dagsverkTeller),
            dagsverkNevner.add(other.dagsverkNevner),
            antallPersoner + other.antallPersoner
        )
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

