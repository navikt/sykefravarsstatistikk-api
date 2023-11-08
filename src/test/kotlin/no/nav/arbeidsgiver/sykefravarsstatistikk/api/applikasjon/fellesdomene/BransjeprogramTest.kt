package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene

import ia.felles.definisjoner.bransjer.BransjeId
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import ia.felles.definisjoner.bransjer.Bransje
internal class BransjeprogramTest {
    @Test
    fun `finnBransje returnerer null hvis næringskoden ikke er i bransjeprogrammet`() {
        val utenforBransjeprogrammet = Næringskode("11111")

        Bransjeprogram.finnBransje(utenforBransjeprogrammet) shouldBe null
    }


    @Test
    fun `finnBransje returnerer BARNEHAGE for næringskode 88911`() {
        val næringskodenTilBarnehager = Næringskode("88911")

        Bransjeprogram.finnBransje(næringskodenTilBarnehager) shouldBe Bransje.BARNEHAGER
    }

    @Test
    fun `finnBransje returnerer null for næringskode 84300`() {
        Bransjeprogram.finnBransje(Næringskode("84300")) shouldBe null
    }

    @Test
    fun `finnBransje returnerer NÆRINGSMIDDELINDUSTRI for næringskode 10320`() {
        val juicepressing = Næringskode("10320")

        Bransjeprogram.finnBransje(juicepressing) shouldBe Bransje.NÆRINGSMIDDELINDUSTRI
    }

    @Test
    fun `bransje i næringsmiddelindustrien er definert på tosiffernivå`() {
        // En bedrift i næringsmiddelindustrien er i bransjeprogrammet, men data hentes likevel på
        // tosiffernivå, aka næringsnivå
        val næringINæringsmiddelindustriBransjen = Næringskode("10411")

        val actual = Bransjeprogram.finnBransje(næringINæringsmiddelindustriBransjen)

        (actual!!.bransjeId as BransjeId.Næring).næring shouldBe "10"
    }
}