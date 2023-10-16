package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.utils.StatistikkUtils.kalkulerSykefraværsprosent
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.Konstanter
import java.math.BigDecimal
import java.util.function.Predicate
import java.util.stream.Collectors

data class SykefraværFlereKvartalerForEksport(
    private val umaskertSykefravær: List<UmaskertSykefraværForEttKvartal>
) {
    var tapteDagsverk: BigDecimal? = null
    var muligeDagsverk: BigDecimal? = null
    var prosent: BigDecimal? = null
    var antallPersoner: Int
    var kvartaler: List<ÅrstallOgKvartal>
    val erMaskert: Boolean

    init {
        erMaskert = (!umaskertSykefravær.isEmpty()
                && umaskertSykefravær.stream()
            .allMatch(
                Predicate { v: UmaskertSykefraværForEttKvartal ->
                    v.antallPersoner < Konstanter.MIN_ANTALL_PERS_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER
                }))
        if (!erMaskert && !umaskertSykefravær.isEmpty()) {
            tapteDagsverk = umaskertSykefravær.stream()
                .map(UmaskertSykefraværForEttKvartal::dagsverkTeller)
                .reduce(BigDecimal.ZERO) { obj: BigDecimal, augend: BigDecimal? -> obj.add(augend) }
            muligeDagsverk = umaskertSykefravær.stream()
                .map(UmaskertSykefraværForEttKvartal::dagsverkNevner)
                .reduce(BigDecimal.ZERO) { obj: BigDecimal, augend: BigDecimal? -> obj.add(augend) }
            prosent = kalkulerSykefraværsprosent(tapteDagsverk, muligeDagsverk)
                .getOrNull()
        } else {
            tapteDagsverk = null
            muligeDagsverk = null
            prosent = null
        }
        kvartaler = umaskertSykefravær.stream()
            .map(UmaskertSykefraværForEttKvartal::årstallOgKvartal)
            .collect(Collectors.toList())
        antallPersoner = if (umaskertSykefravær.isEmpty()) 0 else umaskertSykefravær.stream()
            .max(Comparator.comparing(UmaskertSykefraværForEttKvartal::årstallOgKvartal))
            .get()
            .antallPersoner
    }

    companion object {

        fun utenStatistikk(): SykefraværFlereKvartalerForEksport {
            return SykefraværFlereKvartalerForEksport(listOf())
        }
    }
}
