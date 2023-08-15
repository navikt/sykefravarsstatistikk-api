package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import lombok.AllArgsConstructor
import lombok.Data
import java.math.BigDecimal

@Data
@AllArgsConstructor
class SykefraværsstatistikkVirksomhet : Sykefraværsstatistikk {
    private val årstall = 0
    private override val kvartal = 0
    private val orgnr: String? = null
    private val varighet: String? = null
    private val rectype: String? = null
    private override val antallPersoner = 0
    private override val tapteDagsverk: BigDecimal? = null
    private override val muligeDagsverk: BigDecimal? = null
}
