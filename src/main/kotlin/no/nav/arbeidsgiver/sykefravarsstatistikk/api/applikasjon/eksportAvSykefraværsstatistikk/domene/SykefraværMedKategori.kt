package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.UmaskertSykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
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
        sykefravær.årstallOgKvartal,
        sykefravær.dagsverkTeller,
        sykefravær.dagsverkNevner,
        sykefravær.antallPersoner
    ) {
        kategori = statistikkategori
        this.kode = kode
        this.antallPersoner = sykefravær.antallPersoner
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SykefraværMedKategori) return false
        if (!super.equals(other)) return false
        return (antallPersoner == other.antallPersoner) && (kategori == other.kategori) && (kode == other.kode)
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), kategori, kode, antallPersoner)
    }

    companion object {

        fun utenStatistikk(
            kategori: Statistikkategori, kode: String, årstallOgKvartal: ÅrstallOgKvartal?
        ): SykefraværMedKategori {
            return SykefraværMedKategori(kategori, kode, årstallOgKvartal, null, null, 0)
        }
    }
}
