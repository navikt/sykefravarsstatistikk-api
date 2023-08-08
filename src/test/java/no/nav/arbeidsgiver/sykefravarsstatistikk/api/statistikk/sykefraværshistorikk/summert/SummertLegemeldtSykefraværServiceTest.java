package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.dataTilFrontends.SummertLegemeldtSykefraværService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.UnderenhetLegacy;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Virksomhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.BransjeEllerNæringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.KlassifikasjonerRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.LegemeldtSykefraværsprosent;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.UmaskertSykefraværForEttKvartal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SummertLegemeldtSykefraværServiceTest {

  @Mock private SykefraværRepository sykefraværRepository;

  @Mock private KlassifikasjonerRepository klassifikasjonerRepository;

  private SummertLegemeldtSykefraværService summertLegemeldtSykefraværService;

  @BeforeEach
  public void setUp() {
    summertLegemeldtSykefraværService =
        new SummertLegemeldtSykefraværService(
            sykefraværRepository,
            new BransjeEllerNæringService(klassifikasjonerRepository));
  }

  @Test
  public void legemeldtSykefraværsprosent_utleddes_fra_siste_4_kvartaler() {
    lagTestDataTilRepository();
    UnderenhetLegacy underenhet =
        new UnderenhetLegacy(
            new Orgnr("987654321"),
            new Orgnr("999888777"),
            "Test underenhet 2",
            new Næringskode5Siffer("88911", "Barnehager"),
            15);

    LegemeldtSykefraværsprosent legemeldtSykefraværsprosent =
        summertLegemeldtSykefraværService.hentLegemeldtSykefraværsprosent(
            underenhet, new ÅrstallOgKvartal(2021, 2));

    assertThat(legemeldtSykefraværsprosent).isNotNull();
    assertThat(legemeldtSykefraværsprosent.getType()).isEqualTo(Statistikkategori.VIRKSOMHET);
    assertThat(legemeldtSykefraværsprosent.getLabel()).isEqualTo("Test underenhet 2");
    assertThat(legemeldtSykefraværsprosent.getProsent()).isEqualTo(new BigDecimal("10.5"));
  }

  @Test
  public void
      legemeldtSykefraværsprosent_henter_bransje_sykefraværssprosent_dersom_prosent_er_masker_for_bedriften() {
    lagTestDataTilRepository(4);
    UnderenhetLegacy underenhet =
        new UnderenhetLegacy(
            new Orgnr("987654321"),
            new Orgnr("999888777"),
            "Test underenhet 2",
            new Næringskode5Siffer("88911", "Barnehager"),
            15);

    LegemeldtSykefraværsprosent legemeldtSykefraværsprosent =
        summertLegemeldtSykefraværService.hentLegemeldtSykefraværsprosent(
            underenhet, new ÅrstallOgKvartal(2021, 2));

    assertThat(legemeldtSykefraværsprosent).isNotNull();
    assertThat(legemeldtSykefraværsprosent.getType()).isEqualTo(Statistikkategori.BRANSJE);
    assertThat(legemeldtSykefraværsprosent.getLabel()).isEqualTo("Barnehager");
    assertThat(legemeldtSykefraværsprosent.getProsent()).isEqualTo(new BigDecimal("10.5"));
  }

  @Test
  public void
      legemeldtSykefraværsprosent_for_virksomheter_som_ikke_har_data_skal_returnere_bransje__dersom_virksomhet_er_i_bransjeprogram() {
    lagTestDataTilRepositoryForBransje();
    LegemeldtSykefraværsprosent legemeldtSykefraværsprosent =
        summertLegemeldtSykefraværService.hentLegemeldtSykefraværsprosent(
            new UnderenhetLegacy(
                new Orgnr("456456456"),
                new Orgnr("654654654"),
                "Underenhet uten data i DB",
                new Næringskode5Siffer("88911", "Barnehager"),
                0),
            new ÅrstallOgKvartal(2021, 2));

    assertThat(legemeldtSykefraværsprosent).isNotNull();
    assertThat(legemeldtSykefraværsprosent.getType()).isEqualTo(Statistikkategori.BRANSJE);
    assertThat(legemeldtSykefraværsprosent.getLabel()).isEqualTo("Barnehager");
    assertThat(legemeldtSykefraværsprosent.getProsent()).isEqualTo(new BigDecimal("8.5"));
  }

  @Test
  public void
      legemeldtSykefraværsprosent_for_virksomheter_som_ikke_har_data_skal_returnere_næring__dersom_virksomhet_ikke_er_i_bransjeprogram() {
    lagTestDataTilRepositoryForNæring();
    LegemeldtSykefraværsprosent legemeldtSykefraværsprosent =
        summertLegemeldtSykefraværService.hentLegemeldtSykefraværsprosent(
            new UnderenhetLegacy(
                new Orgnr("456456456"),
                new Orgnr("654654654"),
                "Underenhet uten data i DB",
                new Næringskode5Siffer("88913", "Skolefritidsordninger"),
                0),
            new ÅrstallOgKvartal(2021, 2));

    assertThat(legemeldtSykefraværsprosent).isNotNull();
    assertThat(legemeldtSykefraværsprosent.getType()).isEqualTo(Statistikkategori.NÆRING);
    assertThat(legemeldtSykefraværsprosent.getLabel()).isEqualTo("Skolefritidsordninger");
    assertThat(legemeldtSykefraværsprosent.getProsent()).isEqualTo(new BigDecimal("5.5"));
  }

  private void lagTestDataTilRepository() {
    when(sykefraværRepository.hentUmaskertSykefravær(
            any(Virksomhet.class), any(ÅrstallOgKvartal.class)))
        .thenReturn(
            Arrays.asList(
                umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 2), 11),
                umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 1), 10)));
  }

  private void lagTestDataTilRepository(int antallPersoner) {
    when(sykefraværRepository.hentUmaskertSykefravær(
            any(Virksomhet.class), any(ÅrstallOgKvartal.class)))
        .thenReturn(
            Arrays.asList(
                umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 2), 11, antallPersoner),
                umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 1), 10, antallPersoner)));

    when(sykefraværRepository.hentUmaskertSykefravær(
            any(Bransje.class), any(ÅrstallOgKvartal.class)))
        .thenReturn(
            Arrays.asList(
                umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 2), 11),
                umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 1), 10)));
  }

  private void lagTestDataTilRepositoryForBransje() {
    when(sykefraværRepository.hentUmaskertSykefravær(
            any(Virksomhet.class), any(ÅrstallOgKvartal.class)))
        .thenReturn(Collections.emptyList());

    when(sykefraværRepository.hentUmaskertSykefravær(
            any(Bransje.class), any(ÅrstallOgKvartal.class)))
        .thenReturn(
            Arrays.asList(
                umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 2), 8),
                umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 1), 9)));
  }

  private void lagTestDataTilRepositoryForNæring() {
    when(sykefraværRepository.hentUmaskertSykefravær(
            any(Virksomhet.class), any(ÅrstallOgKvartal.class)))
        .thenReturn(Collections.emptyList());

    when(sykefraværRepository.hentUmaskertSykefravær(
            any(Næring.class), any(ÅrstallOgKvartal.class)))
        .thenReturn(
            Arrays.asList(
                umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 2), 5),
                umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 1), 6)));
    when(klassifikasjonerRepository.hentNæring(any()))
        .thenReturn(new Næring("88913", "Skolefritidsordninger"));
  }

  private static UmaskertSykefraværForEttKvartal umaskertSykefraværprosent(
      ÅrstallOgKvartal årstallOgKvartal, double prosent, int antallPersoner) {
    return new UmaskertSykefraværForEttKvartal(
        årstallOgKvartal, new BigDecimal(prosent), new BigDecimal(100), antallPersoner);
  }

  private static UmaskertSykefraværForEttKvartal umaskertSykefraværprosent(
      ÅrstallOgKvartal årstallOgKvartal, double prosent) {
    return umaskertSykefraværprosent(årstallOgKvartal, prosent, 10);
  }
}
