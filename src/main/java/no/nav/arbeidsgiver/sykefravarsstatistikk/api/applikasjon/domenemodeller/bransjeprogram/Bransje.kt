package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram

import lombok.Data
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Næringskode5Siffer
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.UnderenhetLegacy
import java.util.*

@Data
class Bransje(
    val type: ArbeidsmiljøportalenBransje, val navn: String, vararg koderSomSpesifisererNæringer: String?
) {
    val koderSomSpesifisererNæringer: List<String>

    init {
        this.koderSomSpesifisererNæringer = Arrays.asList(*koderSomSpesifisererNæringer)
        validerKoder()
    }

    private fun validerKoder() {
        if (inneholderKunKoderMedGittAntallSifre(2) || inneholderKunKoderMedGittAntallSifre(5)) {
            return
        }
        throw IllegalArgumentException(
            "Støtter kun bransjer som er spesifisert av enten 2 eller 5 sifre"
        )
    }

    private fun inneholderKunKoderMedGittAntallSifre(antallSifre: Int): Boolean {
        return koderSomSpesifisererNæringer.stream().allMatch { kode: String -> kode.length == antallSifre }
    }

    fun erDefinertPåTosiffernivå(): Boolean {
        return inneholderKunKoderMedGittAntallSifre(2)
    }

    fun erDefinertPåFemsiffernivå(): Boolean {
        return inneholderKunKoderMedGittAntallSifre(5)
    }

    fun inkludererVirksomhet(underenhet: UnderenhetLegacy): Boolean {
        return inkludererNæringskode(underenhet.næringskode)
    }

    fun inkludererNæringskode(næringskode5Siffer: Næringskode5Siffer?): Boolean {
        val næringskode = næringskode5Siffer!!.kode
        return koderSomSpesifisererNæringer.stream().anyMatch { prefix: String? ->
            næringskode.startsWith(
                prefix!!
            )
        }
    }

    fun inkludererNæringskode(næringskode5Siffer: String?): Boolean {
        return if (næringskode5Siffer == null) {
            false
        } else koderSomSpesifisererNæringer.stream().anyMatch { prefix: String? ->
            næringskode5Siffer.startsWith(
                prefix!!
            )
        }
    }
}
