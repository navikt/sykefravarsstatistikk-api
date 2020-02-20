package no.nav.tag.sykefravarsstatistikk.api.sykefraværshistorikk;

import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.bransjeprogram.Bransjeprogram;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Enhet;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.EnhetsregisteretClient;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Næringskode5Siffer;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import no.nav.tag.sykefravarsstatistikk.api.virksomhetsklassifikasjoner.KlassifikasjonerRepository;
import no.nav.tag.sykefravarsstatistikk.api.virksomhetsklassifikasjoner.SektorMappingService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.EmptyResultDataAccessException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SykefraværshistorikkServiceTest {

    @Mock
    private KvartalsvisSykefraværsprosentRepository kvartalsvisSykefraværprosentRepository;
    @Mock
    private EnhetsregisteretClient enhetsregisteretClient;
    @Mock
    private SektorMappingService sektorMappingService;
    @Mock
    private KlassifikasjonerRepository klassifikasjonerRepository;
    @Mock
    private Bransjeprogram bransjeprogram;

    @InjectMocks
    SykefraværshistorikkService sykefraværshistorikkService;

    @Before
    public void setUp() {
        when(enhetsregisteretClient.hentInformasjonOmUnderenhet(any()))
                .thenReturn(
                        Underenhet
                                .builder()
                                .orgnr(new Orgnr("99999998"))
                                .overordnetEnhetOrgnr(new Orgnr("999999999"))
                                .navn("Test Underenhet")
                                .næringskode(new Næringskode5Siffer("12345", "testkode"))
                                .build()
                );
        when(enhetsregisteretClient.hentInformasjonOmEnhet(any()))
                .thenReturn(
                        Enhet
                                .builder()
                                .orgnr(new Orgnr("99999999"))
                                .build()
                );
        when(sektorMappingService.mapTilSSBSektorKode(any()))
                .thenReturn(
                        new Sektor("1", "Statlig forvaltning")
                );
        when(kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentLand(any()))
                .thenReturn(
                        Arrays.asList(sykefraværprosent("Norge"))
                );
        when(kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentSektor(any()))
                .thenReturn(
                        Arrays.asList(sykefraværprosent("Statlig forvlatning"))
                );
        when(kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentVirksomhet(any()))
                .thenReturn(
                        Arrays.asList(sykefraværprosent("Test Virksomhet"))
                );
    }

    @Test
    public void hentSykefraværshistorikk_skal_ikke_feile_dersom_uthenting_av_en_type_data_feiler() {
        when(klassifikasjonerRepository.hentNæring(any()))
                .thenThrow(new EmptyResultDataAccessException(1));

        List<Sykefraværshistorikk> sykefraværshistorikk =
                sykefraværshistorikkService.hentSykefraværshistorikk(
                        new Orgnr("999999998")
                );

        verify(kvartalsvisSykefraværprosentRepository, times(0))
                .hentKvartalsvisSykefraværprosentNæring(any());
        Sykefraværshistorikk næringSFHistorikk = sykefraværshistorikk.get(2);
        assertThat(næringSFHistorikk.getLabel()).isNull();
    }


    private static KvartalsvisSykefraværsprosent sykefraværprosent(String label) {
        return new KvartalsvisSykefraværsprosent(
                new ÅrstallOgKvartal(2019, 1),
                new Sykefraværprosent(label, new BigDecimal(50), new BigDecimal(100), 10));
    }
}
