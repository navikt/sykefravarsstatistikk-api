package no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll;

import no.nav.tag.sykefravarsstatistikk.api.altinn.AltinnClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TilgangskontrollServiceTest {
    @Mock
    private AltinnClient altinnClient;

    private TilgangskontrollService tilgangskontroll;

    @Before
    public void setUp() {
        tilgangskontroll = new TilgangskontrollService(altinnClient);
    }

    @Test
    public void sjekkTilgang__skal_gi_OK_hvis_bruker_har_tilgang_til_orgnr() {
        String fnr = "1353423534582";
        String orgnr = "6487452783576";
        when(altinnClient.hentOrgnumreDerBrukerHarEnkeltrettighetTilIAWeb(fnr)).thenReturn(Collections.singletonList(
                orgnr
        ));

        tilgangskontroll.sjekkTilgang(fnr, orgnr);
    }

    @Test(expected = TilgangskontrollException.class)
    public void sjekkTilgang__skal_feile_hvis_bruker_ikke_har_tilgang_til_orgnr() {
        String fnr = "1353423534582";
        when(altinnClient.hentOrgnumreDerBrukerHarEnkeltrettighetTilIAWeb(fnr)).thenReturn(Collections.singletonList(
                "11111"
        ));

        tilgangskontroll.sjekkTilgang(fnr, "22222");
    }

    @Test(expected = TilgangskontrollException.class)
    public void sjekkTilgang__skal_feile_med_riktig_exception_hvis_altinn_gir_null() {
        String fnr = "1353423534582";
        when(altinnClient.hentOrgnumreDerBrukerHarEnkeltrettighetTilIAWeb(fnr)).thenReturn(null);

        tilgangskontroll.sjekkTilgang(fnr, "6487452783576");
    }
}