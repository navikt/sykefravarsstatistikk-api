package no.nav.tag.sykefravarsstatistikk.api.besøksstatistikk;

import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sammenligning;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static no.nav.tag.sykefravarsstatistikk.api.TestUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
        eventListener.onSammenligningUtsendt(
                new SammenligningEvent(
                        Underenhet.builder().antallAnsatte(5).build(),
                        enEnhet(),
                        enSektor(),
                        enNæringskode5Siffer(),
                        enNæring(),
                        sammenligning
                )
        );

        verify(repository, times(1)).lagreBesøkFraStorVirksomhet(any(), any(), any(), any(), any(), any());
        verify(repository, times(0)).lagreBesøkFraLitenVirksomhet(any());
    }

    @Test
    public void onSammenligningUtsendt__skal_ikke_lagre_info_på_virksomheter_med_få_ansatte_fra_enhetsregisteret() {
        Sammenligning sammenligning = enSammenligning();
        sammenligning.setVirksomhet(enSykefraværprosent(4));
        eventListener.onSammenligningUtsendt(
                new SammenligningEvent(
                        Underenhet.builder().antallAnsatte(1000).build(),
                        enEnhet(),
                        enSektor(),
                        enNæringskode5Siffer(),
                        enNæring(),
                        sammenligning
                )
        );

        verify(repository, times(0)).lagreBesøkFraStorVirksomhet(any(), any(), any(), any(), any(), any());
        verify(repository, times(1)).lagreBesøkFraLitenVirksomhet(any());
    }

    @Test
    public void onSammenligningUtsendt__skal_ikke_lagre_info_på_virksomheter_med_få_personer_i_datagrunnlaget() {
        Sammenligning sammenligning = enSammenligning();
        sammenligning.setVirksomhet(enSykefraværprosent(4000));
        eventListener.onSammenligningUtsendt(
                new SammenligningEvent(
                        Underenhet.builder().antallAnsatte(4).build(),
                        enEnhet(),
                        enSektor(),
                        enNæringskode5Siffer(),
                        enNæring(),
                        sammenligning
                )
        );

        verify(repository, times(0)).lagreBesøkFraStorVirksomhet(any(), any(), any(), any(), any(), any());
        verify(repository, times(1)).lagreBesøkFraLitenVirksomhet(any());
    }
}