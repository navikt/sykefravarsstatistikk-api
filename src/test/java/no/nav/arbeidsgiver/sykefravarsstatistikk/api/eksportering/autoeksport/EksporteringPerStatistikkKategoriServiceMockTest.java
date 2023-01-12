package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.__2019_3;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.__2019_4;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.__2020_1;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.__2020_2;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.landSykefravær;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.sykefraværsstatistikkLandSiste4Kvartaler;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhet;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.virksomhetSykefraværMedKategori;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.EksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.SykefraværsstatistikkTilEksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetEksportPerKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhetUtenVarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
  ArgumentCaptor<Statistikkategori> statistikkategoriArgumentCaptor;
  @Captor
  ArgumentCaptor<String> identifikatorArgumentCaptor;
  @Captor
  ArgumentCaptor<SykefraværMedKategori> sykefraværMedKategoriArgumentCaptor;

  @Captor
  ArgumentCaptor<SykefraværFlereKvartalerForEksport> sykefraværFlereKvartalerForEksportArgumentCaptor;

  @BeforeEach
  public void setUp() {
    service = new EksporteringPerStatistikkKategoriService(sykefraværRepository,
        sykefraværsstatistikkTilEksporteringRepository, eksporteringRepository, kafkaService, true);
  }

  @Test
  public void eksporterSykefraværsstatistikkLand__sender_riktig_melding_til_kafka_og_returnerer_antall_meldinger_sendt() {
    List<UmaskertSykefraværForEttKvartal> umaskertSykefraværForEttKvartalListe = sykefraværsstatistikkLandSiste4Kvartaler(
        __2020_2);
    when(sykefraværRepository.hentUmaskertSykefraværForNorge(any())).thenReturn(
        umaskertSykefraværForEttKvartalListe);
    when(kafkaService.sendTilStatistikkKategoriTopic(any(), any(), any(), any(), any())).thenReturn(
        true);

    int antallEksporterte = service.eksporterSykefraværsstatistikkLand(__2020_2);

    verify(kafkaService).sendTilStatistikkKategoriTopic(årstallOgKvartalArgumentCaptor.capture(),
        statistikkategoriArgumentCaptor.capture(), identifikatorArgumentCaptor.capture(),
        sykefraværMedKategoriArgumentCaptor.capture(),
        sykefraværFlereKvartalerForEksportArgumentCaptor.capture());

    assertThat(årstallOgKvartalArgumentCaptor.getValue()).isEqualTo(__2020_2);
    assertThat(statistikkategoriArgumentCaptor.getValue()).isEqualTo(Statistikkategori.LAND);
    assertThat(identifikatorArgumentCaptor.getValue()).isEqualTo("NO");
    EksporteringServiceTestUtils.assertEqualsSykefraværMedKategori(landSykefravær,
        sykefraværMedKategoriArgumentCaptor.getValue());
    EksporteringServiceTestUtils.assertEqualsSykefraværFlereKvartalerForEksport(
        new SykefraværFlereKvartalerForEksport(umaskertSykefraværForEttKvartalListe),
        sykefraværFlereKvartalerForEksportArgumentCaptor.getValue());
    assertThat(antallEksporterte).isEqualTo(1);
  }

  @Test
  public void eksporterSykefraværsstatistikkVirksomhet__sender_riktig_melding_til_kafka_og_returnerer_antall_meldinger_sendt() {
    // 1- Mock det database og repositories returnerer. Samme med KafkaService
    List<SykefraværsstatistikkVirksomhetUtenVarighet> allData = List.of(
        sykefraværsstatistikkVirksomhet(__2020_2, "987654321"),
        sykefraværsstatistikkVirksomhet(__2020_1, "987654321"),
        sykefraværsstatistikkVirksomhet(__2019_4, "987654321"),
        sykefraværsstatistikkVirksomhet(__2019_3, "987654321"));
    when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværAlleVirksomheter(__2019_3,
        __2020_2)).thenReturn(allData);
    when(kafkaService.sendTilStatistikkKategoriTopic(any(), any(), any(), any(), any())).thenReturn(
        true);
    when(eksporteringRepository.hentVirksomhetEksportPerKvartal(__2020_2)).thenReturn(Arrays.asList(
        new VirksomhetEksportPerKvartal(new Orgnr("987654321"), new ÅrstallOgKvartal(2022, 2),
            false)));

    // 2- Kall tjenesten
    int antallEksporterte = service.eksporterSykefraværsstatistikkVirksomhet(__2020_2,
        EksporteringBegrensning.build().utenBegrensning());

    // 3- Sjekk hva Kafka har fått
    verify(kafkaService).sendTilStatistikkKategoriTopic(årstallOgKvartalArgumentCaptor.capture(),
        statistikkategoriArgumentCaptor.capture(), identifikatorArgumentCaptor.capture(),
        sykefraværMedKategoriArgumentCaptor.capture(),
        sykefraværFlereKvartalerForEksportArgumentCaptor.capture());

    assertThat(årstallOgKvartalArgumentCaptor.getValue()).isEqualTo(__2020_2);
    assertThat(statistikkategoriArgumentCaptor.getValue()).isEqualTo(Statistikkategori.VIRKSOMHET);
    assertThat(identifikatorArgumentCaptor.getValue()).isEqualTo("987654321");

    EksporteringServiceTestUtils.assertEqualsSykefraværMedKategori(virksomhetSykefraværMedKategori,
        sykefraværMedKategoriArgumentCaptor.getValue());
    assertThat(antallEksporterte).isEqualTo(1);
  }

  @Test
  public void eksporterSykefraværsstatistikkVirksomhet__returnerer_korrekt_antall_meldinger_sendt() {
    List<SykefraværsstatistikkVirksomhetUtenVarighet> allData = List.of(
        sykefraværsstatistikkVirksomhet(__2020_2, "987654321"),
        sykefraværsstatistikkVirksomhet(__2020_1, "987654321"),
        sykefraværsstatistikkVirksomhet(__2019_4, "987654321"),
        sykefraværsstatistikkVirksomhet(__2019_3, "987654321"),
        sykefraværsstatistikkVirksomhet(__2020_2, "987654322"),
        sykefraværsstatistikkVirksomhet(__2020_1, "987654322"),
        sykefraværsstatistikkVirksomhet(__2019_4, "987654322"));

    when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværAlleVirksomheter(__2019_3,
        __2020_2)).thenReturn(allData);
    when(kafkaService.sendTilStatistikkKategoriTopic(any(), any(), any(), any(), any())).thenReturn(
        true);
    when(eksporteringRepository.hentVirksomhetEksportPerKvartal(__2020_2)).thenReturn(Arrays.asList(
        new VirksomhetEksportPerKvartal(new Orgnr("987654321"), new ÅrstallOgKvartal(2022, 2),
            false),
        new VirksomhetEksportPerKvartal(new Orgnr("987654322"), new ÅrstallOgKvartal(2022, 2),
            false)));

    int antallEksporterte = service.eksporterSykefraværsstatistikkVirksomhet(__2020_2,
        EksporteringBegrensning.build().utenBegrensning());

    assertThat(antallEksporterte).isEqualTo(2);
  }

  @Test
  public void eksporterSykefraværsstatistikkVirksomhet__eksporterer_til_og_med_bedrifter_uten_statistikk() {
    List<SykefraværsstatistikkVirksomhetUtenVarighet> allData = List.of(
        sykefraværsstatistikkVirksomhet(__2020_2, "987654321"),
        sykefraværsstatistikkVirksomhet(__2020_1, "987654321"));

    when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværAlleVirksomheter(__2019_3,
        __2020_2)).thenReturn(allData);
    when(kafkaService.sendTilStatistikkKategoriTopic(any(), any(), any(),
        any(SykefraværMedKategori.class),
        any(SykefraværFlereKvartalerForEksport.class))).thenReturn(true);
    when(eksporteringRepository.hentVirksomhetEksportPerKvartal(__2020_2)).thenReturn(Arrays.asList(
        new VirksomhetEksportPerKvartal(new Orgnr("987654321"), new ÅrstallOgKvartal(2022, 2),
            false),
        new VirksomhetEksportPerKvartal(new Orgnr("987654322"), new ÅrstallOgKvartal(2022, 2),
            false)));

    int antallEksporterte = service.eksporterSykefraværsstatistikkVirksomhet(__2020_2,
        EksporteringBegrensning.build().utenBegrensning());

    assertThat(antallEksporterte).isEqualTo(2);
  }
}
