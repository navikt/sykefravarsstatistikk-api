package no.nav.arbeidsgiver.sykefravarsstatistikk.api

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.altinn.AltinnOrganisasjon

object TestData {
    const val ORGNR_VIRKSOMHET_1 = "987654321"
    const val ORGNR_VIRKSOMHET_2 = "999999999"
    const val ORGNR_VIRKSOMHET_3 = "999999777"
    const val NÆRINGSKODE_5SIFFER = "10062"
    const val NÆRINGSKODE_2SIFFER = "10"

    val innloggetBruker: InnloggetBruker
        get() = getInnloggetBruker(fnr.verdi)


    fun getInnloggetBruker(fnr: String?): InnloggetBruker {
        val bruker = InnloggetBruker(Fnr(fnr!!))
        bruker.brukerensOrganisasjoner = listOf(getOrganisasjon("999999999"), getOrganisasjon("111111111"))
        return bruker
    }


    fun getOrganisasjon(organizationNumber: String?): AltinnOrganisasjon {
        return AltinnOrganisasjon(null, null, null, organizationNumber, null, null)
    }

    val fnr: Fnr
        get() = Fnr("26070248114")


    fun etOrgnr(): Orgnr {
        return Orgnr("971800534")
    }


    @JvmOverloads
    fun enNæringskode(kode: String? = "12345"): Næringskode {
        return Næringskode(kode!!)
    }


    fun etÅrstallOgKvartal(): ÅrstallOgKvartal {
        return ÅrstallOgKvartal(2019, 4)
    }
}
