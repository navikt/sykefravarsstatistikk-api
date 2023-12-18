package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.tilgangskontroll

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.tilgangsstyring.Fnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.tilgangsstyring.InnloggetBruker
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.sporbarhetslog.Loggevent
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.sporbarhetslog.Sporbarhetslogg
import org.junit.jupiter.api.Test
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.altinn.AltinnOrganisasjon as AltinnOrganisasjon1

class SporbarhetsloggTest {
    @Test
    fun loggHendelse__skal_ikke_feile_ved_nullfelter() {
        val sporbarhetslogg = Sporbarhetslogg()
        sporbarhetslogg.loggHendelse(
            Loggevent(getInnloggetBruker(fnr), null, false, null, null, null, null)
        )
        sporbarhetslogg.loggHendelse(
            Loggevent(getInnloggetBruker(fnr), null, false, null, null, null, null), null
        )
    }

    val fnr = "26070248114"

    fun getInnloggetBruker(fnr: String?): InnloggetBruker {
        val bruker = InnloggetBruker(Fnr(fnr!!))
        bruker.brukerensOrganisasjoner = listOf(
            getOrganisasjon("999999999"),
            getOrganisasjon("111111111")
        )
        return bruker
    }


    fun getOrganisasjon(organizationNumber: String?): AltinnOrganisasjon1 {
        return AltinnOrganisasjon1(
            name = null,
            type = null,
            parentOrganizationNumber = null,
            organizationNumber = organizationNumber,
            organizationForm = null,
            status = null
        )
    }

}
