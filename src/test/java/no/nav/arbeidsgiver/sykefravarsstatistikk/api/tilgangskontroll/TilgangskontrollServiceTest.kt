package no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.getInnloggetBruker
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.getOrganisasjon
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.TilgangskontrollService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Fnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.InnloggetBruker
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.exceptions.TilgangskontrollException
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.utils.TilgangskontrollUtils
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.CorrelationIdFilter
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.altinn.AltinnException
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.altinn.AltinnKlientWrapper
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.sporbarhetslog.Loggevent
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.sporbarhetslog.Sporbarhetslogg
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.tokenx.TokenXClient
import no.nav.security.token.support.core.jwt.JwtToken
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.slf4j.MDC

class TilgangskontrollServiceTest {
    private val altinnKlientWrapper: AltinnKlientWrapper = mock()

    private val tokenUtils: TilgangskontrollUtils = mock()

    private val sporbarhetslogg: Sporbarhetslogg = mock()

    private val tokenXClient: TokenXClient = mock()

    private val fnr: Fnr = Fnr(FNR)

    private val tilgangskontroll: TilgangskontrollService = TilgangskontrollService(
        altinnKlientWrapper,
        tokenUtils,
        sporbarhetslogg,
        IAWEB_SERVICE_CODE,
        IAWEB_SERVICE_EDITION,
        tokenXClient
    )

    @Test
    fun hentInnloggetBruker__skal_feile_med_riktig_exception_hvis_altinn_feiler() {
        Mockito.`when`(tokenUtils.hentInnloggetBruker()).thenReturn(InnloggetBruker(fnr))
        Mockito.`when`(
            altinnKlientWrapper.hentVirksomheterDerBrukerHarSykefraværsstatistikkrettighet(
                ArgumentMatchers.any(), ArgumentMatchers.eq(fnr)
            )
        )
            .thenThrow(AltinnException(""))
        assertThrows(
            AltinnException::class.java
        ) { tilgangskontroll.hentBrukerKunIaRettigheter() }
    }

    @Test
    fun hentInnloggetBrukerForAlleTilganger__skal_feile_med_riktig_exception_hvis_altinn_feiler() {
        whenever(tokenUtils.hentInnloggetBruker()).thenReturn(InnloggetBruker(fnr))
        whenever(
            altinnKlientWrapper.hentVirksomheterDerBrukerHarTilknytning(
                Mockito.any(JwtToken::class.java),
                Mockito.any(Fnr::class.java)
            )
        ).thenThrow(AltinnException(""))
        assertThrows(
            AltinnException::class.java
        ) { tilgangskontroll.hentInnloggetBrukerForAlleRettigheter() }
    }

    @Test
    fun sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse__skal_feile_hvis_bruker_ikke_har_tilgang() {
        val bruker = getInnloggetBruker(FNR)
        bruker.brukerensOrganisasjoner = ArrayList()
        værInnloggetSom(bruker)
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
        val requestUrl = "http://localhost:8080/endepunkt"
        val correlationId = "flfkjdhzdnjb"
        MDC.put(CorrelationIdFilter.CORRELATION_ID_MDC_NAME, correlationId)
        tilgangskontroll.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(orgnr, httpMetode, requestUrl)
        Mockito.verify(sporbarhetslogg)
            ?.loggHendelse(
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
        MDC.remove(CorrelationIdFilter.CORRELATION_ID_MDC_NAME)
    }

    private fun værInnloggetSom(bruker: InnloggetBruker) {
        Mockito.`when`(tokenUtils.hentInnloggetBruker()).thenReturn(bruker)
        Mockito.`when`(
            altinnKlientWrapper.hentVirksomheterDerBrukerHarSykefraværsstatistikkrettighet(
                ArgumentMatchers.any(), ArgumentMatchers.eq(bruker.fnr)
            )
        )
            .thenReturn(bruker.brukerensOrganisasjoner)
    }

    companion object {
        private const val FNR = "01082248486"
        private const val IAWEB_SERVICE_CODE = "7834"
        private const val IAWEB_SERVICE_EDITION = "3"
    }
}
