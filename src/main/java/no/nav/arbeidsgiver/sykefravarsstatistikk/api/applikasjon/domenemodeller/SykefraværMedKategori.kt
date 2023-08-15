package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.util.*

class SykefraværMedKategori : SykefraværForEttKvartal {
    val kategori: Statistikkategori
    @JvmField
    val kode: String
    override val antallPersoner: Int

    constructor(
        statistikkategori: Statistikkategori,
        kode: String,
        årstallOgKvartal: ÅrstallOgKvartal?,
        tapteDagsverk: BigDecimal?,
        muligeDagsverk: BigDecimal?,
        antallPersoner: Int
    ) : super(årstallOgKvartal, tapteDagsverk, muligeDagsverk, antallPersoner) {
        kategori = statistikkategori
        this.kode = kode
        this.antallPersoner = antallPersoner
    }

    constructor(
        statistikkategori: Statistikkategori, kode: String, sykefravær: UmaskertSykefraværForEttKvartal
    ) : super(
        sykefravær.getÅrstallOgKvartal(),
        sykefravær.getDagsverkTeller(),
        sykefravær.getDagsverkNevner(),
        sykefravær.getAntallPersoner()
    ) {
        kategori = statistikkategori
        this.kode = kode
        this.antallPersoner = sykefravær.getAntallPersoner()
    }

    // OBS: Constructor bruk i testene (objectMapper)
    @JsonCreator
    constructor(
        @JsonProperty("kategori") kategori: Statistikkategori,
        @JsonProperty("kode") kode: String,
        @JsonProperty("årstall") årstall: Int,
        @JsonProperty("kvartal") kvartal: Int,
        @JsonProperty("tapteDagsverk") @JsonFormat(shape = JsonFormat.Shape.STRING) tapteDagsverk: BigDecimal?,
        @JsonProperty("muligeDagsverk") muligeDagsverk: BigDecimal?,
        @JsonProperty("antallPersoner") antallPersoner: Int
    ) : super(ÅrstallOgKvartal(årstall, kvartal), tapteDagsverk, muligeDagsverk, antallPersoner) {
        this.kategori = kategori
        this.kode = kode
        this.antallPersoner = antallPersoner
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is SykefraværMedKategori) return false
        if (!super.equals(o)) return false
        val that = o
        return super.equals(that) && antallPersoner == that.antallPersoner && kategori == that.kategori && kode == that.kode
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), kategori, kode, antallPersoner)
    }

    companion object {
        @JvmStatic
        fun utenStatistikk(
            kategori: Statistikkategori, kode: String, årstallOgKvartal: ÅrstallOgKvartal?
        ): SykefraværMedKategori {
            return SykefraværMedKategori(kategori, kode, årstallOgKvartal, null, null, 0)
        }
    }
}
