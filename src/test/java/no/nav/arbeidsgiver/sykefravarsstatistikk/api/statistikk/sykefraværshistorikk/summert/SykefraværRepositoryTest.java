package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import org.junit.jupiter.api.Test;
import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository.sorterKronologisk;
import static org.assertj.core.api.Assertions.assertThat;

public class SykefraværRepositoryTest {

  static UmaskertSykefraværForEttKvartal SF_Q2_2021 =
      new UmaskertSykefraværForEttKvartal(new ÅrstallOgKvartal(2021, 2), 15, 100, 10);

  static UmaskertSykefraværForEttKvartal SF_Q4_2021 =
      new UmaskertSykefraværForEttKvartal(new ÅrstallOgKvartal(2021, 4), 15, 100, 10);

  static UmaskertSykefraværForEttKvartal SF_Q1_2022 =
      new UmaskertSykefraværForEttKvartal(new ÅrstallOgKvartal(2022, 1), 15, 100, 10);

  static UmaskertSykefraværForEttKvartal SF_Q2_2022 =
      new UmaskertSykefraværForEttKvartal(new ÅrstallOgKvartal(2022, 2), 15, 100, 10);
  static UmaskertSykefraværForEttKvartal SF_Q3_2022 =
      new UmaskertSykefraværForEttKvartal(new ÅrstallOgKvartal(2022, 3), 15, 100, 10);

  static List<UmaskertSykefraværForEttKvartal> FRA_Q4_2021_TIL_Q3_2022 =
      List.of(SF_Q4_2021, SF_Q1_2022, SF_Q2_2022, SF_Q3_2022);

  static List<UmaskertSykefraværForEttKvartal> FRA_Q2_2021_TIL_Q1_2022__MANGLER_ET_KVARTAL =
      List.of(SF_Q2_2021, SF_Q4_2021, SF_Q1_2022);

  @Test
  public void sorterKronologisk__returnerer_en_sortert_liste_av_SF_hvor_nyeste_kvartal_er_sist() {
    assertThat(sorterKronologisk(List.of(SF_Q4_2021, SF_Q1_2022, SF_Q3_2022, SF_Q2_2022)))
        .isEqualTo(FRA_Q4_2021_TIL_Q3_2022);
  }

  @Test
  public void
      sorterKronologisk__sorterer_til_ny_liste_uavhengig_av_antall_elementer_som_skal_sorteres() {
    assertThat(sorterKronologisk(List.of()).size()).isEqualTo(0);
    assertThat(sorterKronologisk(List.of(SF_Q4_2021))).isEqualTo(List.of(SF_Q4_2021));
    assertThat(sorterKronologisk(List.of(SF_Q1_2022, SF_Q4_2021)))
        .isEqualTo(List.of(SF_Q4_2021, SF_Q1_2022));
  }

  @Test
  public void sorterKronologisk__sorterer_Sykefraværsstatisikk_selv_om_det_er_hul_i_sekvensen() {
    assertThat(sorterKronologisk(List.of(SF_Q4_2021, SF_Q1_2022, SF_Q2_2021)))
        .isEqualTo(FRA_Q2_2021_TIL_Q1_2022__MANGLER_ET_KVARTAL);
  }
}
