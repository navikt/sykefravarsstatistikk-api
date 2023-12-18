package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.tilgangskontroll

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.error.ProxyError
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.error.exceptions.AltinnException
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.tilgangsstyring.TilgangskontrollException
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.tilgangsstyring.TilgangskontrollService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.tilgangsstyring.Fnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.tilgangsstyring.InnloggetBruker
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.tilgangsstyring.TokenService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.CorrelationIdFilter
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.altinn.AltinnOrganisasjon
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.altinn.AltinnService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.sporbarhetslog.Loggevent
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.sporbarhetslog.Sporbarhetslogg
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.tokenx.TokenXClient
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.MDC

class TilgangskontrollServiceTest {
    private val altinnService: AltinnService = mockk()
    private val tokenService: TokenService = mockk(relaxed = true)
    private val tokenXClient: TokenXClient = mockk(relaxed = true)
    private val sporbarhetslogg: Sporbarhetslogg = spyk()

    private val FNR = "01082248486"
    private val SERVICE_CODE = "7834"
    private val SERVICE_EDITION = "3"

    private val tilgangskontroll: TilgangskontrollService = TilgangskontrollService(
        altinnService = altinnService,
        tokenService = tokenService,
        sporbarhetslogg = sporbarhetslogg,
        iawebServiceCode = SERVICE_CODE,
        iawebServiceEdition = SERVICE_EDITION,
        tokenXClient = tokenXClient
    )

    @BeforeEach
    fun beforeEach() {
        værInnloggetSom(InnloggetBruker(Fnr(FNR)))
    }

    @Test
    fun hentInnloggetBruker__skal_feile_med_riktig_exception_hvis_altinn_feiler() {
        every {
            altinnService.hentVirksomheterDerBrukerHarSykefraværsstatistikkrettighet(any(), any())
        } throws AltinnException(ProxyError(500, "Bad", "Bad"))
        assertThrows(
            AltinnException::class.java
        ) { tilgangskontroll.hentBrukerKunIaRettigheter() }
    }

    @Test
    fun hentInnloggetBrukerForAlleTilganger__skal_feile_med_riktig_exception_hvis_altinn_feiler() {
        every {
            altinnService.hentVirksomheterDerBrukerHarTilknytning(any(), any())
        } throws AltinnException(ProxyError(500, "Bad", "Bad"))

        assertThrows(
            AltinnException::class.java
        ) { tilgangskontroll.hentInnloggetBrukerForAlleRettigheter() }
    }

    @Test
    fun sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse__skal_feile_hvis_bruker_ikke_har_tilgang() {
        assertThrows(
            TilgangskontrollException::class.java
        ) {
            tilgangskontroll.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
                Orgnr("111111111"), "", ""
            )
        }
    }

    @Test
    fun sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse__skal_gi_ok_hvis_bruker_har_tilgang() {
        val bruker = getInnloggetBruker(FNR)
        bruker.brukerensOrganisasjoner = listOf(getOrganisasjon("999999999"))
        værInnloggetSom(bruker)
        tilgangskontroll.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(Orgnr("999999999"), "", "")
    }

    @Test
    fun sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse__skal_sende_med_riktig_parametre_til_sporbarhetsloggen() {
        val bruker = getInnloggetBruker(FNR)
        val orgnr = Orgnr("999999999")
        bruker.brukerensOrganisasjoner = listOf(getOrganisasjon(orgnr.verdi))
        værInnloggetSom(bruker)
        val httpMetode = "GET"
        val requestUrl = "http://127.0.0.1:8080/endepunkt"
        val correlationId = "flfkjdhzdnjb"
        MDC.put(CorrelationIdFilter.CORRELATION_ID_MDC_NAME, correlationId)
        tilgangskontroll.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(orgnr, httpMetode, requestUrl)
        verify {
            sporbarhetslogg
                .loggHendelse(
                    Loggevent(
                        bruker,
                        orgnr,
                        true,
                        httpMetode,
                        requestUrl,
                        SERVICE_CODE,
                        SERVICE_EDITION
                    )
                )
        }
        MDC.remove(CorrelationIdFilter.CORRELATION_ID_MDC_NAME)
    }

    private fun værInnloggetSom(bruker: InnloggetBruker) {
        every { tokenService.hentInnloggetBruker() } returns bruker

        every {
            altinnService.hentVirksomheterDerBrukerHarSykefraværsstatistikkrettighet(
                any(), any()
            )
        } returns bruker.brukerensOrganisasjoner

        every {
            altinnService.hentVirksomheterDerBrukerHarTilknytning(
                any(), any()
            )
        } returns bruker.brukerensOrganisasjoner
    }

    fun getInnloggetBruker(fnr: String?): InnloggetBruker {
        val bruker = InnloggetBruker(Fnr(fnr!!))
        bruker.brukerensOrganisasjoner = listOf(getOrganisasjon("999999999"), getOrganisasjon("111111111"))
        return bruker
    }

    fun getOrganisasjon(organizationNumber: String?): AltinnOrganisasjon {
        return AltinnOrganisasjon(null, null, null, organizationNumber, null, null)
    }
}
