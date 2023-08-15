package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import java.math.BigDecimal

data class SykefraværsstatistikkNæringMedVarighet(
    override val årstall: Int = 0,
    override val kvartal: Int = 0,
    val næringkode: String? = null,
    val varighet: String? = null,
    override val antallPersoner: Int = 0,
    override val tapteDagsverk: BigDecimal,
    override val muligeDagsverk: BigDecimal,
) : Sykefraværsstatistikk

