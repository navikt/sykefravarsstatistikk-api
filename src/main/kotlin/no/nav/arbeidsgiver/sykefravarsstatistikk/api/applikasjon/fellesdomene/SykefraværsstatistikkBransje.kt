package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene

import ia.felles.definisjoner.bransjer.Bransje
import java.math.BigDecimal


data class SykefraværsstatistikkBransje(
    override val årstall: Int,
    override val kvartal: Int,
    val bransje: Bransje,
    override val antallPersoner: Int,
    override val tapteDagsverk: BigDecimal,
    override val muligeDagsverk: BigDecimal,
) : Sykefraværsstatistikk