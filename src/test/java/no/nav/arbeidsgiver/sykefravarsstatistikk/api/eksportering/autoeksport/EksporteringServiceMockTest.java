package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.NæringOgNæringskode5siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.VirksomhetMetadata;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.EksporteringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.EksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværsstatistikkTilEksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.VirksomhetMetadataRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.VirksomhetSykefravær;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EksporteringServiceMockTest {

    @Mock
    private EksporteringRepository eksporteringRepository;
    @Mock
    private VirksomhetMetadataRepository virksomhetMetadataRepository;

    @Mock
    private SykefraværsstatistikkTilEksporteringRepository
            sykefraværsstatistikkTilEksporteringRepository;

    @Mock
    private SykefraværRepository sykefraværsRepository;
    @Mock
    private KafkaService kafkaService;

    private EksporteringService service;

    @Captor
    ArgumentCaptor<ÅrstallOgKvartal> årstallOgKvartalArgumentCaptor;
    @Captor
    ArgumentCaptor<VirksomhetSykefravær> virksomhetSykefraværArgumentCaptor;
    @Captor
    ArgumentCaptor<List<SykefraværMedKategori>> næring5SifferSykefraværArgumentCaptor;
    @Captor
    ArgumentCaptor<SykefraværMedKategori> næringSykefraværArgumentCaptor;
    @Captor
    ArgumentCaptor<SykefraværMedKategori> sektorSykefraværArgumentCaptor;
    @Captor
    ArgumentCaptor<SykefraværMedKategori> landSykefraværArgumentCaptor;

    @BeforeEach
    public void setUp() {
        service =
                new EksporteringService(
                        eksporteringRepository,
                        virksomhetMetadataRepository,
                        sykefraværsstatistikkTilEksporteringRepository,
                        sykefraværsRepository,
                        kafkaService,
                        true);
    }

    @Test
    public void eksporter_returnerer_antall_rader_eksportert() {
        when(eksporteringRepository.hentVirksomhetEksportPerKvartal(__2020_2))
                .thenReturn(Collections.emptyList());

        int antallEksporterte =
                service.eksporter(__2020_2);

        assertThat(antallEksporterte).isEqualTo(0);
    }

    @Test
    public void eksporter_sender_riktig_melding_til_kafka_og_returnerer_antall_meldinger_sendt() {
        when(eksporteringRepository.hentVirksomhetEksportPerKvartal(__2020_2))
                .thenReturn(Arrays.asList(virksomhetEksportPerKvartal));
        virksomhetMetadata.leggTilNæringOgNæringskode5siffer(
                Arrays.asList(
                        new NæringOgNæringskode5siffer("11", "11000"),
                        new NæringOgNæringskode5siffer("85", "85000")));
        ÅrstallOgKvartal fraÅrstallOgKvartal = __2020_2.minusKvartaler(3);

        when(virksomhetMetadataRepository.hentVirksomhetMetadataMedNæringskoder(__2020_2))
                .thenReturn(Arrays.asList(virksomhetMetadata));
        when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleSektorer(__2020_2))
                .thenReturn(Arrays.asList(sykefraværsstatistikkSektor));
        when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleNæringer(__2020_2))
                .thenReturn(
                        Arrays.asList(
                                sykefraværsstatistikkNæring(fraÅrstallOgKvartal),
                                sykefraværsstatistikkNæring(fraÅrstallOgKvartal.plussKvartaler(1)),
                                sykefraværsstatistikkNæring(fraÅrstallOgKvartal.plussKvartaler(2)),
                                sykefraværsstatistikkNæring(fraÅrstallOgKvartal.plussKvartaler(3))));
        when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleNæringer5Siffer(
                __2020_2))
                .thenReturn(Arrays.asList(sykefraværsstatistikkNæring5Siffer));
        when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværAlleVirksomheter(__2020_2))
                .thenReturn(
                        Arrays.asList(
                                sykefraværsstatistikkVirksomhet(fraÅrstallOgKvartal, "987654321"),
                                sykefraværsstatistikkVirksomhet(fraÅrstallOgKvartal.plussKvartaler(1), "987654321"),
                                sykefraværsstatistikkVirksomhet(fraÅrstallOgKvartal.plussKvartaler(2), "987654321"),
                                sykefraværsstatistikkVirksomhet(
                                        fraÅrstallOgKvartal.plussKvartaler(3), "987654321")));
        when(sykefraværsRepository.hentUmaskertSykefraværForNorge(any()))
                .thenReturn(sykefraværsstatistikkLandSiste4Kvartaler(__2020_2));

        int antallEksporterte =
                service.eksporter(__2020_2);

        verify(kafkaService)
                .send(
                        årstallOgKvartalArgumentCaptor.capture(),
                        virksomhetSykefraværArgumentCaptor.capture(),
                        næring5SifferSykefraværArgumentCaptor.capture(),
                        næringSykefraværArgumentCaptor.capture(),
                        sektorSykefraværArgumentCaptor.capture(),
                        landSykefraværArgumentCaptor.capture());
        assertThat(årstallOgKvartalArgumentCaptor.getValue()).isEqualTo(__2020_2);
        assertEqualsVirksomhetSykefravær(
                virksomhetSykefravær, virksomhetSykefraværArgumentCaptor.getValue());
        assertThat(næring5SifferSykefraværArgumentCaptor.getValue().size()).isEqualTo(1);
        assertEqualsSykefraværMedKategori(
                næring5SifferSykefraværArgumentCaptor.getValue().get(0), næring5SifferSykefravær);
        assertEqualsSykefraværMedKategori(næringSykefravær, næringSykefraværArgumentCaptor.getValue());
        assertEqualsSykefraværMedKategori(sektorSykefravær, sektorSykefraværArgumentCaptor.getValue());
        assertEqualsSykefraværMedKategori(landSykefravær, landSykefraværArgumentCaptor.getValue());
        assertThat(antallEksporterte).isEqualTo(1);
    }

    @Test
    public void
    eksporter_sender_riktig_melding_til_kafka_inkluderer_bransje_ved_tilhørighet_bransejprogram() {
        ÅrstallOgKvartal årstallOgKvartal = __2020_2;
        ÅrstallOgKvartal fraÅrstallOgKvartal = __2020_2.minusKvartaler(3);
        VirksomhetMetadata virksomhet1_TilHørerBransjeMetadata =
                virksomhet1_TilHørerBransjeMetadata(årstallOgKvartal);
        virksomhet1_TilHørerBransjeMetadata.leggTilNæringOgNæringskode5siffer(
                Arrays.asList(
                        new NæringOgNæringskode5siffer("86", "86101"),
                        new NæringOgNæringskode5siffer("86", "86102")));

        when(eksporteringRepository.hentVirksomhetEksportPerKvartal(__2020_2))
                .thenReturn(Arrays.asList(virksomhetEksportPerKvartal));

        when(virksomhetMetadataRepository.hentVirksomhetMetadataMedNæringskoder(årstallOgKvartal))
                .thenReturn(Arrays.asList(virksomhet1_TilHørerBransjeMetadata));
        when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleSektorer(
                årstallOgKvartal))
                .thenReturn(Arrays.asList(sykefraværsstatistikkSektor));
        when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleNæringer(__2020_2))
                .thenReturn(
                        Arrays.asList(
                                sykefraværsstatistikkNæring(fraÅrstallOgKvartal),
                                sykefraværsstatistikkNæring(fraÅrstallOgKvartal.plussKvartaler(1)),
                                sykefraværsstatistikkNæring(fraÅrstallOgKvartal.plussKvartaler(2)),
                                sykefraværsstatistikkNæring(fraÅrstallOgKvartal.plussKvartaler(3))));
        when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleNæringer5Siffer(
                __2020_2))
                .thenReturn(
                        Arrays.asList(
                                sykefraværsstatistikkNæring5SifferBransjeprogram("86101", årstallOgKvartal),
                                sykefraværsstatistikkNæring5SifferBransjeprogram(
                                        "86101", årstallOgKvartal.minusKvartaler(1)),
                                sykefraværsstatistikkNæring5SifferBransjeprogram(
                                        "86101", årstallOgKvartal.minusKvartaler(2)),
                                sykefraværsstatistikkNæring5SifferBransjeprogram(
                                        "86101", årstallOgKvartal.minusKvartaler(3)),
                                sykefraværsstatistikkNæring5SifferBransjeprogram("86102", årstallOgKvartal),
                                sykefraværsstatistikkNæring5SifferBransjeprogram(
                                        "86102", årstallOgKvartal.minusKvartaler(1)),
                                sykefraværsstatistikkNæring5SifferBransjeprogram(
                                        "86102", årstallOgKvartal.minusKvartaler(2)),
                                sykefraværsstatistikkNæring5SifferBransjeprogram(
                                        "86102", årstallOgKvartal.minusKvartaler(3))));
        when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværAlleVirksomheter(__2020_2))
                .thenReturn(
                        Arrays.asList(
                                sykefraværsstatistikkVirksomhet(fraÅrstallOgKvartal, "987654321"),
                                sykefraværsstatistikkVirksomhet(fraÅrstallOgKvartal.plussKvartaler(1), "987654321"),
                                sykefraværsstatistikkVirksomhet(fraÅrstallOgKvartal.plussKvartaler(2), "987654321"),
                                sykefraværsstatistikkVirksomhet(
                                        fraÅrstallOgKvartal.plussKvartaler(3), "987654321")));
        when(sykefraværsRepository.hentUmaskertSykefraværForNorge(any()))
                .thenReturn(sykefraværsstatistikkLandSiste4Kvartaler(årstallOgKvartal));

        int antallEksporterte =
                service.eksporter(årstallOgKvartal);

        verify(kafkaService)
                .send(
                        årstallOgKvartalArgumentCaptor.capture(),
                        virksomhetSykefraværArgumentCaptor.capture(),
                        næring5SifferSykefraværArgumentCaptor.capture(),
                        næringSykefraværArgumentCaptor.capture(),
                        sektorSykefraværArgumentCaptor.capture(),
                        landSykefraværArgumentCaptor.capture());
        assertThat(årstallOgKvartalArgumentCaptor.getValue()).isEqualTo(årstallOgKvartal);
        assertEqualsVirksomhetSykefravær(
                virksomhetSykefravær, virksomhetSykefraværArgumentCaptor.getValue());
        assertThat(næring5SifferSykefraværArgumentCaptor.getValue().size()).isEqualTo(2);
        assertEqualsSykefraværMedKategori(
                næring5SifferSykefraværArgumentCaptor.getValue().get(0),
                næring5SifferSykefraværTilhørerBransje);
        assertThat(antallEksporterte).isEqualTo(1);
    }
}
