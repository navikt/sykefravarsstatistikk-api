package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import java.math.BigDecimal

interface Sykefraværsstatistikk {
    @JvmField
    val Årstall: Int
    @JvmField
    val kvartal: Int
    @JvmField
    val antallPersoner: Int
    @JvmField
    val tapteDagsverk: BigDecimal?
    @JvmField
    val muligeDagsverk: BigDecimal?
}
