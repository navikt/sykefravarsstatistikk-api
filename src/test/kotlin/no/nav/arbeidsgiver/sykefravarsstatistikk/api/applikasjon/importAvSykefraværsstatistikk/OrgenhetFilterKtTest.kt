package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.domene.Orgenhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Sektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class OrgenhetFilterKtTest {

    private val dummyvirksomhet = Orgenhet(
        orgnr = Orgnr("111111111"),
        navn = "navn",
        rectype = "2",
        sektor = Sektor.STATLIG,
        næring = "10",
        næringskode = "10123",
        årstallOgKvartal = ÅrstallOgKvartal(
            årstall = 2023, kvartal = 1
        )
    )

    @Test
    fun `fjernDupliserteOrgnr skal fjerne elementet med høyest prioriterte sektor dersom en virksomhet har to sektorer`() {
        val orgenheter = listOf(
            dummyvirksomhet.copy(
                sektor = Sektor.PRIVAT
            ),
            dummyvirksomhet.copy(
                sektor = Sektor.STATLIG
            ),
            dummyvirksomhet.copy(
                sektor = Sektor.KOMMUNAL
            ),
        )

        assertEquals(
            listOf(
                dummyvirksomhet.copy(
                    sektor = Sektor.STATLIG
                )
            ), fjernDupliserteOrgnr(orgenheter)
        )
    }

    @Test
    fun `fjernDupliserteOrgnr skal beholde virksomheter som ikke er duplisert`() {
        val orgenheter = listOf(
            dummyvirksomhet.copy(
                orgnr = Orgnr("111111111"),
                sektor = Sektor.STATLIG
            ),
            dummyvirksomhet.copy(
                orgnr = Orgnr("222222222"),
                sektor = Sektor.PRIVAT
            ),
            dummyvirksomhet.copy(
                orgnr = Orgnr("333333333"),
                sektor = Sektor.KOMMUNAL
            ),
        )
        assertThat(fjernDupliserteOrgnr(orgenheter)).containsExactlyInAnyOrderElementsOf(orgenheter)
    }

    @Test
    fun `fjernDupliserteOrgnr skal fjerne dupliserte virksomheter, og beholde alle de reserende`() {
        val orgenheter = listOf(
            dummyvirksomhet.copy(
                sektor = Sektor.PRIVAT
            ),
            dummyvirksomhet.copy(
                orgnr = Orgnr("222222222"),
                sektor = Sektor.PRIVAT
            ),
            dummyvirksomhet.copy(
                sektor = Sektor.STATLIG
            ),
        )

        assertThat(fjernDupliserteOrgnr(orgenheter)).containsExactlyInAnyOrder(
            dummyvirksomhet.copy(
                orgnr = Orgnr("222222222"),
                sektor = Sektor.PRIVAT
            ),
            dummyvirksomhet.copy(
                sektor = Sektor.STATLIG
            ),
        )
    }

    @Test
    fun `hvis en bedrift har ukjent sektor, så skal denne ikke bli prioritert i tilfelle duolikater`() {
        val orgenheter = listOf(
            dummyvirksomhet.copy(
                sektor = Sektor.UKJENT
            ),
            dummyvirksomhet.copy(
                sektor = Sektor.PRIVAT
            ),
        )
        assertEquals(
            listOf(
                dummyvirksomhet.copy(
                    sektor = Sektor.PRIVAT
                )
            ), fjernDupliserteOrgnr(orgenheter)
        )
    }

    @Test
    fun `Fylkeskommunal forvaltning skal ikke filtreres bort`() {
        val orgenheter = listOf(
            dummyvirksomhet.copy(
                sektor = Sektor.FYLKESKOMMUNAL
            ),
        )
        assertEquals(orgenheter, fjernDupliserteOrgnr(orgenheter))
    }
}