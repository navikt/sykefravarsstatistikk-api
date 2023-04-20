package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.ArbeidsmiljøportalenBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.Sykefraværsstatistikk
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