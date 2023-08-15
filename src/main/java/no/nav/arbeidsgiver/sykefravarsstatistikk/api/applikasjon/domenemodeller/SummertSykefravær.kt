package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import lombok.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.UmaskertSykefraværForEttKvartal
import java.math.BigDecimal
import java.util.stream.Collectors

@Setter
@Getter
class SummertSykefravær(
    tapteDagsverk: BigDecimal?,
    muligeDagsverk: BigDecimal?,
    maksAntallPersonerOverPerioden: Int,
    private val kvartaler: List<ÅrstallOgKvartal?>
) : MaskerbartSykefravær(tapteDagsverk, muligeDagsverk, maksAntallPersonerOverPerioden, !kvartaler.isEmpty()) {
    companion object {
        fun getSummertSykefravær(
            kvartalsvisSykefravær: List<UmaskertSykefraværForEttKvartal>
        ): SummertSykefravær {
            val totalTaptedagsverk: BigDecimal = kvartalsvisSykefravær.stream()
                .map<Any>(UmaskertSykefraværForEttKvartal::getDagsverkTeller)
                .reduce(BigDecimal(0), BigDecimal::add)
            val totalMuligedagsverk: BigDecimal = kvartalsvisSykefravær.stream()
                .map<Any>(UmaskertSykefraværForEttKvartal::getDagsverkNevner)
                .reduce(BigDecimal(0), BigDecimal::add)
            val maksAntallPersoner: Int = kvartalsvisSykefravær.stream()
                .map<Any> { e: UmaskertSykefraværForEttKvartal -> e.getAntallPersoner() }
                .max { x: Any?, y: Any? -> Integer.compare(x, y) }
                .orElse(0)
            return SummertSykefravær(
                totalTaptedagsverk,
                totalMuligedagsverk,
                maksAntallPersoner,
                kvartalsvisSykefravær.stream()
                    .map<Any?> { k: UmaskertSykefraværForEttKvartal -> k.getÅrstallOgKvartal() }
                    .collect(Collectors.toList<Any?>()))
        }
    }
}
