package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene

import java.math.BigDecimal

data class SykefraværsstatistikkSektor(
    override val årstall: Int = 0,
    override val kvartal: Int = 0,
    val sektorkode: String,
    override val antallPersoner: Int = 0,
    override val tapteDagsverk: BigDecimal?, // Må være nullable fordi klassen brukes som en kafkamelding ...
    override val muligeDagsverk: BigDecimal? // Må være nullable fordi klassen brukes som en kafkamelding ...
) : Sykefraværsstatistikk
