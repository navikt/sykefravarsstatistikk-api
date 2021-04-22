package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.EksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.NæringOgNæringskode5siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.SykefraværsstatistikkTilEksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadataRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaTopicValue;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.VirksomhetSykefravær;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.assertj.core.api.Assertions;
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
import java.util.concurrent.TimeUnit;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EksporteringServiceMockTest {

    @Mock
    private EksporteringRepository eksporteringRepository;
    @Mock
    private VirksomhetMetadataRepository virksomhetMetadataRepository;
    @Mock
    private SykefraværsstatistikkTilEksporteringRepository sykefraværsstatistikkTilEksporteringRepository;
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
        service = new EksporteringService(
                eksporteringRepository,
                virksomhetMetadataRepository,
                sykefraværsstatistikkTilEksporteringRepository,
                kafkaService,
                true
        );
    }

    @Test
    public void eksporter_returnerer_antall_rader_eksportert() {
        when(eksporteringRepository.hentVirksomhetEksportPerKvartal(__2020_2)).thenReturn(Collections.emptyList());

        int antallEksporterte = service.eksporter(__2020_2);

        assertThat(antallEksporterte).isEqualTo(0);
    }

    @Test
    public void eksporter_sender_riktig_melding_til_kafka_og_returnerer_antall_meldinger_sendt() throws Exception {
        when(eksporteringRepository.hentVirksomhetEksportPerKvartal(__2020_2))
                .thenReturn(Arrays.asList(virksomhetEksportPerKvartal));
        virksomhetMetadata.leggTilNæringOgNæringskode5siffer(Arrays.asList(
                new NæringOgNæringskode5siffer("11", "11000"),
                new NæringOgNæringskode5siffer("85", "85000")
        ));
        when(virksomhetMetadataRepository.hentVirksomhetMetadata(__2020_2))
                .thenReturn(Arrays.asList(virksomhetMetadata));
        when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentLand(__2020_2))
                .thenReturn(sykefraværsstatistikkLand);
        when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleSektorer(__2020_2))
                .thenReturn(Arrays.asList(sykefraværsstatistikkSektor));
        when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleNæringer(__2020_2))
                .thenReturn(Arrays.asList(sykefraværsstatistikkNæring));
        when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleNæringer5Siffer(__2020_2))
                .thenReturn(Arrays.asList(sykefraværsstatistikkNæring5Siffer));
        when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleVirksomheter(__2020_2))
                .thenReturn(Arrays.asList(sykefraværsstatistikkVirksomhet));

        int antallEksporterte = service.eksporter(__2020_2);

        verify(kafkaService).send(
                årstallOgKvartalArgumentCaptor.capture(),
                virksomhetSykefraværArgumentCaptor.capture(),
                næring5SifferSykefraværArgumentCaptor.capture(),
                næringSykefraværArgumentCaptor.capture(),
                sektorSykefraværArgumentCaptor.capture(),
                landSykefraværArgumentCaptor.capture()
        );
        assertThat(årstallOgKvartalArgumentCaptor.getValue()).isEqualTo(__2020_2);
        assertEqualsVirksomhetSykefravær(virksomhetSykefravær, virksomhetSykefraværArgumentCaptor.getValue());
        assertThat(næring5SifferSykefraværArgumentCaptor.getValue().size()).isEqualTo(1);
        assertEqualsSykefraværMedKategori(
                næring5SifferSykefraværArgumentCaptor.getValue().get(0),
                næring5SifferSykefravær
        );
        assertEqualsSykefraværMedKategori(næringSykefravær, næringSykefraværArgumentCaptor.getValue());
        assertEqualsSykefraværMedKategori(sektorSykefravær, sektorSykefraværArgumentCaptor.getValue());
        assertEqualsSykefraværMedKategori(landSykefravær, landSykefraværArgumentCaptor.getValue());
        assertThat(antallEksporterte).isEqualTo(1);
    }
}