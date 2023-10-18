package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.eksportering

import ia.felles.definisjoner.bransjer.Bransjer
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.aggregert.Sykefraværsstatistikk
import java.math.BigDecimal

data class SykefraværsstatistikkBransje(
    override val årstall: Int,
    override val kvartal: Int,
    val bransje: Bransjer,
    override val antallPersoner: Int,
    override val tapteDagsverk: BigDecimal,
    override val muligeDagsverk: BigDecimal,
) : Sykefraværsstatistikk