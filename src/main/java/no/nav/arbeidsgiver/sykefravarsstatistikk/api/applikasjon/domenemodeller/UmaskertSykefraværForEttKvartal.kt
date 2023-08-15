package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import io.vavr.control.Either
import lombok.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.UmaskertSykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.exceptions.StatistikkException
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.utils.StatistikkUtils
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

@Data
open class UmaskertSykefraværForEttKvartal : Comparable<UmaskertSykefraværForEttKvartal> {
    val dagsverkTeller: BigDecimal
    val dagsverkNevner: BigDecimal
    val antallPersoner: Int
    val årstallOgKvartal: ÅrstallOgKvartal?

    constructor(
        årstallOgKvartal: ÅrstallOgKvartal?,
        dagsverkTeller: BigDecimal?,
        dagsverkNevner: BigDecimal?,
        antallPersoner: Int
    ) {
        this.årstallOgKvartal = årstallOgKvartal
        this.dagsverkTeller = dagsverkTeller!!.setScale(1, RoundingMode.HALF_UP)
        this.dagsverkNevner = dagsverkNevner!!.setScale(1, RoundingMode.HALF_UP)
        this.antallPersoner = antallPersoner
    }

    constructor(statistikk: Sykefraværsstatistikk) : this(
        ÅrstallOgKvartal(statistikk.getÅrstall(), statistikk.getKvartal()),
        statistikk.getTapteDagsverk(),
        statistikk.getMuligeDagsverk(),
        statistikk.getAntallPersoner()
    )

    constructor(
        kvartal: ÅrstallOgKvartal?, dagsverkTeller: Int, dagsverkNevner: Int, antallPersoner: Int
    ) {
        årstallOgKvartal = kvartal
        this.dagsverkTeller = BigDecimal(dagsverkTeller.toString())
        this.dagsverkNevner = BigDecimal(dagsverkNevner.toString())
        this.antallPersoner = antallPersoner
    }

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
            årstallOgKvartal!!.årstall,
            årstallOgKvartal.kvartal,
            antallPersoner,
            dagsverkTeller,
            dagsverkNevner
        )
    }

    val kvartal: Int
        get() = årstallOgKvartal?.kvartal ?: 0
    val Årstall: Int
        get() = årstallOgKvartal?.årstall ?: 0

    fun kalkulerSykefraværsprosent(): Either<StatistikkException, BigDecimal> {
        return StatistikkUtils.kalkulerSykefraværsprosent(dagsverkTeller, dagsverkNevner)
    }

    fun add(other: UmaskertSykefraværForEttKvartal): UmaskertSykefraværForEttKvartal {
        require(
            other.getÅrstallOgKvartal().equals(årstallOgKvartal)
        ) { "Kan ikke summere kvartalsvis sykefravær med forskjellige kvartaler" }
        return UmaskertSykefraværForEttKvartal(
            årstallOgKvartal,
            dagsverkTeller.add(other.getDagsverkTeller()),
            dagsverkNevner.add(other.getDagsverkNevner()),
            antallPersoner + other.getAntallPersoner()
        )
    }

    override fun compareTo(kvartalsvisSykefravær: UmaskertSykefraværForEttKvartal): Int {
        return Comparator.comparing<Any, Any>(UmaskertSykefraværForEttKvartal::getÅrstallOgKvartal)
            .compare(this, kvartalsvisSykefravær)
    }

    companion object {
        fun hentUtKvartal(
            sykefravær: Collection<UmaskertSykefraværForEttKvartal>?, kvartal: ÅrstallOgKvartal
        ): Optional<UmaskertSykefraværForEttKvartal> {
            return if (sykefravær == null) Optional.empty<UmaskertSykefraværForEttKvartal>() else sykefravær.stream()
                .filter { datapunkt: UmaskertSykefraværForEttKvartal ->
                    datapunkt.getÅrstallOgKvartal().equals(kvartal)
                }
                .findAny()
        }
    }
}
