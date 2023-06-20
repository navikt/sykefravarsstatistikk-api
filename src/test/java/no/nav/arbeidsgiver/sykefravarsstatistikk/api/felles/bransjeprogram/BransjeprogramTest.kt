package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.ArbeidsmiljøportalenBransje.BARNEHAGER
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.ArbeidsmiljøportalenBransje.NÆRINGSMIDDELINDUSTRI
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class BransjeprogramTest {

    @Test
    fun `finnBransje returnerer tom Optional hvis input er null`() {
        val næringskode: Næringskode5Siffer? = null

        assertThat(Bransjeprogram.finnBransje(næringskode)).isEmpty
    }

    @Test
    fun `finnBransje returnerer tom Optional hvis næringskoden ikke er i bransjeprogrammet`() {
        val næringskodeUtenforBransjeprogrammet = "11111"

        assertThat(Bransjeprogram.finnBransje(næringskodeUtenforBransjeprogrammet)).isEmpty
    }


    @Test
    fun `finnBransje returnerer BARNEHAGE for næringskode 88911` () {
        val næringskodenTilBarnehager = Næringskode5Siffer("88911", "Barnehager")

        val barnehagebransjen = Bransjeprogram.finnBransje(næringskodenTilBarnehager).get().type
        assertEquals(BARNEHAGER, barnehagebransjen)
    }

    @Test
    fun `finnBransje returnerer empty for næringskode 84300` () {
        val næringskode = Næringskode5Siffer("84300", "")

        val bransje = Bransjeprogram.finnBransje(næringskode)
        assertThat(bransje).isEmpty
    }

    @Test
    fun `finnBransje returnerer empty for en tom string` () {
        val bransje = Bransjeprogram.finnBransje("")
        assertThat(bransje).isEmpty
    }

    @Test
    fun `finnBransje returnerer NÆRINGSMIDDELINDUSTRI for næringskode 10320`() {
        val juicepressing = Næringskode5Siffer("10320", "Produksjon av juice av frukt og grønnsaker")

        val næringsmiddelbransjen = Bransjeprogram.finnBransje(juicepressing).get().type
        assertEquals(NÆRINGSMIDDELINDUSTRI, næringsmiddelbransjen)
    }
}