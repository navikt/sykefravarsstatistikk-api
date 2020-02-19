package no.nav.tag.sykefravarsstatistikk.api.sammenligning;

import no.nav.tag.sykefravarsstatistikk.api.domene.InnloggetBruker;
import no.nav.tag.sykefravarsstatistikk.api.domene.bransjeprogram.Bransjeprogram;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sammenligning;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.EnhetsregisteretClient;
import no.nav.tag.sykefravarsstatistikk.api.virksomhetsklassifikasjoner.KlassifikasjonerRepository;
import no.nav.tag.sykefravarsstatistikk.api.virksomhetsklassifikasjoner.SektorMappingService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static no.nav.tag.sykefravarsstatistikk.api.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SammenligningServiceTest {
    @Mock
    SammenligningRepository sammenligningRepository;
    @Mock
    EnhetsregisteretClient enhetsregisteretClient;
    @Mock
    SektorMappingService sektorMappingService;
    @Mock
    KlassifikasjonerRepository klassifikasjonerRepository;
    @Mock
    ApplicationEventPublisher eventPublisher;
    @Mock
    Bransjeprogram bransjeprogram;

    private SammenligningService sammenligningService;

    @Before
    public void setUp() {
        when(enhetsregisteretClient.hentInformasjonOmUnderenhet(any())).thenReturn(enUnderenhet());
        when(enhetsregisteretClient.hentInformasjonOmEnhet(any())).thenReturn(enEnhet());
        when(sektorMappingService.mapTilSSBSektorKode(any())).thenReturn(enSektor());
        when(sammenligningRepository.hentSykefraværprosentNæring(anyInt(), anyInt(), any())).thenReturn(enSykefraværprosent());
        when(sammenligningRepository.hentSykefraværprosentBransje(anyInt(), anyInt(), any())).thenReturn(enSykefraværprosent());

        sammenligningService = new SammenligningService(
                sammenligningRepository,
                enhetsregisteretClient,
                sektorMappingService,
                klassifikasjonerRepository,
                eventPublisher,
                bransjeprogram
        );
    }

    @Test
    public void hentSammenligningForUnderenhet__skal_hente_bransje_og_ikke_næring_hvis_virksomhet_er_med_i_bransjeprogrammet() {
        Sykefraværprosent sykefraværprosentBransje = enSykefraværprosent("hei", 10, 100, 6);
        when(bransjeprogram.finnBransje(any())).thenReturn(Optional.of(enBransje()));
        when(sammenligningRepository.hentSykefraværprosentBransje(anyInt(), anyInt(), any())).thenReturn(sykefraværprosentBransje);

        Sammenligning sammenligning = sammenligningService.hentSammenligningForUnderenhet(etOrgnr(), new InnloggetBruker(getFnr()), "asdfa");

        assertThat(sammenligning.getNæring()).isNull();
        assertThat(sammenligning.getBransje()).isEqualTo(sykefraværprosentBransje);
    }

    @Test
    public void hentSammenligningForUnderenhet__skal_hente_næring_og_ikke_bransje_hvis_virksomhet_er_med_i_bransjeprogrammet() {
        Sykefraværprosent sykefraværprosentNæring = enSykefraværprosent("hei", 10, 100, 6);
        when(bransjeprogram.finnBransje(any())).thenReturn(Optional.empty());
        when(sammenligningRepository.hentSykefraværprosentNæring(anyInt(), anyInt(), any())).thenReturn(sykefraværprosentNæring);

        Sammenligning sammenligning = sammenligningService.hentSammenligningForUnderenhet(etOrgnr(), new InnloggetBruker(getFnr()), "asdfa");

        assertThat(sammenligning.getBransje()).isNull();
        assertThat(sammenligning.getNæring()).isEqualTo(sykefraværprosentNæring);
    }

}
