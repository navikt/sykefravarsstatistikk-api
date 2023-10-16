package no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.error.ProxyError
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.error.exceptions.AltinnException
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.getInnloggetBruker
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.getOrganisasjon
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.TilgangskontrollService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Fnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.InnloggetBruker
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.exceptions.TilgangskontrollException
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.TokenService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.CorrelationIdFilter
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.altinn.AltinnKlientWrapper
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.sporbarhetslog.Loggevent
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.sporbarhetslog.Sporbarhetslogg
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.tokenx.TokenXClient
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.MDC

class TilgangskontrollServiceTest {
    private val altinnKlientWrapper: AltinnKlientWrapper = mockk()
    private val tokenService: TokenService = mockk(relaxed = true)
    private val tokenXClient: TokenXClient = mockk(relaxed = true)
    private val sporbarhetslogg: Sporbarhetslogg = spyk()

    private val tilgangskontroll: TilgangskontrollService = TilgangskontrollService(
        altinnKlientWrapper,
        tokenService,
        sporbarhetslogg,
        IAWEB_SERVICE_CODE,
        IAWEB_SERVICE_EDITION,
        tokenXClient
    )

    @BeforeEach
    fun beforeEach() {
        værInnloggetSom(InnloggetBruker(Fnr(FNR)))
    }

    @Test
    fun hentInnloggetBruker__skal_feile_med_riktig_exception_hvis_altinn_feiler() {
        every {
            altinnKlientWrapper.hentVirksomheterDerBrukerHarSykefraværsstatistikkrettighet(any(), any())
        } throws AltinnException(ProxyError(500, "Bad", "Bad"))
        assertThrows(
            AltinnException::class.java
        ) { tilgangskontroll.hentBrukerKunIaRettigheter() }
    }

    @Test
    fun hentInnloggetBrukerForAlleTilganger__skal_feile_med_riktig_exception_hvis_altinn_feiler() {
        every {
            altinnKlientWrapper.hentVirksomheterDerBrukerHarTilknytning(any(), any())
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
                        IAWEB_SERVICE_CODE,
                        IAWEB_SERVICE_EDITION
                    )
                )
        }
        MDC.remove(CorrelationIdFilter.CORRELATION_ID_MDC_NAME)
    }

    private fun værInnloggetSom(bruker: InnloggetBruker) {
        every { tokenService.hentInnloggetBruker() } returns bruker

        every {
            altinnKlientWrapper.hentVirksomheterDerBrukerHarSykefraværsstatistikkrettighet(
                any(), any()
            )
        } returns bruker.brukerensOrganisasjoner

        every {
            altinnKlientWrapper.hentVirksomheterDerBrukerHarTilknytning(
                any(), any()
            )
        } returns bruker.brukerensOrganisasjoner
    }

    companion object {
        private const val FNR = "01082248486"
        private const val IAWEB_SERVICE_CODE = "7834"
        private const val IAWEB_SERVICE_EDITION = "3"
    }
}
