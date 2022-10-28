package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.SykefraværsstatistikkTilEksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.StatistikkDto;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.__2020_2;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.assertEqualsSykefraværMedKategori;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.landSykefravær;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.sykefraværsstatistikkLandSiste4Kvartaler;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EksporteringPerStatistikkKategoriServiceMockTest {

    @Mock
    private SykefraværsstatistikkTilEksporteringRepository sykefraværsstatistikkTilEksporteringRepository;
    @Mock
    private SykefraværRepository sykefraværsRepository;
    @Mock
    private KafkaService kafkaService;

    private EksporteringPerStatistikkKategoriService service;

    @Captor
    ArgumentCaptor<ÅrstallOgKvartal> årstallOgKvartalArgumentCaptor;
    @Captor
    ArgumentCaptor<SykefraværMedKategori> landSykefraværArgumentCaptor;
    @Captor
    ArgumentCaptor<List<StatistikkDto>> statistikkDtoListArgumentCaptor;
    @Captor
    ArgumentCaptor<StatistikkDto> statistikkDtoArgumentCaptor;

    @BeforeEach
    public void setUp() {
        service = new EksporteringPerStatistikkKategoriService(
                sykefraværsRepository,
                kafkaService
        );
    }


    @Test
    public void eksport_statistikk_LAND_sender_riktig_melding_til_kafka_og_returnerer_antall_meldinger_sendt() {
        ÅrstallOgKvartal fraÅrstallOgKvartal = __2020_2.minusKvartaler(3);

        // 1- Mock det database og repositories returnerer. Samme med KafkaService
        when(sykefraværsRepository.hentUmaskertSykefraværForNorge(any()))
                .thenReturn(sykefraværsstatistikkLandSiste4Kvartaler(__2020_2));
        when(kafkaService.sendTilStatistikkKategoriTopic(any(), any(), any())).thenReturn(1);

        // 2- Kall tjenesten
        int antallEksporterte = service.eksporterSykefraværsstatistikkLand(__2020_2);

        // 3- Sjekk hva Kafka har fått
        verify(kafkaService).sendTilStatistikkKategoriTopic(
                årstallOgKvartalArgumentCaptor.capture(),
                landSykefraværArgumentCaptor.capture(),
                statistikkDtoArgumentCaptor.capture()
        );

        assertThat(årstallOgKvartalArgumentCaptor.getValue()).isEqualTo(__2020_2);
        assertEqualsSykefraværMedKategori(landSykefravær, landSykefraværArgumentCaptor.getValue());
        assertEqualsSykefraværMedKategori(
                        StatistikkDto.builder()
                                .statistikkategori(Statistikkategori.LAND)
                                .label("Norge")
                                .verdi("1.9")
                                .antallPersonerIBeregningen(10000000)
                                .kvartalerIBeregningen(
                                        List.of(
                                                fraÅrstallOgKvartal.plussKvartaler(3),
                                                fraÅrstallOgKvartal.plussKvartaler(2),
                                                fraÅrstallOgKvartal.plussKvartaler(1),
                                                fraÅrstallOgKvartal
                                        )
                                )
                                .build()
                , statistikkDtoArgumentCaptor.getValue()
        );
        assertThat(antallEksporterte).isEqualTo(1);
    }
}
