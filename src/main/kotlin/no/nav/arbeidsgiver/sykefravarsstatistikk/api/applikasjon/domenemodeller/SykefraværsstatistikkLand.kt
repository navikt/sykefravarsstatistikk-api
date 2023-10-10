package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import java.math.BigDecimal

data class SykefraværsstatistikkLand(
    override val årstall: Int = 0,
    override val kvartal: Int = 0,
    override val antallPersoner: Int = 0,
    override val tapteDagsverk: BigDecimal,
    override val muligeDagsverk: BigDecimal
) : Sykefraværsstatistikk
