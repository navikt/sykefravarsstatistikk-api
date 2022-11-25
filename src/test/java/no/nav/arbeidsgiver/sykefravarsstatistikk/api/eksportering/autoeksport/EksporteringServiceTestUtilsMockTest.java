package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.__2020_2;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.assertEqualsSykefraværMedKategori;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.assertEqualsVirksomhetSykefravær;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.landSykefravær;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.næring5SifferSykefravær;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.næring5SifferSykefraværTilhørerBransje;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.næringSykefravær;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.sektorSykefravær;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.sykefraværsstatistikkLandSiste4Kvartaler;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.sykefraværsstatistikkNæring;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.sykefraværsstatistikkNæring5Siffer;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.sykefraværsstatistikkNæring5SifferBransjeprogram;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.sykefraværsstatistikkSektor;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhet;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.virksomhet1_TilHørerBransjeMetadata;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.virksomhetEksportPerKvartal;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.virksomhetMetadata;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.virksomhetSykefravær;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.EksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.NæringOgNæringskode5siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.SykefraværsstatistikkTilEksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetEksportPerKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadata;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadataRepository;
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

@ExtendWith(MockitoExtension.class)
public class EksporteringServiceTestUtilsMockTest {

  @Mock
  private EksporteringRepository eksporteringRepository;

  @Test
  public void getListeAvVirksomhetEksportPerKvartal_tar_hensyn_til_begrensning_i_eksportering_også_med_emptyList() {
    when(eksporteringRepository.hentVirksomhetEksportPerKvartal(__2020_2)).thenReturn(
        Collections.emptyList());

    List<VirksomhetEksportPerKvartal> emptyList1 =
        EksporteringServiceUtils.getListeAvVirksomhetEksportPerKvartal(
            __2020_2,
            EksporteringBegrensning.build().utenBegrensning(),
            eksporteringRepository
        );
    List<VirksomhetEksportPerKvartal> emptyList2 =
        EksporteringServiceUtils.getListeAvVirksomhetEksportPerKvartal(
            __2020_2,
            EksporteringBegrensning.build().medBegrensning(100),
            eksporteringRepository
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
        EksporteringServiceUtils.getListeAvVirksomhetEksportPerKvartal(
            __2020_2,
            EksporteringBegrensning.build().utenBegrensning(),
            eksporteringRepository
        );
    List<VirksomhetEksportPerKvartal> list2 =
        EksporteringServiceUtils.getListeAvVirksomhetEksportPerKvartal(
            __2020_2,
            EksporteringBegrensning.build().medBegrensning(2),
            eksporteringRepository
        );
    List<VirksomhetEksportPerKvartal> list3 =
        EksporteringServiceUtils.getListeAvVirksomhetEksportPerKvartal(
            __2020_2,
            EksporteringBegrensning.build().medBegrensning(3),
            eksporteringRepository
        );
    List<VirksomhetEksportPerKvartal> list4 =
        EksporteringServiceUtils.getListeAvVirksomhetEksportPerKvartal(
            __2020_2,
            EksporteringBegrensning.build().medBegrensning(100),
            eksporteringRepository
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
        EksporteringServiceUtils.getListeAvVirksomhetEksportPerKvartal(
            __2020_2,
            EksporteringBegrensning.build().utenBegrensning(),
            eksporteringRepository
        );
    assertThat(ikkeBegrenset.size()).isEqualTo(430000);

    List<VirksomhetEksportPerKvartal> begrensetTil10 =
        EksporteringServiceUtils.getListeAvVirksomhetEksportPerKvartal(
            __2020_2,
            EksporteringBegrensning.build().medBegrensning(10),
            eksporteringRepository
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
        EksporteringServiceUtils.getListeAvVirksomhetEksportPerKvartal(
            __2020_2,
            EksporteringBegrensning.build().utenBegrensning(),
            eksporteringRepository
        );
    List<VirksomhetEksportPerKvartal> begrensetMed1 =
        EksporteringServiceUtils.getListeAvVirksomhetEksportPerKvartal(
            __2020_2,
            EksporteringBegrensning.build().medBegrensning(1),
            eksporteringRepository
        );
    List<VirksomhetEksportPerKvartal> begrensetMed100 =
        EksporteringServiceUtils.getListeAvVirksomhetEksportPerKvartal(
            __2020_2,
            EksporteringBegrensning.build().medBegrensning(100),
            eksporteringRepository
        );

    assertThat(ikkeBegrenset.size()).isEqualTo(2);
    assertThat(begrensetMed1.size()).isEqualTo(1);
    assertThat(begrensetMed100.size()).isEqualTo(2);
  }



  private List<VirksomhetEksportPerKvartal> bigList(int antallEksportertIsTrue,
      int antallEksportertIsFalse) {
    List<VirksomhetEksportPerKvartal> list = new ArrayList<>();

    IntStream.range(0, antallEksportertIsTrue).forEach(i ->
        list.add(new VirksomhetEksportPerKvartal(
            new Orgnr(UUID.randomUUID().toString().substring(0, 9)),
            __2020_2,
            true
        )));

    IntStream.range(0, antallEksportertIsFalse).forEach(i ->
        list.add(new VirksomhetEksportPerKvartal(
            new Orgnr(UUID.randomUUID().toString().substring(0, 9)),
            __2020_2,
            false
        )));

    return list;
  }

}
