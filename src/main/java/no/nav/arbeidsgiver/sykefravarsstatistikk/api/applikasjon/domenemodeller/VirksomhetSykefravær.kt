package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.util.*

class VirksomhetSykefravær : SykefraværForEttKvartal {
    @JsonProperty("kategori")
    private val kategori: Statistikkategori
    val orgnr: String
    val navn: String
    override val antallPersoner: Int

    constructor(
        orgnr: String,
        navn: String,
        årstallOgKvartal: ÅrstallOgKvartal?,
        tapteDagsverk: BigDecimal?,
        mulige_dagsverk: BigDecimal?,
        antallPersoner: Int
    ) : super(årstallOgKvartal, tapteDagsverk, mulige_dagsverk, antallPersoner) {
        kategori = Statistikkategori.VIRKSOMHET
        this.orgnr = orgnr
        this.navn = navn
        this.antallPersoner = antallPersoner
    }

    @JsonCreator
    constructor(
        @JsonProperty("orgnr") orgnr: String,
        @JsonProperty("navn") navn: String,
        @JsonProperty("årstall") årstall: Int,
        @JsonProperty("kvartal") kvartal: Int,
        @JsonProperty("tapteDagsverk") tapteDagsverk: BigDecimal?,
        @JsonProperty("muligeDagsverk") muligeDagsverk: BigDecimal?,
        @JsonProperty("antallPersoner") antallPersoner: Int
    ) : super(ÅrstallOgKvartal(årstall, kvartal), tapteDagsverk, muligeDagsverk, antallPersoner) {
        kategori = Statistikkategori.VIRKSOMHET
        this.orgnr = orgnr
        this.navn = navn
        this.antallPersoner = antallPersoner
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is VirksomhetSykefravær) return false
        if (!super.equals(o)) return false
        val that = o
        return super.equals(that) && antallPersoner == that.antallPersoner && kategori == that.kategori && orgnr == that.orgnr && navn == that.navn
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), kategori, orgnr, navn, antallPersoner)
    }
}
