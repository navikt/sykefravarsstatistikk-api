package no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.getInnloggetBruker
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.getOrganisasjon
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.exceptions.TilgangskontrollException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.List

class InnloggetBrukerTest {
    @Test
    fun sjekkTilgang__kaster_ingen_Exception_hvis_bruker_har_tilgang_til_orgnr() {
        val bruker = getInnloggetBruker("12345678901")
        val organisasjon = getOrganisasjon("123456789")
        bruker.brukerensOrganisasjoner = List.of(organisasjon)
        bruker.sjekkTilgang(Orgnr(organisasjon.organizationNumber!!))
    }

    @Test
    fun sjekkTilgang__skal_feile_hvis_bruker_ikke_har_tilgang_til_noen_org() {
        val bruker = getInnloggetBruker("12345678901")
        Assertions.assertThrows(
            TilgangskontrollException::class.java
        ) { bruker.sjekkTilgang(Orgnr("123456789")) }
    }

    @Test
    fun sjekkTilgang__skal_feile_hvis_bruker_ikke_har_tilgang_til_orgnr() {
        val bruker = getInnloggetBruker("12345678901")
        val organisasjon = getOrganisasjon("123456789")
        bruker.brukerensOrganisasjoner = List.of(organisasjon)
        Assertions.assertThrows(
            TilgangskontrollException::class.java
        ) { bruker.sjekkTilgang(Orgnr("987654321")) }
    }

    @Test
    fun sjekkTilgang__skal_ikke_feile_selv_om_listen_med_organisasjoner_har_null() {
        val bruker = getInnloggetBruker("12345678901")
        val organisasjoner = List.of(getOrganisasjon(null), getOrganisasjon("987654321"))
        bruker.brukerensOrganisasjoner = organisasjoner
        bruker.sjekkTilgang(Orgnr("987654321"))
    }
}
