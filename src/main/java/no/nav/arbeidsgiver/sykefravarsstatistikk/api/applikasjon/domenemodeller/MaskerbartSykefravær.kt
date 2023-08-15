package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import lombok.EqualsAndHashCode
import lombok.Getter
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.utils.StatistikkUtils
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.Konstanter
import java.math.BigDecimal
import java.math.RoundingMode

@Getter
@EqualsAndHashCode
abstract class MaskerbartSykefravær(
    tapteDagsverk: BigDecimal?,
    muligeDagsverk: BigDecimal?,
    antallPersoner: Int,
    harSykefraværData: Boolean
) {
    var prosent: BigDecimal? = null
    val tapteDagsverk: BigDecimal? = null
    val muligeDagsverk: BigDecimal? = null
    val erMaskert: Boolean

    init {
        erMaskert = (harSykefraværData
                && antallPersoner < Konstanter.MIN_ANTALL_PERS_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER)
        if (!erMaskert && harSykefraværData) {
            prosent = StatistikkUtils.kalkulerSykefraværsprosent(tapteDagsverk!!, muligeDagsverk!!).getOrNull()
            this.tapteDagsverk = tapteDagsverk.setScale(1, RoundingMode.HALF_UP)
            this.muligeDagsverk = muligeDagsverk.setScale(1, RoundingMode.HALF_UP)
        } else {
            prosent = null
            this.tapteDagsverk = null
            this.muligeDagsverk = null
        }
    }
}
