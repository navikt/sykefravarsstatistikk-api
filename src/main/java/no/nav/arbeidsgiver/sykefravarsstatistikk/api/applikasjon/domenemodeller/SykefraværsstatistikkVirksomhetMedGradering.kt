package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import lombok.AllArgsConstructor
import lombok.Data
import java.math.BigDecimal

@Data
@AllArgsConstructor
class SykefraværsstatistikkVirksomhetMedGradering : Sykefraværsstatistikk {
    private val årstall = 0
    private override val kvartal = 0
    private val orgnr: String? = null
    private val næring: String? = null
    private val næringkode: String? = null
    private val rectype: String? = null
    private val antallGraderteSykemeldinger = 0
    private val tapteDagsverkGradertSykemelding: BigDecimal? = null
    private val antallSykemeldinger = 0
    private override val antallPersoner = 0
    private override val tapteDagsverk: BigDecimal? = null
    private override val muligeDagsverk: BigDecimal? = null
}
