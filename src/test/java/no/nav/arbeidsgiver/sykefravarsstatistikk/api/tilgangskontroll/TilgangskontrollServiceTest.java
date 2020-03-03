package no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.CorrelationIdFilter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.altinn.AltinnClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.altinn.AltinnException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.Fnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.InnloggetBruker;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.Orgnr;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.Arrays;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.getInnloggetBruker;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.getOrganisasjon;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TilgangskontrollServiceTest {
    private static final String FNR = "01082248486";
    private static final String IAWEB_SERVICE_CODE = "7834";
    private static final String IAWEB_SERVICE_EDITION = "3";

    @Mock
    private AltinnClient altinnClient;
    @Mock
    private TilgangskontrollService tilgangskontroll;
    @Mock
    private TilgangskontrollUtils tokenUtils;
    @Mock
    private Sporbarhetslogg sporbarhetslogg;

    private Fnr fnr;


    @Before
    public void setUp() {
        tilgangskontroll = new TilgangskontrollService(
                altinnClient,
                tokenUtils,
                sporbarhetslogg,
                IAWEB_SERVICE_CODE,
                IAWEB_SERVICE_EDITION
        );
        fnr = new Fnr(FNR);
    }

    @Test(expected = AltinnException.class)
    public void hentInnloggetBruker__skal_feile_med_riktig_exception_hvis_altinn_feiler() {
        værInnloggetSom(new InnloggetBruker(fnr));
        when(altinnClient.hentOrgnumreDerBrukerHarEnkeltrettighetTilIAWeb(fnr)).thenThrow(new AltinnException(""));

        tilgangskontroll.hentInnloggetBruker();
    }

    @Test(expected = TilgangskontrollException.class)
    public void sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse__skal_feile_hvis_bruker_ikke_har_tilgang() {
        InnloggetBruker bruker = getInnloggetBruker(FNR);
        bruker.setOrganisasjoner(new ArrayList<>());
        værInnloggetSom(bruker);
        tilgangskontroll.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(new Orgnr("111111111"), "", "");
    }

    @Test
    public void sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse__skal_gi_ok_hvis_bruker_har_tilgang() {
        InnloggetBruker bruker = getInnloggetBruker(FNR);
        bruker.setOrganisasjoner(Arrays.asList(
                getOrganisasjon("999999999")
        ));
        værInnloggetSom(bruker);
        tilgangskontroll.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(new Orgnr("999999999"), "", "");
    }

    @Test
    public void sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse__skal_sende_med_riktig_parametre_til_sporbarhetsloggen() {
        InnloggetBruker bruker = getInnloggetBruker(FNR);
        Orgnr orgnr = new Orgnr("999999999");
        bruker.setOrganisasjoner(Arrays.asList(getOrganisasjon(orgnr.getVerdi())));
        værInnloggetSom(bruker);
        String httpMetode = "GET";
        String requestUrl = "http://localhost:8080/endepunkt";
        String correlationId = "flfkjdhzdnjb";
        MDC.put(CorrelationIdFilter.CORRELATION_ID_MDC_NAME, correlationId);

        tilgangskontroll.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(orgnr, httpMetode, requestUrl);

        verify(sporbarhetslogg).loggHendelse(
                bruker,
                orgnr,
                true,
                httpMetode,
                requestUrl,
                IAWEB_SERVICE_CODE,
                IAWEB_SERVICE_EDITION

        );

        MDC.remove(CorrelationIdFilter.CORRELATION_ID_MDC_NAME);
    }

    private void værInnloggetSom(InnloggetBruker bruker) {
        when(tokenUtils.erInnloggetSelvbetjeningBruker()).thenReturn(true);
        when(tokenUtils.hentInnloggetSelvbetjeningBruker()).thenReturn(bruker);
        when(altinnClient.hentOrgnumreDerBrukerHarEnkeltrettighetTilIAWeb(bruker.getFnr())).thenReturn(bruker.getOrganisasjoner());
    }

}
