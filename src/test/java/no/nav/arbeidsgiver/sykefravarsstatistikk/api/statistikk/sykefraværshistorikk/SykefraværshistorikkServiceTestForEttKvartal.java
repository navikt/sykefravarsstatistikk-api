package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjeprogram;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Sektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.KlassifikasjonerRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.SektorMappingService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis.KvartalsvisSykefraværRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis.KvartalsvisSykefraværshistorikk;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis.KvartalsvisSykefraværshistorikkService;
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

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.enInstitusjonellSektorkode;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.enUnderenhet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SykefraværshistorikkServiceTestForEttKvartal {

    @Mock
    private KvartalsvisSykefraværRepository kvartalsvisSykefraværprosentRepository;
    @Mock
    private EnhetsregisteretClient enhetsregisteretClient;
    @Mock
    private SektorMappingService sektorMappingService;
    @Mock
    private KlassifikasjonerRepository klassifikasjonerRepository;
    @Mock
    private Bransjeprogram bransjeprogram;

    @InjectMocks
    KvartalsvisSykefraværshistorikkService kvartalsvisSykefraværshistorikkService;

    @Before
    public void setUp() {
        when(sektorMappingService.mapTilSSBSektorKode(any()))
                .thenReturn(
                        new Sektor("1", "Statlig forvaltning")
                );
        when(kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentLand())
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

        List<KvartalsvisSykefraværshistorikk> kvartalsvisSykefraværshistorikk =
                kvartalsvisSykefraværshistorikkService.hentSykefraværshistorikk(
                        enUnderenhet("999999998"),
                        enInstitusjonellSektorkode()
                );

        verify(kvartalsvisSykefraværprosentRepository, times(0))
                .hentKvartalsvisSykefraværprosentNæring(any());
        KvartalsvisSykefraværshistorikk næringSFHistorikk = kvartalsvisSykefraværshistorikk.get(2);
        assertThat(næringSFHistorikk.getLabel()).isNull();
    }


    private static SykefraværForEttKvartal sykefraværprosent(String label) {
        return new SykefraværForEttKvartal(
                new ÅrstallOgKvartal(2019, 1),
                new BigDecimal(50),
                new BigDecimal(100),
                10
        );
    }
}
