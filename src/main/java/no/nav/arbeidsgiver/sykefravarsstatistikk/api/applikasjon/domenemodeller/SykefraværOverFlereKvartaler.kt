package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import java.math.BigDecimal
import java.util.*

class SykefraværOverFlereKvartaler(
    @JvmField val kvartaler: List<ÅrstallOgKvartal?>,
    tapteDagsverk: BigDecimal,
    muligeDagsverk: BigDecimal,
    sykefraværList: List<SykefraværForEttKvartal?>
) : MaskerbartSykefraværOverFlereKvartaler(tapteDagsverk, muligeDagsverk, sykefraværList, kvartaler.size != 0) {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is SykefraværOverFlereKvartaler) return false
        if (!super.equals(o)) return false
        val that = o
        return (kvartaler == that.kvartaler && getProsent().equals(that.getProsent())
                && getTapteDagsverk().equals(that.getTapteDagsverk())
                && getMuligeDagsverk().equals(that.getMuligeDagsverk()))
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), kvartaler)
    }
}
