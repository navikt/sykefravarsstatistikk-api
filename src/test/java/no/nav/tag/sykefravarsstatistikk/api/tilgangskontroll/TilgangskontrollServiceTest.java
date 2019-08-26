package no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll;

import no.nav.tag.sykefravarsstatistikk.api.altinn.AltinnClient;
import no.nav.tag.sykefravarsstatistikk.api.altinn.AltinnException;
import no.nav.tag.sykefravarsstatistikk.api.altinn.Organisasjon;
import no.nav.tag.sykefravarsstatistikk.api.domain.Fnr;
import no.nav.tag.sykefravarsstatistikk.api.domain.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.domain.autorisasjon.InnloggetSelvbetjeningBruker;
import no.nav.tag.sykefravarsstatistikk.api.utils.TokenUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TilgangskontrollServiceTest {
    public static final String FNR = "01082248486";
    public static final String ORGNR = "6487452783576";

    @Mock
    private AltinnClient altinnClient;
    @Mock
    private TilgangskontrollService tilgangskontroll;
    @Mock
    private TokenUtils tokenUtils;

    private Fnr fnr;
    private Orgnr orgnr;


    @Before
    public void setUp() {
        tilgangskontroll = new TilgangskontrollService(altinnClient, tokenUtils);
        fnr = new Fnr(FNR);
        orgnr = new Orgnr(ORGNR);
    }


    @Test
    public void sjekkTilgang__skal_gi_OK_hvis_bruker_har_tilgang_til_orgnr() {
        when(altinnClient.hentOrgnumreDerBrukerHarEnkeltrettighetTilIAWeb(fnr))
                .thenReturn(
                        Collections.singletonList(
                                getOrganisasjon(orgnr)
                        )
                );
        when(tokenUtils.erInnloggetSelvbetjeningBruker()).thenReturn(true);
        when(tokenUtils.hentInnloggetSelvbetjeningBruker()).thenReturn(new InnloggetSelvbetjeningBruker(fnr));

        tilgangskontroll.sjekkTilgang(orgnr);
    }

    @Test(expected = TilgangskontrollException.class)
    public void sjekkTilgang__skal_feile_hvis_bruker_ikke_har_tilgang_til_noen_org() {
        when(altinnClient.hentOrgnumreDerBrukerHarEnkeltrettighetTilIAWeb(fnr)).thenReturn(Collections.emptyList());
        when(tokenUtils.erInnloggetSelvbetjeningBruker()).thenReturn(true);
        when(tokenUtils.hentInnloggetSelvbetjeningBruker()).thenReturn(new InnloggetSelvbetjeningBruker(fnr));

        tilgangskontroll.sjekkTilgang(orgnr);
    }

    @Test(expected = TilgangskontrollException.class)
    public void sjekkTilgang__skal_feile_hvis_bruker_ikke_har_tilgang_til_orgnr() {
        when(altinnClient.hentOrgnumreDerBrukerHarEnkeltrettighetTilIAWeb(fnr))
                .thenReturn(
                        List.of(
                                getOrganisasjon(new Orgnr("123456789")),
                                getOrganisasjon(new Orgnr("987654321"))
                        )
                );
        when(tokenUtils.erInnloggetSelvbetjeningBruker()).thenReturn(true);
        when(tokenUtils.hentInnloggetSelvbetjeningBruker()).thenReturn(new InnloggetSelvbetjeningBruker(fnr));

        tilgangskontroll.sjekkTilgang(orgnr);
    }

    @Test(expected = AltinnException.class)
    public void sjekkTilgang__skal_feile_med_riktig_exception_hvis_altinn_feiler() {
        when(altinnClient.hentOrgnumreDerBrukerHarEnkeltrettighetTilIAWeb(fnr)).thenThrow(new AltinnException(""));
        when(tokenUtils.erInnloggetSelvbetjeningBruker()).thenReturn(true);
        when(tokenUtils.hentInnloggetSelvbetjeningBruker()).thenReturn(new InnloggetSelvbetjeningBruker(fnr));

        tilgangskontroll.sjekkTilgang(orgnr);
    }


    private static Organisasjon getOrganisasjon(Orgnr orgnr) {
        Organisasjon organisasjon = new Organisasjon();
        organisasjon.setOrganizationNumber(orgnr.getVerdi());
        return organisasjon;
    }
}