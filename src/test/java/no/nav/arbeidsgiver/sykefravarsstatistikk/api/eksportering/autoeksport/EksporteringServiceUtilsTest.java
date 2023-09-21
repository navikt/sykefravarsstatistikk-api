package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.SISTE_PUBLISERTE_KVARTAL;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.EksporteringServiceUtils.*;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.*;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EksporteringServiceUtilsTest {

  @Test
  public void getVirksomhetMetadataHashMap__returnerer_en_map_med_orgnr_som_key() {
    Map<String, VirksomhetMetadata> virksomhetMetadataHashMap =
        getVirksomhetMetadataHashMap(
            Arrays.asList(virksomhet1Metadata_2020_4, virksomhet2Metadata_2020_4));

    assertEquals(2, virksomhetMetadataHashMap.size());
    assertEquals(
        virksomhet1Metadata_2020_4,
        virksomhetMetadataHashMap.get(virksomhet1Metadata_2020_4.getOrgnr()));
    assertEquals(
        virksomhet2Metadata_2020_4,
        virksomhetMetadataHashMap.get(virksomhet2Metadata_2020_4.getOrgnr()));
  }

  @Test
  public void getVirksomheterMetadataFraSubset__returnerer_intersection() {
    Map<String, VirksomhetMetadata> virksomhetMetadataHashMap = new HashMap<>();
    virksomhetMetadataHashMap.put(
        virksomhet1Metadata_2020_4.getOrgnr(), virksomhet1Metadata_2020_4);
    virksomhetMetadataHashMap.put(
        virksomhet2Metadata_2020_4.getOrgnr(), virksomhet2Metadata_2020_4);
    virksomhetMetadataHashMap.put(
        virksomhet3Metadata_2020_4.getOrgnr(), virksomhet3Metadata_2020_4);

    List<VirksomhetMetadata> virksomhetMetadataList =
        getVirksomheterMetadataFraSubset(
            virksomhetMetadataHashMap,
            Arrays.asList(
                new VirksomhetEksportPerKvartal(ORGNR_VIRKSOMHET_1, __2020_4, false),
                new VirksomhetEksportPerKvartal(ORGNR_VIRKSOMHET_2, __2020_4, false)));

    assertEquals(2, virksomhetMetadataList.size());
    assertTrue(virksomhetMetadataList.contains(virksomhet1Metadata_2020_4));
    assertTrue(virksomhetMetadataList.contains(virksomhet2Metadata_2020_4));
  }


  @Test
  public void
      getVirksomhetSykefravær__returnerer_VirksomhetSykefravær_uten_statistikk__dersom_ingen_entry_matcher_Virksomhet() {
    VirksomhetSykefravær actualVirksomhetSykefravær =
        getVirksomhetSykefravær(
            virksomhet1Metadata_2020_4, buildMapAvSykefraværsstatistikkPerVirksomhet(10));

    VirksomhetSykefravær expectedVirksomhetSykefravær =
        new VirksomhetSykefravær(
            virksomhet1Metadata_2020_4.getOrgnr(),
                virksomhet1Metadata_2020_4.getNavn(),
            2020,
            4,
            null,
            null,
            0);
    assertEqualsVirksomhetSykefravær(expectedVirksomhetSykefravær, actualVirksomhetSykefravær);
  }

  @Test
  public void getVirksomhetSykefravær__returnerer_VirksomhetSykefravær_som_matcher_Virksomhet() {
    VirksomhetMetadata virksomhetToBeFound =
        new VirksomhetMetadata(
            new Orgnr("399000"), "Virksomhet 1", RECTYPE_FOR_VIRKSOMHET, Sektor.STATLIG, "11", "11111", __2020_4);
    Map<String, SykefraværsstatistikkVirksomhetUtenVarighet> bigMap =
        buildMapAvSykefraværsstatistikkPerVirksomhet(500000);

    long startWithMap = System.nanoTime();
    VirksomhetSykefravær actualVirksomhetSykefravær =
        getVirksomhetSykefravær(virksomhetToBeFound, bigMap);
    long stopWithMap = System.nanoTime();

    VirksomhetSykefravær expectedVirksomhetSykefravær =
        new VirksomhetSykefravær(
            virksomhetToBeFound.getOrgnr(),
                virksomhetToBeFound.getNavn(),
            2020,
            4,
            new BigDecimal(100),
            new BigDecimal(1000),
            10);
    assertEqualsVirksomhetSykefravær(expectedVirksomhetSykefravær, actualVirksomhetSykefravær);
    System.out.println("Elapsed time in nanoseconds (With Map) = " + (stopWithMap - startWithMap));
  }


  @Test
  public void
      getSykefraværMedKategoriForSektor__returnerer_SykefraværMedKategori__med_sykefraværsstatistikk_for_sektor() {
    SykefraværMedKategori resultat =
        getSykefraværMedKategoriForSektor(
            virksomhet1Metadata_2020_4,
            Arrays.asList(
                byggSykefraværStatistikkSektor(virksomhet1Metadata_2020_4, 10, 156, 22233),
                byggSykefraværStatistikkSektor(virksomhet2Metadata_2020_4)));

    EksporteringServiceTestUtils.assertEqualsSykefraværMedKategori(
        byggSykefraværStatistikkSektor(virksomhet1Metadata_2020_4, 10, 156, 22233),
        resultat,
        Statistikkategori.SEKTOR,
            virksomhet1Metadata_2020_4.getSektor().getSektorkode());
  }

  @Test
  public void
      getSykefraværMedKategoriForNæring__returnerer_SykefraværMedKategori__med_sykefraværsstatistikk_for_næring() {
    SykefraværMedKategori resultat =
        getSykefraværMedKategoriNæringForVirksomhet(
            virksomhet1Metadata_2020_4,
            Arrays.asList(
                byggSykefraværStatistikkNæring(virksomhet1Metadata_2020_4, 10, 156, 22233),
                byggSykefraværStatistikkNæring(virksomhet2Metadata_2020_4)));

    EksporteringServiceTestUtils.assertEqualsSykefraværMedKategori(
        byggSykefraværStatistikkNæring(virksomhet1Metadata_2020_4, 10, 156, 22233),
        resultat,
        Statistikkategori.NÆRING,
            virksomhet1Metadata_2020_4.getPrimærnæring());
  }

  @Test
  public void
      getSykefraværMedKategoriForNæring5Siffer__returnerer_SykefraværMedKategori__med_sykefraværsstatistikk_for_næring_5_siffer() {
    virksomhet1Metadata_2020_4.leggTilNæringOgNæringskode5siffer(
        Arrays.asList(
                new Næringskode("85000"),
                new Næringskode("11000")
        ));
    List<SykefraværMedKategori> resultat =
        getSykefraværMedKategoriForNæring5Siffer(
            virksomhet1Metadata_2020_4,
            Arrays.asList(
                byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "11000"),
                byggSykefraværStatistikkNæring5Siffer(virksomhet2Metadata_2020_4, "85000")));

    assertThat(resultat.size()).isEqualTo(2);
    SykefraværMedKategori sykefraværMedKategori85000 =
        resultat.stream().filter(r -> "85000".equals(r.kode)).findFirst().get();

    EksporteringServiceTestUtils.assertEqualsSykefraværMedKategori(
        byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "85000"),
        sykefraværMedKategori85000,
        Statistikkategori.NÆRINGSKODE,
            virksomhet1Metadata_2020_4.getNæringOgNæringskode5siffer().get(0).getFemsifferIdentifikator());
    SykefraværMedKategori sykefraværMedKategori11000 =
        resultat.stream().filter(r -> "11000".equals(r.kode)).findFirst().get();

    EksporteringServiceTestUtils.assertEqualsSykefraværMedKategori(
        byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "11000"),
        sykefraværMedKategori11000,
        Statistikkategori.NÆRINGSKODE,
            virksomhet1Metadata_2020_4.getNæringOgNæringskode5siffer().get(1).getFemsifferIdentifikator());
  }

  @Test
  public void getSykefraværsstatistikkNæring5Siffers__skal_returnere_riktig_liste() {
    VirksomhetMetadata virksomhetMetadata_2020_4_med_næring5siffer = virksomhet1Metadata_2020_4;
    virksomhetMetadata_2020_4_med_næring5siffer.leggTilNæringOgNæringskode5siffer(
        Arrays.asList(
                new Næringskode("11000"),
                new Næringskode("85000")));
    List<SykefraværsstatistikkForNæringskode> sykefraværsstatistikkForNæringskodeList =
        Arrays.asList(
            byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "11000"),
            byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "45210"),
            byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "85000"));
    List<SykefraværsstatistikkForNæringskode> resultat =
        getSykefraværsstatistikkNæring5Siffers(
            virksomhetMetadata_2020_4_med_næring5siffer, sykefraværsstatistikkForNæringskodeList);

    assertThat(resultat.size()).isEqualTo(2);
    assertThat(
        resultat.contains(
            byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "11000")));
    assertThat(
        resultat.contains(
            byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "85000")));
    assertThat(
        !resultat.contains(
            byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "45210")));
  }

  @Test
  public void filterByKvartal_skalIkkeFeile() {
    assertThat(
            filterByKvartal(
                SISTE_PUBLISERTE_KVARTAL,
                List.of(byggSykefraværsstatistikkVirksomhet(virksomhet1Metadata_2020_4))))
        .isEmpty();
  }

  @Test
  public void filterByKvartal_skalReturnereRiktigKvartal() {
    List<SykefraværsstatistikkVirksomhetUtenVarighet> ønskedeResultat__2021_2 =
        List.of(byggSykefraværsstatistikkVirksomhet(virksomhet1Metadata_2021_2));
    List<SykefraværsstatistikkVirksomhetUtenVarighet> actual__2021_2 =
        filterByKvartal(
            __2021_2,
            List.of(
                byggSykefraværsstatistikkVirksomhet(virksomhet1Metadata_2020_4),
                byggSykefraværsstatistikkVirksomhet(virksomhet1Metadata_2021_1),
                byggSykefraværsstatistikkVirksomhet(virksomhet1Metadata_2021_2)));
    assertThat(actual__2021_2).size().isEqualTo(1);
    assertThat(actual__2021_2).containsExactlyInAnyOrderElementsOf(ønskedeResultat__2021_2);
  }

  @Test
  public void filterByKvartal_skalReturnereAlleVirksomheterForØnskedeKvartal() {
    List<SykefraværsstatistikkVirksomhetUtenVarighet> ønskedeResultat__2020_4 =
        List.of(
            byggSykefraværsstatistikkVirksomhet(virksomhet1Metadata_2020_4),
            byggSykefraværsstatistikkVirksomhet(virksomhet2Metadata_2020_4));
    List<SykefraværsstatistikkVirksomhetUtenVarighet> actual__2020_4 =
        filterByKvartal(
            __2020_4,
            List.of(
                byggSykefraværsstatistikkVirksomhet(virksomhet1Metadata_2020_4),
                byggSykefraværsstatistikkVirksomhet(virksomhet2Metadata_2020_4),
                byggSykefraværsstatistikkVirksomhet(virksomhet1Metadata_2021_1),
                byggSykefraværsstatistikkVirksomhet(virksomhet1Metadata_2021_2)));
    assertThat(actual__2020_4).size().isEqualTo(2);
    assertThat(actual__2020_4).containsExactlyInAnyOrderElementsOf(ønskedeResultat__2020_4);
  }

  private static Map<String, SykefraværsstatistikkVirksomhetUtenVarighet>
      buildMapAvSykefraværsstatistikkPerVirksomhet(int size) {
    Map<String, SykefraværsstatistikkVirksomhetUtenVarighet>
        sykefraværsstatistikkVirksomhetUtenVarighetMap = new HashMap<>();

    int count = 1;
    do {
      sykefraværsstatistikkVirksomhetUtenVarighetMap.put(
          Integer.toString(count),
          new SykefraværsstatistikkVirksomhetUtenVarighet(
              2020, 4, Integer.toString(count), 10, new BigDecimal(100), new BigDecimal(1000)));
      count++;
    } while (count < size);
    return sykefraværsstatistikkVirksomhetUtenVarighetMap;
  }
}
