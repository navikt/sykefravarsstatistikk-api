package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import lombok.AllArgsConstructor
import lombok.Data
import lombok.EqualsAndHashCode
import java.math.BigDecimal

@Data
@AllArgsConstructor
@EqualsAndHashCode
class SykefraværsstatistikkLand : Sykefraværsstatistikk {
    private val årstall = 0
    private override val kvartal = 0
    private override val antallPersoner = 0
    private override val tapteDagsverk: BigDecimal? = null
    private override val muligeDagsverk: BigDecimal? = null
}
