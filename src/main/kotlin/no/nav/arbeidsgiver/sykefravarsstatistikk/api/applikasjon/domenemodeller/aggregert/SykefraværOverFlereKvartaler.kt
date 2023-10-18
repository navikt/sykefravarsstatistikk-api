package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.aggregert

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.eksportering.SykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.utils.StatistikkUtils
import java.math.BigDecimal
import java.math.RoundingMode

data class SykefraværOverFlereKvartaler(
    val kvartaler: List<ÅrstallOgKvartal>,
    var tapteDagsverk: BigDecimal? = null,
    var muligeDagsverk: BigDecimal? = null,
    val sykefraværList: List<SykefraværForEttKvartal>
) {

    var prosent: BigDecimal? = null

    init {
        val erMaskert = sykefraværList.isNotEmpty() && sykefraværList.all { it.erMaskert }
        if (!erMaskert) {
            tapteDagsverk = tapteDagsverk?.setScale(1, RoundingMode.HALF_UP)
            muligeDagsverk = muligeDagsverk?.setScale(1, RoundingMode.HALF_UP)
            prosent = StatistikkUtils.kalkulerSykefraværsprosent(tapteDagsverk, muligeDagsverk).getOrNull()
        }
    }
}
