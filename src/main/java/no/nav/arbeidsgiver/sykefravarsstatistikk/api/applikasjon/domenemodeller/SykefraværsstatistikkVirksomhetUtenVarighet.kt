package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import lombok.AllArgsConstructor
import lombok.Data
import java.math.BigDecimal

@Data
@AllArgsConstructor // TODO: rename SykefraværsstatistikkVirksomhet til SykefraværsstatistikkVirksomhetMedVarighet og
// denne til SykefraværsstatistikkVirksomhet

class SykefraværsstatistikkVirksomhetUtenVarighet : Sykefraværsstatistikk {
    private val årstall = 0
    private override val kvartal = 0

    // Kotlin kjenner ikke til @Data annotation (Lombok)
    @JvmField
    val orgnr: String? = null
    private override val antallPersoner = 0
    private override val tapteDagsverk: BigDecimal? = null
    private override val muligeDagsverk: BigDecimal? = null
}
