package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.aggregert.Sykefraværsstatistikk
import java.math.BigDecimal

data class SykefraværsstatistikkLand(
    override val årstall: Int = 0,
    override val kvartal: Int = 0,
    override val antallPersoner: Int = 0,
    override val tapteDagsverk: BigDecimal,
    override val muligeDagsverk: BigDecimal
) : Sykefraværsstatistikk
