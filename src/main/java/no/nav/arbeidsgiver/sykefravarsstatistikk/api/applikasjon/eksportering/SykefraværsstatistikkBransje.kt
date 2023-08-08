package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.ArbeidsmiljøportalenBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Sykefraværsstatistikk
import java.math.BigDecimal

data class SykefraværsstatistikkBransje(
    private val årstall: Int,
    private val kvartal: Int,
    val bransje: ArbeidsmiljøportalenBransje,
    private val antallPersoner: Int,
    private val tapteDagsverk: BigDecimal,
    private val muligeDagsverk: BigDecimal,
) : Sykefraværsstatistikk {
    override fun getÅrstall(): Int = årstall

    override fun getKvartal(): Int = kvartal

    override fun getAntallPersoner(): Int = antallPersoner

    override fun getTapteDagsverk(): BigDecimal = tapteDagsverk

    override fun getMuligeDagsverk(): BigDecimal = muligeDagsverk

}