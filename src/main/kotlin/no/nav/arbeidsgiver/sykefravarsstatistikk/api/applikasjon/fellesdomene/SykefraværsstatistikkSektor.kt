package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene

import java.math.BigDecimal

data class SykefraværsstatistikkSektor(
    override val årstall: Int,
    override val kvartal: Int,
    val sektorkode: String,
    override val antallPersoner: Int,
    override val tapteDagsverk: BigDecimal?, // Må være nullable fordi klassen brukes som en kafkamelding ...
    override val muligeDagsverk: BigDecimal? // Må være nullable fordi klassen brukes som en kafkamelding ...
) : Sykefraværsstatistikk
