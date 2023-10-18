package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.importering

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.aggregert.Sykefraværsstatistikk
import java.math.BigDecimal

data class SykefraværsstatistikkVirksomhet(
    override val årstall: Int = 0,
    override val kvartal: Int = 0,
    val orgnr: String? = null,
    val varighet: String? = null,
    val rectype: String? = null,
    override val antallPersoner: Int = 0,
    override val tapteDagsverk: BigDecimal,
    override val muligeDagsverk: BigDecimal
) : Sykefraværsstatistikk
