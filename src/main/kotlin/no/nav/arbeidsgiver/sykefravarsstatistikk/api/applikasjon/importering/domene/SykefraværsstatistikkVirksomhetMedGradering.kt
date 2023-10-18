package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importering.domene

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Sykefraværsstatistikk
import java.math.BigDecimal

data class SykefraværsstatistikkVirksomhetMedGradering(
    override val årstall: Int = 0,
    override val kvartal: Int = 0,
    val orgnr: String? = null,
    val næring: String? = null,
    val næringkode: String? = null,
    val rectype: String? = null,
    val antallGraderteSykemeldinger: Int = 0,
    val tapteDagsverkGradertSykemelding: BigDecimal? = null,
    val antallSykemeldinger: Int = 0,
    override val antallPersoner: Int = 0,
    override val tapteDagsverk: BigDecimal,
    override val muligeDagsverk: BigDecimal
) : Sykefraværsstatistikk
