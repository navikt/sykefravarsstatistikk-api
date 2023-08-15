package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import com.fasterxml.jackson.annotation.JsonIgnore
import java.math.BigDecimal
import java.util.*
import java.util.function.Function

open class SykefraværForEttKvartal(
    @field:JsonIgnore val ÅrstallOgKvartal: ÅrstallOgKvartal?,
    tapteDagsverk: BigDecimal?,
    muligeDagsverk: BigDecimal?,
    // TODO: trenger vi det?
    @field:JsonIgnore val antallPersoner: Int
) : MaskerbartSykefravær(
    tapteDagsverk,
    muligeDagsverk,
    antallPersoner,
    ÅrstallOgKvartal != null && tapteDagsverk != null && muligeDagsverk != null
), Comparable<SykefraværForEttKvartal> {

    val kvartal: Int
        get() = ÅrstallOgKvartal?.kvartal ?: 0
    val Årstall: Int
        get() = ÅrstallOgKvartal?.årstall ?: 0

    override fun compareTo(other: SykefraværForEttKvartal): Int {
        return Comparator.comparing(Function { obj: SykefraværForEttKvartal -> obj.ÅrstallOgKvartal })
            .compare(this, other)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o !is SykefraværForEttKvartal) {
            return false
        }
        if (!super.equals(o)) {
            return false
        }
        val that = o
        val erÅrstallOgKvartalLike = ÅrstallOgKvartal == that.ÅrstallOgKvartal
        val erProsentLike =
            if (this.getProsent() == null) that.getProsent() == null else this.getProsent().equals(that.getProsent())
        val erTapteDagsverkLike =
            if (this.getTapteDagsverk() == null) that.getTapteDagsverk() == null else this.getTapteDagsverk()
                .equals(that.getTapteDagsverk())
        val erMuligeDagsverkLike =
            if (this.getMuligeDagsverk() == null) that.getMuligeDagsverk() == null else this.getMuligeDagsverk()
                .equals(that.getMuligeDagsverk())
        return erÅrstallOgKvartalLike && erProsentLike && erTapteDagsverkLike && erMuligeDagsverkLike
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), ÅrstallOgKvartal)
    }
}
