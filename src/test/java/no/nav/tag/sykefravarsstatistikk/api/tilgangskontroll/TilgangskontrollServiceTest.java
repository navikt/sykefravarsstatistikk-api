package no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll;

import no.nav.tag.sykefravarsstatistikk.api.altinn.AltinnClient;
import no.nav.tag.sykefravarsstatistikk.api.altinn.AltinnException;
import no.nav.tag.sykefravarsstatistikk.api.domene.Fnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.InnloggetBruker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TilgangskontrollServiceTest {
    private static final String FNR = "01082248486";

    @Mock
    private AltinnClient altinnClient;
    @Mock
    private TilgangskontrollService tilgangskontroll;
    @Mock
    private TilgangskontrollUtils tokenUtils;

    private Fnr fnr;


    @Before
    public void setUp() {
        tilgangskontroll = new TilgangskontrollService(altinnClient, tokenUtils);
        fnr = new Fnr(FNR);
    }

    @Test(expected = AltinnException.class)
    public void hentInnloggetBruker__skal_feile_med_riktig_exception_hvis_altinn_feiler() {
        when(altinnClient.hentOrgnumreDerBrukerHarEnkeltrettighetTilIAWeb(fnr)).thenThrow(new AltinnException(""));
        when(tokenUtils.erInnloggetSelvbetjeningBruker()).thenReturn(true);
        when(tokenUtils.hentInnloggetSelvbetjeningBruker()).thenReturn(new InnloggetBruker(fnr));

        tilgangskontroll.hentInnloggetBruker();
    }


}