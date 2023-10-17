package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.utils.StatistikkUtils.kalkulerSykefraværsprosent
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.Konstanter.MIN_ANTALL_PERS_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER
import java.math.BigDecimal

data class SykefraværFlereKvartalerForEksport(
    private val umaskertSykefravær: List<UmaskertSykefraværForEttKvartal>
) {
    var tapteDagsverk: BigDecimal? = null
    var muligeDagsverk: BigDecimal? = null
    var prosent: BigDecimal? = null
    var antallPersoner: Int
    var kvartaler: List<ÅrstallOgKvartal>
    val erMaskert =
        umaskertSykefravær.isNotEmpty() && umaskertSykefravær.all {
            it.antallPersoner < MIN_ANTALL_PERS_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER
        }

    init {
        if (!erMaskert && umaskertSykefravær.isNotEmpty()) {
            tapteDagsverk = umaskertSykefravær.sumOf { it.dagsverkTeller }
            muligeDagsverk = umaskertSykefravær.sumOf { it.dagsverkNevner }
            prosent = kalkulerSykefraværsprosent(tapteDagsverk, muligeDagsverk).getOrNull()
        } else {
            tapteDagsverk = null
            muligeDagsverk = null
            prosent = null
        }
        kvartaler = umaskertSykefravær
            .map(UmaskertSykefraværForEttKvartal::årstallOgKvartal)
            .toList()
        antallPersoner =
            if (umaskertSykefravær.isEmpty()) 0
            else umaskertSykefravær.maxByOrNull { it.årstallOgKvartal }!!.antallPersoner
    }

    companion object {

        fun utenStatistikk(): SykefraværFlereKvartalerForEksport {
            return SykefraværFlereKvartalerForEksport(listOf())
        }
    }
}
