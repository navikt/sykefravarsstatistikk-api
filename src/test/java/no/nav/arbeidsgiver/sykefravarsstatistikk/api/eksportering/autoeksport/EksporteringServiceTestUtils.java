package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.AssertUtils.assertBigDecimalIsEqual;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetEksportPerKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadata;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.Sykefraværsstatistikk;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkSektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhetUtenVarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.VirksomhetSykefravær;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;

public class EksporteringServiceTestUtils {

  // Data for testing & Utilities
  public static ÅrstallOgKvartal __2021_2 = new ÅrstallOgKvartal(2021, 2);
  public static ÅrstallOgKvartal __2021_1 = new ÅrstallOgKvartal(2021, 1);
  public static ÅrstallOgKvartal __2020_4 = new ÅrstallOgKvartal(2020, 4);
  public static ÅrstallOgKvartal __2020_2 = new ÅrstallOgKvartal(2020, 2);
  public static ÅrstallOgKvartal __2020_1 = new ÅrstallOgKvartal(2020, 1);
  public static ÅrstallOgKvartal __2019_4 = new ÅrstallOgKvartal(2019, 4);
  public static ÅrstallOgKvartal __2019_3 = new ÅrstallOgKvartal(2019, 3);
  public static Orgnr ORGNR_VIRKSOMHET_1 = new Orgnr("987654321");
  public static Orgnr ORGNR_VIRKSOMHET_2 = new Orgnr("912345678");
  public static Orgnr ORGNR_VIRKSOMHET_3 = new Orgnr("999966633");

  public static VirksomhetSykefravær virksomhetSykefravær =
      new VirksomhetSykefravær(
          "987654321", "Virksomhet 1", __2020_2, new BigDecimal(10), new BigDecimal(500), 6);
  public static SykefraværMedKategori næringSykefravær =
      new SykefraværMedKategori(
          Statistikkategori.NÆRING, "11", __2020_2, new BigDecimal(100), new BigDecimal(5000), 150);

  public static SykefraværMedKategori næring5SifferSykefravær =
      new SykefraværMedKategori(
          Statistikkategori.NÆRING5SIFFER,
          "11000",
          __2020_2,
          new BigDecimal(40),
          new BigDecimal(4000),
          1250);
  public static SykefraværMedKategori næring5SifferSykefraværTilhørerBransje =
      new SykefraværMedKategori(
          Statistikkategori.NÆRING5SIFFER,
          "86101",
          __2020_2,
          new BigDecimal(80),
          new BigDecimal(6000),
          1000);
  public static SykefraværMedKategori sektorSykefravær =
      new SykefraværMedKategori(
          Statistikkategori.SEKTOR,
          "1",
          __2020_2,
          new BigDecimal(1340),
          new BigDecimal(88000),
          33000);
  public static SykefraværMedKategori landSykefravær =
      new SykefraværMedKategori(
          Statistikkategori.LAND,
          "NO",
          __2020_2,
          new BigDecimal(10000000),
          new BigDecimal(500000000),
          2500000);
  public static SykefraværMedKategori virksomhetSykefraværMedKategori =
      new SykefraværMedKategori(
          Statistikkategori.VIRKSOMHET,
          "987654321",
          __2020_2,
          new BigDecimal(10),
          new BigDecimal(500),
          2500000);

  public static VirksomhetMetadata virksomhet1Metadata_2020_4 =
      new VirksomhetMetadata(
          ORGNR_VIRKSOMHET_1, "Virksomhet 1", RECTYPE_FOR_VIRKSOMHET, "1", "11", __2020_4);

  public static VirksomhetMetadata virksomhet2Metadata_2020_4 =
      new VirksomhetMetadata(
          ORGNR_VIRKSOMHET_2, "Virksomhet 2", RECTYPE_FOR_VIRKSOMHET, "2", "22", __2020_4);

  public static VirksomhetMetadata virksomhet3Metadata_2020_4 =
      new VirksomhetMetadata(
          ORGNR_VIRKSOMHET_3, "Virksomhet 3", RECTYPE_FOR_VIRKSOMHET, "3", "33", __2020_4);

  public static VirksomhetMetadata virksomhet1Metadata_2021_1 =
      new VirksomhetMetadata(
          ORGNR_VIRKSOMHET_1, "Virksomhet 1", RECTYPE_FOR_VIRKSOMHET, "1", "11", __2021_1);

  public static VirksomhetMetadata virksomhet1Metadata_2021_2 =
      new VirksomhetMetadata(
          ORGNR_VIRKSOMHET_1, "Virksomhet 1", RECTYPE_FOR_VIRKSOMHET, "1", "11", __2021_2);

  public static VirksomhetMetadata virksomhet1_TilHørerBransjeMetadata(
      ÅrstallOgKvartal årstallOgKvartal) {
    return new VirksomhetMetadata(
        ORGNR_VIRKSOMHET_1, "Virksomhet 1", RECTYPE_FOR_VIRKSOMHET, "1", "86", årstallOgKvartal);
  }

  public static SykefraværsstatistikkVirksomhetUtenVarighet byggSykefraværsstatistikkVirksomhet(
      VirksomhetMetadata virksomhetMetadata) {
    return byggSykefraværsstatistikkVirksomhet(virksomhetMetadata, 156, 3678, 188000);
  }

  public static SykefraværsstatistikkVirksomhetUtenVarighet byggSykefraværsstatistikkVirksomhet(
      VirksomhetMetadata virksomhetMetadata,
      int antallPersoner,
      int tapteDagsverk,
      int muligeDagsverk) {
    return new SykefraværsstatistikkVirksomhetUtenVarighet(
        virksomhetMetadata.getÅrstall(),
        virksomhetMetadata.getKvartal(),
        virksomhetMetadata.getOrgnr(),
        antallPersoner,
        new BigDecimal(tapteDagsverk),
        new BigDecimal(muligeDagsverk));
  }

  public static VirksomhetSykefravær tomVirksomhetSykefravær(
      VirksomhetMetadata virksomhetMetadata) {
    return new VirksomhetSykefravær(
        virksomhetMetadata.getOrgnr(),
            virksomhetMetadata.getNavn(),
        new ÅrstallOgKvartal(virksomhetMetadata.getÅrstall(), virksomhetMetadata.getKvartal()),
        null,
        null,
        0);
  }

  public static SykefraværsstatistikkNæring byggSykefraværStatistikkNæring(
      VirksomhetMetadata virksomhetMetadata,
      int antallPersoner,
      int tapteDagsverk,
      int muligeDagsverk) {
    return new SykefraværsstatistikkNæring(
        virksomhetMetadata.getÅrstall(),
        virksomhetMetadata.getKvartal(),
            virksomhetMetadata.getNæring(),
        antallPersoner,
        new BigDecimal(tapteDagsverk),
        new BigDecimal(muligeDagsverk));
  }

  public static SykefraværsstatistikkNæring byggSykefraværStatistikkNæring(
      VirksomhetMetadata virksomhetMetadata) {
    return new SykefraværsstatistikkNæring(
        virksomhetMetadata.getÅrstall(),
        virksomhetMetadata.getKvartal(),
            virksomhetMetadata.getNæring(),
        156,
        new BigDecimal(3678),
        new BigDecimal(188000));
  }

  public static SykefraværsstatistikkNæring byggSykefraværStatistikkNæring(
      VirksomhetMetadata virksomhetMetadata, ÅrstallOgKvartal statistikkÅrstallOgKvartal) {
    return new SykefraværsstatistikkNæring(
        statistikkÅrstallOgKvartal.getÅrstall(),
        statistikkÅrstallOgKvartal.getKvartal(),
            virksomhetMetadata.getNæring(),
        156,
        new BigDecimal(3678),
        new BigDecimal(188000));
  }

  public static SykefraværsstatistikkNæring5Siffer byggSykefraværStatistikkNæring5Siffer(
      VirksomhetMetadata virksomhetMetadata, String næringskode5Siffer) {
    return new SykefraværsstatistikkNæring5Siffer(
        virksomhetMetadata.getÅrstall(),
        virksomhetMetadata.getKvartal(),
        næringskode5Siffer,
        100,
        new BigDecimal(250),
        new BigDecimal(25000));
  }

  public static SykefraværsstatistikkNæring5Siffer byggSykefraværStatistikkNæring5Siffer(
      ÅrstallOgKvartal statistikkÅrstallOgKvartal, String næringskode5Siffer) {
    return new SykefraværsstatistikkNæring5Siffer(
        statistikkÅrstallOgKvartal.getÅrstall(),
        statistikkÅrstallOgKvartal.getKvartal(),
        næringskode5Siffer,
        200,
        new BigDecimal(300),
        new BigDecimal(10000));
  }

  public static SykefraværsstatistikkSektor byggSykefraværStatistikkSektor(
      VirksomhetMetadata virksomhetMetadata,
      int antallPersoner,
      int tapteDagsverk,
      int muligeDagsverk) {
    return new SykefraværsstatistikkSektor(
        virksomhetMetadata.getÅrstall(),
        virksomhetMetadata.getKvartal(),
            virksomhetMetadata.getSektor(),
        antallPersoner,
        new BigDecimal(tapteDagsverk),
        new BigDecimal(muligeDagsverk));
  }

  public static SykefraværsstatistikkSektor byggSykefraværStatistikkSektor(
      VirksomhetMetadata virksomhetMetadata) {
    return new SykefraværsstatistikkSektor(
        virksomhetMetadata.getÅrstall(),
        virksomhetMetadata.getKvartal(),
            virksomhetMetadata.getSektor(),
        156,
        new BigDecimal(3678),
        new BigDecimal(188000));
  }

  public static VirksomhetSykefravær byggVirksomhetSykefravær(
      VirksomhetMetadata virksomhetMetadata,
      int antallPersoner,
      int tapteDagsverk,
      int muligeDagsverk) {
    return new VirksomhetSykefravær(
        virksomhetMetadata.getOrgnr(),
            virksomhetMetadata.getNavn(),
        new ÅrstallOgKvartal(virksomhetMetadata.getÅrstall(), virksomhetMetadata.getKvartal()),
        new BigDecimal(tapteDagsverk),
        new BigDecimal(muligeDagsverk),
        antallPersoner);
  }

  public static VirksomhetSykefravær byggVirksomhetSykefravær(
      VirksomhetMetadata virksomhetMetadata) {
    return new VirksomhetSykefravær(
        virksomhetMetadata.getOrgnr(),
            virksomhetMetadata.getNavn(),
        new ÅrstallOgKvartal(virksomhetMetadata.getÅrstall(), virksomhetMetadata.getKvartal()),
        new BigDecimal(3678),
        new BigDecimal(188000),
        156);
  }

  public static SykefraværsstatistikkVirksomhetUtenVarighet sykefraværsstatistikkVirksomhet(
      ÅrstallOgKvartal årstallOgKvartal, String orgnr) {
    return new SykefraværsstatistikkVirksomhetUtenVarighet(
        årstallOgKvartal.getÅrstall(),
        årstallOgKvartal.getKvartal(),
        orgnr,
        6,
        new BigDecimal(10),
        new BigDecimal(500));
  }

  public static VirksomhetEksportPerKvartal virksomhetEksportPerKvartal =
      new VirksomhetEksportPerKvartal(new Orgnr("987654321"), __2020_2, false);
  public static VirksomhetMetadata virksomhetMetadata =
      new VirksomhetMetadata(new Orgnr("987654321"), "Virksomhet 1", "2", "1", "11", __2020_2);

  public static List<UmaskertSykefraværForEttKvartal> sykefraværsstatistikkLandSiste4Kvartaler(
      ÅrstallOgKvartal årstallOgKvartal) {
    return List.of(
        new UmaskertSykefraværForEttKvartal(
            årstallOgKvartal, new BigDecimal(10000000), new BigDecimal(500000000), 2500000),
        new UmaskertSykefraværForEttKvartal(
            årstallOgKvartal.minusKvartaler(1),
            new BigDecimal(9000000),
            new BigDecimal(500000000),
            2500000),
        new UmaskertSykefraværForEttKvartal(
            årstallOgKvartal.minusKvartaler(2),
            new BigDecimal(11000000),
            new BigDecimal(500000000),
            2500000),
        new UmaskertSykefraværForEttKvartal(
            årstallOgKvartal.minusKvartaler(3),
            new BigDecimal(8000000),
            new BigDecimal(500000000),
            2500000));
  }

  public static SykefraværsstatistikkSektor sykefraværsstatistikkSektor =
      new SykefraværsstatistikkSektor(
          __2020_2.getÅrstall(),
          __2020_2.getKvartal(),
          "1",
          33000,
          new BigDecimal(1340),
          new BigDecimal(88000));
  public static SykefraværsstatistikkNæring sykefraværsstatistikkNæring =
      new SykefraværsstatistikkNæring(
          __2020_2.getÅrstall(),
          __2020_2.getKvartal(),
          "11",
          150,
          new BigDecimal(100),
          new BigDecimal(5000));

  public static SykefraværsstatistikkNæring sykefraværsstatistikkNæring(
      ÅrstallOgKvartal årstallOgKvartal) {
    return sykefraværsstatistikkNæring(årstallOgKvartal, "11");
  }

  public static SykefraværsstatistikkNæring sykefraværsstatistikkNæring(
      ÅrstallOgKvartal årstallOgKvartal, String næringskode) {
    return new SykefraværsstatistikkNæring(
        årstallOgKvartal.getÅrstall(),
        årstallOgKvartal.getKvartal(),
        næringskode,
        150,
        new BigDecimal(100),
        new BigDecimal(5000));
  }

  public static SykefraværsstatistikkNæring5Siffer sykefraværsstatistikkNæring5Siffer =
      new SykefraværsstatistikkNæring5Siffer(
          __2020_2.getÅrstall(),
          __2020_2.getKvartal(),
          "11000",
          1250,
          new BigDecimal(40),
          new BigDecimal(4000));

  public static SykefraværsstatistikkNæring5Siffer sykefraværsstatistikkNæring5SifferBransjeprogram(
      String næringskode5Siffer, ÅrstallOgKvartal årstallOgKvartal) {
    return new SykefraværsstatistikkNæring5Siffer(
        årstallOgKvartal.getÅrstall(),
        årstallOgKvartal.getKvartal(),
        næringskode5Siffer,
        1000,
        new BigDecimal(80),
        new BigDecimal(6000));
  }

  public static void assertEqualsSykefraværFlereKvartalerForEksport(
      SykefraværFlereKvartalerForEksport expected, SykefraværFlereKvartalerForEksport actual) {
    assertThat(actual.getProsent()).as("Sjekk prosent").isEqualByComparingTo(expected.getProsent());
    assertThat(actual.getKvartaler())
        .as("Sjekk listen av kvartaler")
        .isEqualTo(expected.getKvartaler());
    assertBigDecimalIsEqual(actual.getMuligeDagsverk(), expected.getMuligeDagsverk());
    assertBigDecimalIsEqual(actual.getTapteDagsverk(), expected.getTapteDagsverk());
  }

  public static void assertEqualsVirksomhetSykefravær(
      VirksomhetSykefravær expected, VirksomhetSykefravær actual) {
    assertThat(actual.getÅrstall()).isEqualTo(expected.getÅrstall());
    assertThat(actual.getKvartal()).isEqualTo(expected.getKvartal());
    assertThat(actual.getOrgnr()).isEqualTo(expected.getOrgnr());
    assertBigDecimalIsEqual(actual.getMuligeDagsverk(), expected.getMuligeDagsverk());
    assertBigDecimalIsEqual(actual.getTapteDagsverk(), expected.getTapteDagsverk());
  }

  public static void assertEqualsSykefraværMedKategori(
      Sykefraværsstatistikk expected,
      SykefraværMedKategori actual,
      Statistikkategori expectedKategori,
      String expectedKode) {
    assertThat(actual.getKategori()).as("Sjekk Statistikkategori").isEqualTo(expectedKategori);
    assertThat(actual.getKode()).as("Sjekk kode").isEqualTo(expectedKode);
    assertThat(actual.getÅrstall()).as("Sjekk årstall").isEqualTo(expected.getÅrstall());
    assertThat(actual.getKvartal()).as("Sjekk kvartal").isEqualTo(expected.getKvartal());
    assertBigDecimalIsEqual(actual.getMuligeDagsverk(), expected.getMuligeDagsverk());
    assertBigDecimalIsEqual(actual.getTapteDagsverk(), expected.getTapteDagsverk());
  }

  public static void assertEqualsSykefraværMedKategori(
      SykefraværMedKategori expected, SykefraværMedKategori actual) {
    assertThat(actual.getKategori())
        .as("Sjekk Statistikkategori")
        .isEqualTo(expected.getKategori());
    assertThat(actual.getKode()).as("Sjekk kode").isEqualTo(expected.getKode());
    assertThat(actual.getÅrstall()).as("Sjekk årstall").isEqualTo(expected.getÅrstall());
    assertThat(actual.getKvartal()).as("Sjekk kvartal").isEqualTo(expected.getKvartal());
    assertBigDecimalIsEqual(actual.getMuligeDagsverk(), expected.getMuligeDagsverk());
    assertBigDecimalIsEqual(actual.getTapteDagsverk(), expected.getTapteDagsverk());
  }
}
