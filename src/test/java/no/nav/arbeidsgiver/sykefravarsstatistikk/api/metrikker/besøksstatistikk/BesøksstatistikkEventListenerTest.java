package no.nav.arbeidsgiver.sykefravarsstatistikk.api.metrikker.besøksstatistikk;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.altinn.AltinnClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.domene.sammenligning.Sammenligning;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BesøksstatistikkEventListenerTest {
    @Mock
    private BesøksstatistikkRepository repository;
    @Mock
    private AltinnClient altinnClient;

    private BesøksstatistikkEventListener eventListener;

    @Before
    public void setup() {
        eventListener = new BesøksstatistikkEventListener(repository, altinnClient);
    }

    @Test
    public void onSammenligningUtsendt__skal_lagre_mye_info_for_store_virksomheter() {
        Sammenligning sammenligning = enSammenligning();
        sammenligning.setVirksomhet(enSykefraværprosent(5));

        SammenligningEvent event = enSammenligningEventBuilder()
                .underenhet(enUnderenhetBuilder().antallAnsatte(5).build())
                .sammenligning(sammenligning)
                .build();

        eventListener.onSammenligningUtsendt(event);

        verify(repository, times(1)).lagreBesøkFraStorVirksomhet(any());
        verify(repository, times(0)).lagreBesøkFraLitenVirksomhet(any());
    }

    @Test
    public void onSammenligningUtsendt__skal_ikke_lagre_info_på_virksomheter_med_få_ansatte_fra_enhetsregisteret() {
        Sammenligning sammenligning = enSammenligning();
        sammenligning.setVirksomhet(enSykefraværprosent(4));

        SammenligningEvent event = enSammenligningEventBuilder()
                .underenhet(enUnderenhetBuilder().antallAnsatte(1000).build())
                .sammenligning(sammenligning)
                .build();

        eventListener.onSammenligningUtsendt(event);

        verify(repository, times(0)).lagreBesøkFraStorVirksomhet(any());
        verify(repository, times(1)).lagreBesøkFraLitenVirksomhet(any());
    }

    @Test
    public void onSammenligningUtsendt__skal_ikke_lagre_info_på_virksomheter_med_få_personer_i_datagrunnlaget() {
        Sammenligning sammenligning = enSammenligning();
        sammenligning.setVirksomhet(enSykefraværprosent(4000));
        SammenligningEvent event = enSammenligningEventBuilder()
                .underenhet(enUnderenhetBuilder().antallAnsatte(4).build())
                .build();

        eventListener.onSammenligningUtsendt(event);

        verify(repository, times(0)).lagreBesøkFraStorVirksomhet(any());
        verify(repository, times(1)).lagreBesøkFraLitenVirksomhet(any());
    }

    @Test
    public void onSammenligningUtsendt__skal_ikke_lagre_data_hvis_session_allerede_er_registrert() {
        String sessionId = "sessionId";
        when(repository.sessionHarBlittRegistrert(eq(sessionId), any(Orgnr.class))).thenReturn(true);

        SammenligningEvent event = enSammenligningEventBuilder()
                .sessionId(sessionId)
                .build();

        eventListener.onSammenligningUtsendt(event);

        verify(repository, times(0)).lagreBesøkFraStorVirksomhet(any());
        verify(repository, times(0)).lagreBesøkFraLitenVirksomhet(any());
    }

    @Test
    public void onSammenligningUtsendt__skal_ikke_feile_hvis_næring_og_bransje_er_null_for_stor_virksomhet() {
        Sammenligning sammenligning = enSammenligningBuilder()
                .næring(null)
                .bransje(null)
                .virksomhet(enSykefraværprosent(5))
                .build();

        SammenligningEvent event = enSammenligningEventBuilder()
                .bransje(null)
                .underenhet(enUnderenhetBuilder().antallAnsatte(5).build())
                .sammenligning(sammenligning)
                .build();

        eventListener.onSammenligningUtsendt(event);
    }
}
