package no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Fnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.CorrelationIdFilter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.altinn.AltinnException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.altinn.AltinnKlientWrapper;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.sporbarhet.Loggevent;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.sporbarhet.Sporbarhetslogg;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.Arrays;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.getInnloggetBruker;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.getOrganisasjon;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TilgangskontrollServiceTest {
    private static final String FNR = "01082248486";
    private static final String IAWEB_SERVICE_CODE = "7834";
    private static final String IAWEB_SERVICE_EDITION = "3";

    @Mock
    private AltinnKlientWrapper altinnKlientWrapper;
    @Mock
    private TilgangskontrollService tilgangskontroll;
    @Mock
    private TilgangskontrollUtils tokenUtils;
    @Mock
    private Sporbarhetslogg sporbarhetslogg;

    private Fnr fnr;


    @BeforeEach
    public void setUp() {
        tilgangskontroll = new TilgangskontrollService(
                altinnKlientWrapper,
                tokenUtils,
                sporbarhetslogg,
                IAWEB_SERVICE_CODE,
                IAWEB_SERVICE_EDITION
        );
        fnr = new Fnr(FNR);
    }

    @Test
    public void hentInnloggetBruker__skal_feile_med_riktig_exception_hvis_altinn_feiler() {
        when(tokenUtils.erInnloggetSelvbetjeningBruker()).thenReturn(true);
        when(tokenUtils.hentInnloggetSelvbetjeningBruker()).thenReturn(new InnloggetBruker(fnr));
        when(altinnKlientWrapper.hentOrgnumreDerBrukerHarEnkeltrettighetTilIAWeb(any(), eq(fnr))).thenThrow(new AltinnException(""));

        assertThrows(AltinnException.class, () -> tilgangskontroll.hentInnloggetBruker());
    }
    @Test
    public void hentInnloggetBrukerForAlleTilganger__skal_feile_med_riktig_exception_hvis_altinn_feiler() {
        when(tokenUtils.erInnloggetSelvbetjeningBruker()).thenReturn(true);
        when(tokenUtils.hentInnloggetSelvbetjeningBruker()).thenReturn(new InnloggetBruker(fnr));
        when(altinnKlientWrapper.hentOrgnumreDerBrukerHarTilgangTil(any(), eq(fnr))).thenThrow(new AltinnException(""));

        assertThrows(AltinnException.class, () -> tilgangskontroll.hentInnloggetBrukerForAlleRettigheter());
    }

    @Test
    public void sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse__skal_feile_hvis_bruker_ikke_har_tilgang() {
        InnloggetBruker bruker = getInnloggetBruker(FNR);
        bruker.setOrganisasjoner(new ArrayList<>());
        værInnloggetSom(bruker);

        assertThrows(
                TilgangskontrollException.class,
                () -> tilgangskontroll.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(new Orgnr("111111111"), "", "")
        );
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

        verify(sporbarhetslogg).loggHendelse(new Loggevent(
                bruker,
                orgnr,
                true,
                httpMetode,
                requestUrl,
                IAWEB_SERVICE_CODE,
                IAWEB_SERVICE_EDITION
        ));

        MDC.remove(CorrelationIdFilter.CORRELATION_ID_MDC_NAME);
    }

    private void værInnloggetSom(InnloggetBruker bruker) {
        when(tokenUtils.erInnloggetSelvbetjeningBruker()).thenReturn(true);
        when(tokenUtils.hentInnloggetSelvbetjeningBruker()).thenReturn(bruker);
        when(altinnKlientWrapper.hentOrgnumreDerBrukerHarEnkeltrettighetTilIAWeb(any(), eq(bruker.getFnr()))).thenReturn(bruker.getOrganisasjoner());
    }

}
