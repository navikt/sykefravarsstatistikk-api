package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene

import ia.felles.definisjoner.bransjer.Bransjer
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Sykefraværsstatistikk
import java.math.BigDecimal

data class SykefraværsstatistikkBransje(
    override val årstall: Int,
    override val kvartal: Int,
    val bransje: Bransjer,
    override val antallPersoner: Int,
    override val tapteDagsverk: BigDecimal,
    override val muligeDagsverk: BigDecimal,
) : Sykefraværsstatistikk