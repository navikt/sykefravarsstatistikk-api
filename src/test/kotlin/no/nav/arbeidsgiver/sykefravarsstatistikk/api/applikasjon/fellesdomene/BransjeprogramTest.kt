package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ia.felles.definisjoner.bransjer.Bransje as Bransjer

internal class BransjeprogramTest {
    @Test
    fun `finnBransje returnerer tom Optional hvis næringskoden ikke er i bransjeprogrammet`() {
        val utenforBransjeprogrammet = Næringskode("11111")

        assertThat(Bransjeprogram.finnBransje(utenforBransjeprogrammet)).isEmpty
    }


    @Test
    fun `finnBransje returnerer BARNEHAGE for næringskode 88911`() {
        val næringskodenTilBarnehager = Næringskode("88911")

        val barnehagebransjen = Bransjeprogram.finnBransje(næringskodenTilBarnehager).get().type
        assertEquals(Bransjer.BARNEHAGER, barnehagebransjen)
    }

    @Test
    fun `finnBransje returnerer empty for næringskode 84300`() {
        val bransje = Bransjeprogram.finnBransje(Næringskode("84300"))
        assertThat(bransje).isEmpty
    }

    @Test
    fun `finnBransje returnerer NÆRINGSMIDDELINDUSTRI for næringskode 10320`() {
        val juicepressing = Næringskode("10320")

        val næringsmiddelbransjen = Bransjeprogram.finnBransje(juicepressing).get().type
        assertEquals(Bransjer.NÆRINGSMIDDELINDUSTRI, næringsmiddelbransjen)
    }
    @Test
    fun `bransje i næringsmiddelindustrien er definert på tosiffernivå`() {
        // En bedrift i næringsmiddelindustrien er i bransjeprogrammet, men data hentes likevel på
        // tosiffernivå, aka næringsnivå
        val næringINæringsmiddelindustriBransjen = Næringskode("10411")

        val actual = Bransjeprogram.finnBransje(næringINæringsmiddelindustriBransjen)

        assertThat(actual).isNotEmpty()
        assertThat(actual.get().identifikatorer).isEqualTo(listOf("10"))
    }
}