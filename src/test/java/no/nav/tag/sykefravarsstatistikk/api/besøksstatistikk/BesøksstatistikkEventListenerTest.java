package no.nav.tag.sykefravarsstatistikk.api.besøksstatistikk;

import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sammenligning;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static no.nav.tag.sykefravarsstatistikk.api.TestUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BesøksstatistikkEventListenerTest {
    @Mock
    private BesøksstatistikkRepository repository;

    private BesøksstatistikkEventListener eventListener;

    @Before
    public void setup() {
        eventListener = new BesøksstatistikkEventListener(repository);
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
}