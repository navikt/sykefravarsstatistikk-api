package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.EksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.SykefraværsstatistikkTilEksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetEksportPerKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhetUtenVarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværOverFlereKvartaler;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
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

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.__2019_3;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.__2019_4;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.__2020_1;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.__2020_2;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.assertEqualsStatistikkDto;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.convertToSykefraværForEttKvartal;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.landSykefravær;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.sykefraværsstatistikkLandSiste4Kvartaler;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhet;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.virksomhetSykefravær;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.virksomhetSykefraværMedKategori;
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
    ArgumentCaptor<SykefraværMedKategori> sykefraværMedKategoriArgumentCaptor;
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
                sykefraværMedKategoriArgumentCaptor.capture(),
                sykefraværOverFlereKvartalerArgumentCaptor.capture()
        );

        assertThat(årstallOgKvartalArgumentCaptor.getValue()).isEqualTo(__2020_2);
        EksporteringServiceTestUtils.assertEqualsSykefraværMedKategori(
                landSykefravær,
                sykefraværMedKategoriArgumentCaptor.getValue()
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

    @Test
    public void eksporterSykefraværsstatistikkVirksomhet__sender_riktig_melding_til_kafka_og_returnerer_antall_meldinger_sendt() {
        // 1- Mock det database og repositories returnerer. Samme med KafkaService
        List<SykefraværsstatistikkVirksomhetUtenVarighet> allData = List.of(
                sykefraværsstatistikkVirksomhet(__2020_2),
                sykefraværsstatistikkVirksomhet(__2020_1),
                sykefraværsstatistikkVirksomhet(__2019_4),
                sykefraværsstatistikkVirksomhet(__2019_3)
        );
                ;
        when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværAlleVirksomheter(__2019_3, __2020_2))
                .thenReturn(allData);
        when( eksporteringRepository.hentVirksomhetEksportPerKvartal(__2020_2)).thenReturn(
                List.of(new VirksomhetEksportPerKvartal(new Orgnr("987654321"), __2020_2, false))
        );

        when(kafkaService.sendTilStatistikkKategoriTopic(any(), any(), any())).thenReturn(1);

        // 2- Kall tjenesten
        int antallEksporterte = service.eksporterSykefraværsstatistikkVirksomhet(__2020_2);

        // 3- Sjekk hva Kafka har fått
        verify(kafkaService).sendTilStatistikkKategoriTopic(
                årstallOgKvartalArgumentCaptor.capture(),
                sykefraværMedKategoriArgumentCaptor.capture(),
                sykefraværOverFlereKvartalerArgumentCaptor.capture()
        );

        assertThat(årstallOgKvartalArgumentCaptor.getValue()).isEqualTo(__2020_2);
        EksporteringServiceTestUtils.assertEqualsSykefraværMedKategori(
                virksomhetSykefraværMedKategori,
                sykefraværMedKategoriArgumentCaptor.getValue()
        );
        assertThat(antallEksporterte).isEqualTo(0); // TODO: implement me!
    }
}
