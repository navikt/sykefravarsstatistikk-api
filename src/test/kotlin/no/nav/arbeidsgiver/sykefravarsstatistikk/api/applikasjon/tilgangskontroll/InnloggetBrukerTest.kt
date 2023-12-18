package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.tilgangskontroll

import io.kotest.matchers.shouldBe
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.tilgangsstyring.Fnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.tilgangsstyring.InnloggetBruker
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.altinn.AltinnOrganisasjon
import org.junit.jupiter.api.Test

class InnloggetBrukerTest {
    val DUMMY_FNR = "12345678901"
    val DUMMY_ORGNR = "123456789"

    @Test
    fun harTilgang__returnerer_true_hvis_bruker_har_tilgang_til_orgnr() {
        val bruker = getInnloggetBruker(DUMMY_FNR)
        val organisasjon = getOrganisasjon(DUMMY_ORGNR)
        bruker.brukerensOrganisasjoner = listOf(organisasjon)

        bruker.harTilgang(Orgnr(organisasjon.organizationNumber!!)) shouldBe true
    }

    @Test
    fun harTilgang__skal_returnere_false_hvis_bruker_ikke_har_tilgang_til_noen_org() {
        val bruker = getInnloggetBruker(DUMMY_FNR)

        bruker.harTilgang(Orgnr(DUMMY_ORGNR)) shouldBe false
    }

    @Test
    fun harTilgang__skal_feile_hvis_bruker_ikke_har_tilgang_til_orgnr() {
        val bruker = getInnloggetBruker(DUMMY_FNR)
        val organisasjon = getOrganisasjon(DUMMY_ORGNR)
        bruker.brukerensOrganisasjoner = listOf(organisasjon)

        bruker.harTilgang(Orgnr("987654321")) shouldBe false
    }

    @Test
    fun harTilgang__skal_ikke_feile_selv_om_listen_med_organisasjoner_har_null() {
        val bruker = getInnloggetBruker(DUMMY_FNR)
        val organisasjoner = listOf(getOrganisasjon(null), getOrganisasjon(DUMMY_ORGNR))
        bruker.brukerensOrganisasjoner = organisasjoner

        bruker.harTilgang(Orgnr(DUMMY_ORGNR)) shouldBe true
    }

    fun getInnloggetBruker(fnr: String?): InnloggetBruker {
        val bruker = InnloggetBruker(Fnr(fnr!!))
        bruker.brukerensOrganisasjoner = listOf(getOrganisasjon("999999999"), getOrganisasjon("111111111"))
        return bruker
    }

    fun getOrganisasjon(organizationNumber: String?): AltinnOrganisasjon {
        return AltinnOrganisasjon(
            name = null,
            type = null,
            parentOrganizationNumber = null,
            organizationNumber = organizationNumber,
            organizationForm = null,
            status = null
        )
    }
}
