package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.KlassifikasjonerRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.VirksomhetSykefravær;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.StatistikkDto;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.IntStream;

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
    private SykefraværsstatistikkTilEksporteringRepository sykefraværsstatistikkTilEksporteringRepository;
    @Mock
    private SykefraværRepository sykefraværsRepository;
    @Mock
    private KlassifikasjonerRepository klassifikasjonerRepository;
    @Mock
    private KafkaService kafkaService;
    @Mock
    private BransjeEllerNæringService bransjeEllerNæringService;

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
    @Captor
    ArgumentCaptor<List<StatistikkDto>> statistikkDtoListArgumentCaptor;

    @BeforeEach
    public void setUp() {
        service = new EksporteringService(
                eksporteringRepository,
                virksomhetMetadataRepository,
                sykefraværsstatistikkTilEksporteringRepository,
                sykefraværsRepository,
                kafkaService,
                true);
    }

    @Test
    public void getListeAvVirksomhetEksportPerKvartal_tar_hensyn_til_begrensning_i_eksportering_også_med_emptyList() {
        when(eksporteringRepository.hentVirksomhetEksportPerKvartal(__2020_2)).thenReturn(Collections.emptyList());

        List<VirksomhetEksportPerKvartal> emptyList1 =
                service.getListeAvVirksomhetEksportPerKvartal(
                        __2020_2,
                        EksporteringBegrensning.build().utenBegrensning()
                );
        List<VirksomhetEksportPerKvartal> emptyList2 =
                service.getListeAvVirksomhetEksportPerKvartal(
                        __2020_2,
                        EksporteringBegrensning.build().medBegrensning(100)
                );


        assertThat(emptyList1.size()).isEqualTo(0);
        assertThat(emptyList2.size()).isEqualTo(0);
    }

    @Test
    public void getListeAvVirksomhetEksportPerKvartal_tar_hensyn_til_begrensning_i_eksportering() {
        when(eksporteringRepository.hentVirksomhetEksportPerKvartal(__2020_2))
                .thenReturn(
                        Arrays.asList(
                                new VirksomhetEksportPerKvartal(
                                        new Orgnr("987654321"),
                                        __2020_2,
                                        false
                                ),
                                new VirksomhetEksportPerKvartal(
                                        new Orgnr("999999999"),
                                        __2020_2,
                                        false
                                ),
                                new VirksomhetEksportPerKvartal(
                                        new Orgnr("888888888"),
                                        __2020_2,
                                        false
                                )));

        List<VirksomhetEksportPerKvartal> list1 =
                service.getListeAvVirksomhetEksportPerKvartal(
                        __2020_2,
                        EksporteringBegrensning.build().utenBegrensning()
                );
        List<VirksomhetEksportPerKvartal> list2 =
                service.getListeAvVirksomhetEksportPerKvartal(
                        __2020_2,
                        EksporteringBegrensning.build().medBegrensning(2)
                );
        List<VirksomhetEksportPerKvartal> list3 =
                service.getListeAvVirksomhetEksportPerKvartal(
                        __2020_2,
                        EksporteringBegrensning.build().medBegrensning(3)
                );
        List<VirksomhetEksportPerKvartal> list4 =
                service.getListeAvVirksomhetEksportPerKvartal(
                        __2020_2,
                        EksporteringBegrensning.build().medBegrensning(100)
                );

        assertThat(list1.size()).isEqualTo(3);
        assertThat(list2.size()).isEqualTo(2);
        assertThat(list3.size()).isEqualTo(3);
        assertThat(list4.size()).isEqualTo(3);
    }

    @Test
    public void getListeAvVirksomhetEksportPerKvartal_tar_hensyn_til_begrensning_i_eksportering_og_eksportert_flag__med_mye_data() {
        when(eksporteringRepository.hentVirksomhetEksportPerKvartal(__2020_2))
                .thenReturn(bigList(90000, 430000));

        List<VirksomhetEksportPerKvartal> ikkeBegrenset =
                service.getListeAvVirksomhetEksportPerKvartal(
                        __2020_2,
                        EksporteringBegrensning.build().utenBegrensning()
                );
        assertThat(ikkeBegrenset.size()).isEqualTo(430000);

        List<VirksomhetEksportPerKvartal> begrensetTil10 =
                service.getListeAvVirksomhetEksportPerKvartal(
                        __2020_2,
                        EksporteringBegrensning.build().medBegrensning(10)
                );
        assertThat(begrensetTil10.size()).isEqualTo(10);
    }

    @Test
    public void getListeAvVirksomhetEksportPerKvartal_tar_hensyn_til_begrensning_i_eksportering_og_eksportert_flag() {
        when(eksporteringRepository.hentVirksomhetEksportPerKvartal(__2020_2))
                .thenReturn(
                        Arrays.asList(
                                new VirksomhetEksportPerKvartal(
                                        new Orgnr("987654321"),
                                        __2020_2,
                                        false
                                ),
                                new VirksomhetEksportPerKvartal(
                                        new Orgnr("999999999"),
                                        __2020_2,
                                        false
                                ),
                                new VirksomhetEksportPerKvartal(
                                        new Orgnr("888888888"),
                                        __2020_2,
                                        true
                                )));

        List<VirksomhetEksportPerKvartal> ikkeBegrenset =
                service.getListeAvVirksomhetEksportPerKvartal(
                        __2020_2,
                        EksporteringBegrensning.build().utenBegrensning()
                );
        List<VirksomhetEksportPerKvartal> begrensetMed1 =
                service.getListeAvVirksomhetEksportPerKvartal(
                        __2020_2,
                        EksporteringBegrensning.build().medBegrensning(1)
                );
        List<VirksomhetEksportPerKvartal> begrensetMed100 =
                service.getListeAvVirksomhetEksportPerKvartal(
                        __2020_2,
                        EksporteringBegrensning.build().medBegrensning(100)
                );

        assertThat(ikkeBegrenset.size()).isEqualTo(2);
        assertThat(begrensetMed1.size()).isEqualTo(1);
        assertThat(begrensetMed100.size()).isEqualTo(2);
    }

    @Test
    public void eksporter_returnerer_antall_rader_eksportert() {
        when(eksporteringRepository.hentVirksomhetEksportPerKvartal(__2020_2)).thenReturn(Collections.emptyList());

        int antallEksporterte = service.eksporter(__2020_2, EksporteringBegrensning.build().utenBegrensning());

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
        ÅrstallOgKvartal fraÅrstallOgKvartal=__2020_2.minusKvartaler(3);

        when(virksomhetMetadataRepository.hentVirksomhetMetadata(__2020_2))
                .thenReturn(Arrays.asList(virksomhetMetadata));
        when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleSektorer(__2020_2))
                .thenReturn(Arrays.asList(sykefraværsstatistikkSektor));
        when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleNæringer(__2020_2))
              .thenReturn(Arrays.asList(
                    sykefraværsstatistikkNæring(fraÅrstallOgKvartal),
                    sykefraværsstatistikkNæring(fraÅrstallOgKvartal.plussKvartaler(1)),
                    sykefraværsstatistikkNæring(fraÅrstallOgKvartal.plussKvartaler(2)),
                    sykefraværsstatistikkNæring(fraÅrstallOgKvartal.plussKvartaler(3)))
              );
        when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleNæringer5Siffer(__2020_2))
                .thenReturn(Arrays.asList(sykefraværsstatistikkNæring5Siffer));
        when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleVirksomheter(__2020_2))
                .thenReturn(Arrays.asList(
                      sykefraværsstatistikkVirksomhet(fraÅrstallOgKvartal),
                      sykefraværsstatistikkVirksomhet(fraÅrstallOgKvartal.plussKvartaler(1)),
                      sykefraværsstatistikkVirksomhet(fraÅrstallOgKvartal.plussKvartaler(2)),
                      sykefraværsstatistikkVirksomhet(fraÅrstallOgKvartal.plussKvartaler(3))
                ));
        when(sykefraværsRepository.hentUmaskertSykefraværForNorge(any()))
                .thenReturn(sykefraværsstatistikkLandSiste4Kvartaler(__2020_2));

        int antallEksporterte = service.eksporter(__2020_2, EksporteringBegrensning.build().utenBegrensning());

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


    @Test
    public void eksporter_sender_riktig_melding_til_kafka_inkluderer_bransje_ved_tilhørighet_bransejprogram() throws Exception {
        ÅrstallOgKvartal årstallOgKvartal = __2020_2;
        ÅrstallOgKvartal fraÅrstallOgKvartal = __2020_2.minusKvartaler(3);
        VirksomhetMetadata virksomhet1_TilHørerBransjeMetadata = virksomhet1_TilHørerBransjeMetadata(årstallOgKvartal);
        virksomhet1_TilHørerBransjeMetadata.leggTilNæringOgNæringskode5siffer(Arrays.asList(
              new NæringOgNæringskode5siffer("86", "86101"),
              new NæringOgNæringskode5siffer("86", "86102")
        ));

        when(eksporteringRepository.hentVirksomhetEksportPerKvartal(__2020_2))
                .thenReturn(Arrays.asList(virksomhetEksportPerKvartal));

        when(virksomhetMetadataRepository.hentVirksomhetMetadata(årstallOgKvartal))
                .thenReturn(Arrays.asList(virksomhet1_TilHørerBransjeMetadata));
        when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleSektorer(årstallOgKvartal))
                .thenReturn(Arrays.asList(sykefraværsstatistikkSektor));
        when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleNæringer(__2020_2))
              .thenReturn(Arrays.asList(
                    sykefraværsstatistikkNæring(fraÅrstallOgKvartal),
                    sykefraværsstatistikkNæring(fraÅrstallOgKvartal.plussKvartaler(1)),
                    sykefraværsstatistikkNæring(fraÅrstallOgKvartal.plussKvartaler(2)),
                    sykefraværsstatistikkNæring(fraÅrstallOgKvartal.plussKvartaler(3)))
              );
        when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleNæringer5Siffer(__2020_2))
                .thenReturn(
                      Arrays.asList(
                            sykefraværsstatistikkNæring5SifferBransjeprogram("86101",årstallOgKvartal),
                            sykefraværsstatistikkNæring5SifferBransjeprogram("86101",årstallOgKvartal.minusKvartaler(1)),
                            sykefraværsstatistikkNæring5SifferBransjeprogram("86101",årstallOgKvartal.minusKvartaler(2)),
                            sykefraværsstatistikkNæring5SifferBransjeprogram("86101",årstallOgKvartal.minusKvartaler(3)),
                            sykefraværsstatistikkNæring5SifferBransjeprogram("86102",årstallOgKvartal),
                            sykefraværsstatistikkNæring5SifferBransjeprogram("86102",årstallOgKvartal.minusKvartaler(1)),
                            sykefraværsstatistikkNæring5SifferBransjeprogram("86102",årstallOgKvartal.minusKvartaler(2)),
                            sykefraværsstatistikkNæring5SifferBransjeprogram("86102",årstallOgKvartal.minusKvartaler(3))
                            )
                );
        when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleVirksomheter(__2020_2))
                .thenReturn(Arrays.asList(
                      sykefraværsstatistikkVirksomhet(fraÅrstallOgKvartal),
                      sykefraværsstatistikkVirksomhet(fraÅrstallOgKvartal.plussKvartaler(1)),
                      sykefraværsstatistikkVirksomhet(fraÅrstallOgKvartal.plussKvartaler(2)),
                      sykefraværsstatistikkVirksomhet(fraÅrstallOgKvartal.plussKvartaler(3))
                ));
        when(sykefraværsRepository.hentUmaskertSykefraværForNorge(any()))
                .thenReturn(sykefraværsstatistikkLandSiste4Kvartaler(årstallOgKvartal));

        int antallEksporterte = service.eksporter(årstallOgKvartal, EksporteringBegrensning.build().utenBegrensning());

        verify(kafkaService).send(
                årstallOgKvartalArgumentCaptor.capture(),
                virksomhetSykefraværArgumentCaptor.capture(),
                næring5SifferSykefraværArgumentCaptor.capture(),
                næringSykefraværArgumentCaptor.capture(),
                sektorSykefraværArgumentCaptor.capture(),
                landSykefraværArgumentCaptor.capture()
        );
        assertThat(årstallOgKvartalArgumentCaptor.getValue()).isEqualTo(årstallOgKvartal);
        assertEqualsVirksomhetSykefravær(virksomhetSykefravær, virksomhetSykefraværArgumentCaptor.getValue());
        assertThat(næring5SifferSykefraværArgumentCaptor.getValue().size()).isEqualTo(2);
        assertEqualsSykefraværMedKategori(
                næring5SifferSykefraværArgumentCaptor.getValue().get(0),
              næring5SifferSykefraværTilhørerBransje
        );
        assertThat(antallEksporterte).isEqualTo(1);
    }


    private List<VirksomhetEksportPerKvartal> bigList(int antallEksportertIsTrue, int antallEksportertIsFalse) {
        List<VirksomhetEksportPerKvartal> list = new ArrayList<>();

        IntStream.range(0, antallEksportertIsTrue).forEach( i ->
                list.add(new VirksomhetEksportPerKvartal(
                        new Orgnr(UUID.randomUUID().toString().substring(0, 9)),
                        __2020_2,
                        true
                )));

        IntStream.range(0, antallEksportertIsFalse).forEach( i ->
                list.add(new VirksomhetEksportPerKvartal(
                        new Orgnr(UUID.randomUUID().toString().substring(0, 9)),
                        __2020_2,
                        false
                )));

        return list;
    }

}
