package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import lombok.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.utils.StatistikkUtils
import java.math.BigDecimal
import java.math.RoundingMode

@Getter
@EqualsAndHashCode
abstract class MaskerbartSykefraværOverFlereKvartaler(
    tapteDagsverk: BigDecimal,
    muligeDagsverk: BigDecimal,
    sykefraværForEttKvartalList: List<SykefraværForEttKvartal?>,
    harSykefraværData: Boolean
) {
    private var prosent: BigDecimal? = null
    private val tapteDagsverk: BigDecimal? = null
    private val muligeDagsverk: BigDecimal? = null
    private val erMaskert: Boolean

    init {
        erMaskert = (harSykefraværData
                && sykefraværForEttKvartalList.stream().allMatch(MaskerbartSykefravær::isErMaskert))
        if (!erMaskert && harSykefraværData) {
            prosent = StatistikkUtils.kalkulerSykefraværsprosent(tapteDagsverk, muligeDagsverk).getOrNull()
            this.tapteDagsverk = tapteDagsverk.setScale(1, RoundingMode.HALF_UP)
            this.muligeDagsverk = muligeDagsverk.setScale(1, RoundingMode.HALF_UP)
        } else {
            prosent = null
            this.tapteDagsverk = null
            this.muligeDagsverk = null
        }
    }
}
