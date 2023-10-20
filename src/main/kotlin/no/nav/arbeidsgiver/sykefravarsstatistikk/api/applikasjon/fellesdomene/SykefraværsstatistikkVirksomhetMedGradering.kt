package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene

import java.math.BigDecimal

data class SykefraværsstatistikkVirksomhetMedGradering(
    override val årstall: Int,
    override val kvartal: Int,
    val orgnr: String,
    val næring: String,
    val næringkode: String,
    val rectype: String,
    val antallGraderteSykemeldinger: Int,
    val tapteDagsverkGradertSykemelding: BigDecimal,
    val antallSykemeldinger: Int,
    override val antallPersoner: Int,
    override val tapteDagsverk: BigDecimal,
    override val muligeDagsverk: BigDecimal
) : Sykefraværsstatistikk
