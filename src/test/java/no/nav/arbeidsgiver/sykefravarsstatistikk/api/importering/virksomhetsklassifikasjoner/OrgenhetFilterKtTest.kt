package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.virksomhetsklassifikasjoner

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.fjernDupliserteOrgnr
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class OrgenhetFilterKtTest {

    private val dummyvirksomhet = Orgenhet(
        orgnr = Orgnr("111111111"),
        navn = "navn",
        rectype = "2",
        sektor = "1",
        næring = "10",
        næringskode = "10123",
        årstallOgKvartal = ÅrstallOgKvartal(
            årstall = 2023, kvartal = 1
        )
    )

    @Test
    fun `fjernDupliserteOrgnr skal fjerne elementet med høyeste sektor dersom en virksomhet har to sektorer`() {
        val orgenheter = listOf(
            dummyvirksomhet.copy(
                sektor = "3"
            ),
            dummyvirksomhet.copy(
                sektor = "1"
            ),
            dummyvirksomhet.copy(
                sektor = "2"
            ),
        )

        assertEquals(
            listOf(
                dummyvirksomhet.copy(
                    sektor = "1"
                )
            ), fjernDupliserteOrgnr(orgenheter)
        )
    }

    @Test
    fun `fjernDupliserteOrgnr skal beholde virksomheter som ikke er duplisert`() {
        val orgenheter = listOf(
            dummyvirksomhet.copy(
                orgnr = Orgnr("111111111"),
                sektor = "1"
            ),
            dummyvirksomhet.copy(
                orgnr = Orgnr("222222222"),
                sektor = "3"
            ),
            dummyvirksomhet.copy(
                orgnr = Orgnr("333333333"),
                sektor = "2"
            ),
        )
        assertThat(fjernDupliserteOrgnr(orgenheter)).containsExactlyInAnyOrderElementsOf(orgenheter)
    }

    @Test
    fun `fjernDupliserteOrgnr skal fjerne dupliserte virksomheter, og beholde alle de reserende`() {
        val orgenheter = listOf(
            dummyvirksomhet.copy(
                sektor = "3"
            ),
            dummyvirksomhet.copy(
                orgnr = Orgnr("222222222"),
                sektor = "3"
            ),
            dummyvirksomhet.copy(
                sektor = "1"
            ),
        )

        assertThat(fjernDupliserteOrgnr(orgenheter)).containsExactlyInAnyOrder(
            dummyvirksomhet.copy(
                orgnr = Orgnr("222222222"),
                sektor = "3"
            ),
            dummyvirksomhet.copy(
                sektor = "1"
            ),
        )
    }

    @Test
    fun `hvis en bedrift har ukjent sektor, så skal denne ikke bli prioritert i tilfelle duolikater`() {
        val orgenheter = listOf(
            dummyvirksomhet.copy(
                sektor = "0"
            ),
            dummyvirksomhet.copy(
                sektor = "3"
            ),
        )
        assertEquals(
            listOf(
                dummyvirksomhet.copy(
                    sektor = "3"
                )
            ), fjernDupliserteOrgnr(orgenheter)
        )
    }
}