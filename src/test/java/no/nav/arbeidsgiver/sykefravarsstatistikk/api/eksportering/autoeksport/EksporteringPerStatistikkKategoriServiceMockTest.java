package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.EksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.SykefraværsstatistikkTilEksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværOverFlereKvartaler;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.StatistikkDto;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.__2020_2;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.assertEqualsStatistikkDto;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.convertToSykefraværForEttKvartal;
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
    private SykefraværRepository sykefraværRepository;
    @Mock
    private EksporteringRepository eksporteringRepository;

    @Mock
    private KafkaService kafkaService;

    private EksporteringPerStatistikkKategoriService service;

    @Captor
    ArgumentCaptor<ÅrstallOgKvartal> årstallOgKvartalArgumentCaptor;
    @Captor
    ArgumentCaptor<SykefraværMedKategori> landSykefraværArgumentCaptor;
    @Captor
    ArgumentCaptor<SykefraværOverFlereKvartaler> sykefraværOverFlereKvartalerArgumentCaptor;

    @BeforeEach
    public void setUp() {
        service = new EksporteringPerStatistikkKategoriService(
                sykefraværRepository,
                sykefraværsstatistikkTilEksporteringRepository,
                eksporteringRepository,
                kafkaService
        );
    }


    @Test
    public void eksporterSykefraværsstatistikkLand__sender_riktig_melding_til_kafka_og_returnerer_antall_meldinger_sendt() {
        // 1- Mock det database og repositories returnerer. Samme med KafkaService
        List<UmaskertSykefraværForEttKvartal> umaskertSykefraværForEttKvartalListe =
                sykefraværsstatistikkLandSiste4Kvartaler(__2020_2);
        when(sykefraværRepository.hentUmaskertSykefraværForNorge(any()))
                .thenReturn(umaskertSykefraværForEttKvartalListe);
        when(kafkaService.sendTilStatistikkKategoriTopic(any(), any(), any())).thenReturn(1);

        // 2- Kall tjenesten
        int antallEksporterte = service.eksporterSykefraværsstatistikkLand(__2020_2);

        // 3- Sjekk hva Kafka har fått
        verify(kafkaService).sendTilStatistikkKategoriTopic(
                årstallOgKvartalArgumentCaptor.capture(),
                landSykefraværArgumentCaptor.capture(),
                sykefraværOverFlereKvartalerArgumentCaptor.capture()
        );

        assertThat(årstallOgKvartalArgumentCaptor.getValue()).isEqualTo(__2020_2);
        EksporteringServiceTestUtils.assertEqualsSykefraværMedKategori(
                landSykefravær,
                landSykefraværArgumentCaptor.getValue()
        );
        BigDecimal sumAvTapteDagsverk =
                umaskertSykefraværForEttKvartalListe.stream()
                        .map(
                                item -> item.getDagsverkTeller()
                        )
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sumAvMuligeDagsverk =
                umaskertSykefraværForEttKvartalListe.stream()
                        .map(
                                item -> item.getDagsverkNevner()
                        )
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        EksporteringServiceTestUtils.assertEqualsSykefraværOverFlereKvartaler(
                new SykefraværOverFlereKvartaler(
                        List.of(
                                __2020_2,
                                __2020_2.minusKvartaler(1),
                                __2020_2.minusKvartaler(2),
                                __2020_2.minusKvartaler(3)
                        ),
                        sumAvTapteDagsverk,
                        sumAvMuligeDagsverk,
                        convertToSykefraværForEttKvartal(umaskertSykefraværForEttKvartalListe)
                ),
                sykefraværOverFlereKvartalerArgumentCaptor.getValue()
        );
        assertThat(antallEksporterte).isEqualTo(1);
    }

}
