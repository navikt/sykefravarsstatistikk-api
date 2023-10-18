package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.eksportering

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.MaskerbartSykefravær
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.ÅrstallOgKvartal
import java.math.BigDecimal
import java.util.*

open class SykefraværForEttKvartal(
    @field:JsonIgnore val årstallOgKvartal: ÅrstallOgKvartal?,
    tapteDagsverk: BigDecimal?,
    muligeDagsverk: BigDecimal?,
    // TODO: trenger vi det?
    @field:JsonIgnore open val antallPersoner: Int
) : MaskerbartSykefravær(
    tapteDagsverk,
    muligeDagsverk,
    antallPersoner,
    årstallOgKvartal != null && tapteDagsverk != null && muligeDagsverk != null
), Comparable<SykefraværForEttKvartal> {

    val kvartal: Int
        get() = årstallOgKvartal?.kvartal ?: 0
    val Årstall: Int
        get() = årstallOgKvartal?.årstall ?: 0

    override fun compareTo(other: SykefraværForEttKvartal) = compareValuesBy(this, other) { it.årstallOgKvartal }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is SykefraværForEttKvartal) {
            return false
        }
        if (!super.equals(other)) {
            return false
        }
        val erÅrstallOgKvartalLike = årstallOgKvartal == other.årstallOgKvartal
        val erProsentLike = prosent == other.prosent
        val erTapteDagsverkLike = tapteDagsverk == other.tapteDagsverk
        val erMuligeDagsverkLike = muligeDagsverk == other.muligeDagsverk

        return erÅrstallOgKvartalLike && erProsentLike && erTapteDagsverkLike && erMuligeDagsverkLike
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), årstallOgKvartal)
    }
}
