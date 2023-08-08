package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import java.math.BigDecimal

data class SykefraværsstatistikkNæring(
    private val årstall: Int,
    private val kvartal: Int,
    val næringkode: String,
    private val antallPersoner: Int,
    private val tapteDagsverk: BigDecimal?,
    private val muligeDagsverk: BigDecimal?
) : Sykefraværsstatistikk {
    override fun getÅrstall(): Int {
        return årstall
    }

    override fun getKvartal(): Int {
        return kvartal
    }

    override fun getAntallPersoner(): Int {
        return antallPersoner
    }

    override fun getTapteDagsverk(): BigDecimal? {
        return tapteDagsverk
    }

    override fun getMuligeDagsverk(): BigDecimal? {
        return muligeDagsverk
    }
}