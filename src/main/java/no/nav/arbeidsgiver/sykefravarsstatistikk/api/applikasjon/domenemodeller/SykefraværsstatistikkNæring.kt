package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import java.math.BigDecimal

data class SykefraværsstatistikkNæring(
    override val årstall: Int,
    override val kvartal: Int,
    val næringkode: String,
    override val antallPersoner: Int,
    override val tapteDagsverk: BigDecimal?, // Må være nullable fordi klassen brukes som en kafkamelding ...
    override val muligeDagsverk: BigDecimal?, // Må være nullable fordi klassen brukes som en kafkamelding ...
) : Sykefraværsstatistikk