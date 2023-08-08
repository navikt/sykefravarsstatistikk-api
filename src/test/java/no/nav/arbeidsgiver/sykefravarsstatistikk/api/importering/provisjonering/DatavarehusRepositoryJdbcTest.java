package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.NÆRINGSKODE_2SIFFER;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.NÆRINGSKODE_5SIFFER;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.ORGNR_VIRKSOMHET_1;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.ORGNR_VIRKSOMHET_2;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.ORGNR_VIRKSOMHET_3;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.SEKTOR;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository.RECTYPE_FOR_FORETAK;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering.DatavarehusRepositoryJdbcTestUtils.cleanUpTestDb;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering.DatavarehusRepositoryJdbcTestUtils.insertNæringInDvhTabell;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering.DatavarehusRepositoryJdbcTestUtils.insertOrgenhetInDvhTabell;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering.DatavarehusRepositoryJdbcTestUtils.insertSektorInDvhTabell;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering.DatavarehusRepositoryJdbcTestUtils.insertSykefraværsstatistikkLandInDvhTabell;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering.DatavarehusRepositoryJdbcTestUtils.insertSykefraværsstatistikkNærin5SiffergInDvhTabell;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering.DatavarehusRepositoryJdbcTestUtils.insertSykefraværsstatistikkNæringInDvhTabell;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering.DatavarehusRepositoryJdbcTestUtils.insertSykefraværsstatistikkVirksomhetGraderingInDvhTabell;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering.DatavarehusRepositoryJdbcTestUtils.insertSykefraværsstatistikkVirksomhetInDvhTabell;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Varighetskategori._1_DAG_TIL_7_DAGER;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Varighetskategori._8_DAGER_TIL_16_DAGER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.AppConfigForJdbcTesterConfig;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Sektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.StatistikkildeDvh;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.SykefraværsstatistikkLand;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.SykefraværsstatistikkNæringMedVarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.SykefraværsstatistikkSektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.SykefraværsstatistikkVirksomhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.SykefraværsstatistikkVirksomhetMedGradering;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Orgenhet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfigForJdbcTesterConfig.class})
@DataJdbcTest(excludeAutoConfiguration = {TestDatabaseAutoConfiguration.class})
public class DatavarehusRepositoryJdbcTest {

  @Autowired
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  private DatavarehusRepository repository;

  @BeforeEach
  public void setUp() {
    repository = new DatavarehusRepository(namedParameterJdbcTemplate);
    cleanUpTestDb(namedParameterJdbcTemplate);
  }

  @AfterEach
  public void tearDown() {
    cleanUpTestDb(namedParameterJdbcTemplate);
  }

  @Test
  public void
  hentSisteÅrstallOgKvartalFraSykefraværsstatistikk__returnerer_siste_ÅrstallOgKvartal_for_Land_og_sektor() {
    insertSykefraværsstatistikkLandInDvhTabell(namedParameterJdbcTemplate, 2019, 4, 4, 5, 100);
    insertSykefraværsstatistikkLandInDvhTabell(namedParameterJdbcTemplate, 2019, 4, 6, 10, 100);
    insertSykefraværsstatistikkLandInDvhTabell(namedParameterJdbcTemplate, 2020, 1, 1, 1, 10);

    ÅrstallOgKvartal sisteÅrstallOgKvartal =
        repository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(
            StatistikkildeDvh.LAND_OG_SEKTOR);

    assertThat(sisteÅrstallOgKvartal).isEqualTo(new ÅrstallOgKvartal(2020, 1));
  }

  @Test
  public void
  hentSisteÅrstallOgKvartalFraSykefraværsstatistikk__returnerer_siste_ÅrstallOgKvartal_for_Næring() {
    insertSykefraværsstatistikkNæringInDvhTabell(
        namedParameterJdbcTemplate, 2019, 4, 4, "23", "K", 5, 100);
    insertSykefraværsstatistikkNæringInDvhTabell(
        namedParameterJdbcTemplate, 2022, 3, 2, "90", "M", 12, 100);

    ÅrstallOgKvartal sisteÅrstallOgKvartal =
        repository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(StatistikkildeDvh.NÆRING);

    assertThat(sisteÅrstallOgKvartal).isEqualTo(new ÅrstallOgKvartal(2022, 3));
  }

  @Test
  public void
  hentSisteÅrstallOgKvartalFraSykefraværsstatistikk__returnerer_siste_ÅrstallOgKvartal_for_Næring5Siffer() {
    insertSykefraværsstatistikkNærin5SiffergInDvhTabell(
        namedParameterJdbcTemplate, 2022, 3, 4, "01110", "K", 5, 100);
    insertSykefraværsstatistikkNærin5SiffergInDvhTabell(
        namedParameterJdbcTemplate, 2020, 1, 2, "01110", "M", 12, 100);

    ÅrstallOgKvartal sisteÅrstallOgKvartal =
        repository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(
            StatistikkildeDvh.NÆRING_5_SIFFER);

    assertThat(sisteÅrstallOgKvartal).isEqualTo(new ÅrstallOgKvartal(2022, 3));
  }

  @Test
  public void
  hentSisteÅrstallOgKvartalFraSykefraværsstatistikk__returnerer_siste_ÅrstallOgKvartal_for_Virksomhet() {
    insertSykefraværsstatistikkVirksomhetInDvhTabell(
        namedParameterJdbcTemplate,
        2018,
        4,
        4,
        ORGNR_VIRKSOMHET_1,
        "10062",
        _8_DAGER_TIL_16_DAGER,
        "K",
        5,
        100);
    insertSykefraværsstatistikkVirksomhetInDvhTabell(
        namedParameterJdbcTemplate,
        2019,
        1,
        5,
        ORGNR_VIRKSOMHET_2,
        "10062",
        _8_DAGER_TIL_16_DAGER,
        "M",
        5,
        101);

    ÅrstallOgKvartal sisteÅrstallOgKvartal =
        repository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(StatistikkildeDvh.VIRKSOMHET);

    assertThat(sisteÅrstallOgKvartal).isEqualTo(new ÅrstallOgKvartal(2019, 1));
  }

  @Test
  public void
  hentSykefraværsstatistikkSektor__lager_sum_og_returnerer_antall_tapte_og_mulige_dagsverk() {
    insertSykefraværsstatistikkLandInDvhTabell(namedParameterJdbcTemplate, 2018, 4, 1, 5, 100);
    insertSykefraværsstatistikkLandInDvhTabell(namedParameterJdbcTemplate, 2018, 4, 3, 10, 100);
    insertSykefraværsstatistikkLandInDvhTabell(namedParameterJdbcTemplate, 2019, 1, 1, 1, 10);

    List<SykefraværsstatistikkSektor> sykefraværsstatistikkSektor =
        repository.hentSykefraværsstatistikkSektor(new ÅrstallOgKvartal(2018, 4));

    assertThat(sykefraværsstatistikkSektor.size()).isEqualTo(1);
    SykefraværsstatistikkSektor sykefraværsstatistikkSektorExpected =
        new SykefraværsstatistikkSektor(2018, 4, "1", 4, new BigDecimal(15), new BigDecimal(200));
    SykefraværsstatistikkSektor sykefraværsstatistikkSektorActual =
        sykefraværsstatistikkSektor.get(0);

    assertTrue(
        new ReflectionEquals(sykefraværsstatistikkSektorExpected)
            .matches(sykefraværsstatistikkSektorActual));
  }

  @Test
  public void
  hentSykefraværsstatistikkLand__lager_sum_og_returnerer_antall_tapte_og_mulige_dagsverk() {
    insertSykefraværsstatistikkLandInDvhTabell(namedParameterJdbcTemplate, 2018, 4, 4, 5, 100);
    insertSykefraværsstatistikkLandInDvhTabell(namedParameterJdbcTemplate, 2018, 4, 6, 10, 100);
    insertSykefraværsstatistikkLandInDvhTabell(namedParameterJdbcTemplate, 2019, 1, 1, 1, 10);

    List<SykefraværsstatistikkLand> sykefraværsstatistikkLand =
        repository.hentSykefraværsstatistikkLand(new ÅrstallOgKvartal(2018, 4));

    assertThat(sykefraværsstatistikkLand.size()).isEqualTo(1);
    assertThat(sykefraværsstatistikkLand.get(0))
        .isEqualTo(
            new SykefraværsstatistikkLand(2018, 4, 10, new BigDecimal(15), new BigDecimal(200)));
  }

  @Test
  public void
  hentSykefraværsstatistikkVirksomhet__lager_sum_og_returnerer_antall_tapte_og_mulige_dagsverk() {
    insertSykefraværsstatistikkVirksomhetInDvhTabell(
        namedParameterJdbcTemplate,
        2018,
        4,
        4,
        ORGNR_VIRKSOMHET_1,
        "10062",
        _1_DAG_TIL_7_DAGER,
        "K",
        5,
        100);
    insertSykefraværsstatistikkVirksomhetInDvhTabell(
        namedParameterJdbcTemplate,
        2018,
        4,
        3,
        ORGNR_VIRKSOMHET_1,
        "10062",
        _1_DAG_TIL_7_DAGER,
        "M",
        8,
        88);
    insertSykefraværsstatistikkVirksomhetInDvhTabell(
        namedParameterJdbcTemplate,
        2018,
        4,
        6,
        ORGNR_VIRKSOMHET_2,
        "10062",
        _1_DAG_TIL_7_DAGER,
        "K",
        3,
        75);
    insertSykefraværsstatistikkVirksomhetInDvhTabell(
        namedParameterJdbcTemplate,
        2019,
        1,
        5,
        ORGNR_VIRKSOMHET_1,
        "10062",
        _1_DAG_TIL_7_DAGER,
        "M",
        5,
        101);
    insertSykefraværsstatistikkVirksomhetInDvhTabell(
        namedParameterJdbcTemplate,
        2019,
        2,
        9,
        ORGNR_VIRKSOMHET_1,
        "10062",
        _8_DAGER_TIL_16_DAGER,
        "M",
        9,
        99);

    List<SykefraværsstatistikkVirksomhet> sykefraværsstatistikkVirksomhet =
        repository.hentSykefraværsstatistikkVirksomhet(new ÅrstallOgKvartal(2018, 4));

    assertThat(sykefraværsstatistikkVirksomhet.size()).isEqualTo(2);
    SykefraværsstatistikkVirksomhet expected =
        new SykefraværsstatistikkVirksomhet(
            2018,
            4,
            ORGNR_VIRKSOMHET_1,
            _1_DAG_TIL_7_DAGER.kode,
            RECTYPE_FOR_VIRKSOMHET,
            7,
            new BigDecimal(13),
            new BigDecimal(188));
    assertThat(sykefraværsstatistikkVirksomhet.get(0)).isEqualTo(expected);
  }

  @Test
  public void
  hentSykefraværsstatistikkNæringMedVarighet__lager_sum_og_returnerer_antall_tapte_og_mulige_dagsverk_med_varighet() {
    insertSykefraværsstatistikkVirksomhetInDvhTabell(
        namedParameterJdbcTemplate,
        2018,
        4,
        4,
        ORGNR_VIRKSOMHET_1,
        "10062",
        _1_DAG_TIL_7_DAGER,
        "K",
        5,
        100);
    insertSykefraværsstatistikkVirksomhetInDvhTabell(
        namedParameterJdbcTemplate,
        2018,
        4,
        4,
        ORGNR_VIRKSOMHET_1,
        "10062",
        _1_DAG_TIL_7_DAGER,
        "K",
        1,
        10,
        RECTYPE_FOR_FORETAK);
    insertSykefraværsstatistikkVirksomhetInDvhTabell(
        namedParameterJdbcTemplate,
        2018,
        4,
        4,
        ORGNR_VIRKSOMHET_1,
        "10062",
        _8_DAGER_TIL_16_DAGER,
        "K",
        5,
        100);
    insertSykefraværsstatistikkVirksomhetInDvhTabell(
        namedParameterJdbcTemplate,
        2018,
        4,
        3,
        ORGNR_VIRKSOMHET_1,
        "10062",
        _1_DAG_TIL_7_DAGER,
        "M",
        8,
        88);
    insertSykefraværsstatistikkVirksomhetInDvhTabell(
        namedParameterJdbcTemplate,
        2018,
        4,
        6,
        ORGNR_VIRKSOMHET_2,
        "10062",
        _1_DAG_TIL_7_DAGER,
        "K",
        3,
        75);
    insertSykefraværsstatistikkVirksomhetInDvhTabell(
        namedParameterJdbcTemplate,
        2018,
        4,
        6,
        ORGNR_VIRKSOMHET_3,
        "85000",
        _1_DAG_TIL_7_DAGER,
        "K",
        10,
        80);

    insertSykefraværsstatistikkVirksomhetInDvhTabell(
        namedParameterJdbcTemplate,
        2019,
        1,
        5,
        ORGNR_VIRKSOMHET_1,
        "10062",
        _1_DAG_TIL_7_DAGER,
        "M",
        5,
        101);
    insertSykefraværsstatistikkVirksomhetInDvhTabell(
        namedParameterJdbcTemplate,
        2019,
        2,
        9,
        ORGNR_VIRKSOMHET_3,
        "85000",
        _8_DAGER_TIL_16_DAGER,
        "M",
        9,
        99);

    List<SykefraværsstatistikkNæringMedVarighet> sykefraværsstatistikkNæringMedVarighet =
        repository.hentSykefraværsstatistikkNæringMedVarighet(new ÅrstallOgKvartal(2018, 4));

    assertThat(sykefraværsstatistikkNæringMedVarighet.size()).isEqualTo(3);
    SykefraværsstatistikkNæringMedVarighet expected =
        new SykefraværsstatistikkNæringMedVarighet(
            2018,
            4,
            NÆRINGSKODE_5SIFFER,
            _1_DAG_TIL_7_DAGER.kode,
            13,
            new BigDecimal(16),
            new BigDecimal(263));
    assertThat(sykefraværsstatistikkNæringMedVarighet.get(0)).isEqualTo(expected);
  }

  @Test
  public void
  hentSykefraværsstatistikkVirksomhetMedGradering__lager_sum_og_returnerer_antall_tapte_dagsverk_i_gradert_sykemelding_og_mulige_dagsverk() {
    insertSykefraværsstatistikkVirksomhetGraderingInDvhTabell(
        namedParameterJdbcTemplate,
        2018,
        4,
        13,
        ORGNR_VIRKSOMHET_1,
        NÆRINGSKODE_2SIFFER,
        NÆRINGSKODE_5SIFFER,
        3,
        1,
        3,
        16,
        100);
    insertSykefraværsstatistikkVirksomhetGraderingInDvhTabell(
        namedParameterJdbcTemplate,
        2018,
        4,
        26,
        ORGNR_VIRKSOMHET_2,
        NÆRINGSKODE_2SIFFER,
        NÆRINGSKODE_5SIFFER,
        6,
        2,
        2,
        32,
        200);
    insertSykefraværsstatistikkVirksomhetGraderingInDvhTabell(
        namedParameterJdbcTemplate,
        2019,
        4,
        13,
        ORGNR_VIRKSOMHET_2,
        NÆRINGSKODE_2SIFFER,
        NÆRINGSKODE_5SIFFER,
        10,
        2,
        4,
        20,
        100);

    List<SykefraværsstatistikkVirksomhetMedGradering> sykefraværsstatistikkVirksomhetMedGradering =
        repository.hentSykefraværsstatistikkVirksomhetMedGradering(new ÅrstallOgKvartal(2018, 4));

    assertThat(sykefraværsstatistikkVirksomhetMedGradering.size()).isEqualTo(2);
    SykefraværsstatistikkVirksomhetMedGradering expected =
        new SykefraværsstatistikkVirksomhetMedGradering(
            2018,
            4,
            ORGNR_VIRKSOMHET_1,
            NÆRINGSKODE_2SIFFER,
            NÆRINGSKODE_5SIFFER,
            RECTYPE_FOR_VIRKSOMHET,
            1,
            new BigDecimal(3),
            3,
            13,
            new BigDecimal(16),
            new BigDecimal(100));
    SykefraværsstatistikkVirksomhetMedGradering expectedLinje2 =
        new SykefraværsstatistikkVirksomhetMedGradering(
            2018,
            4,
            ORGNR_VIRKSOMHET_2,
            NÆRINGSKODE_2SIFFER,
            NÆRINGSKODE_5SIFFER,
            RECTYPE_FOR_VIRKSOMHET,
            2,
            new BigDecimal(6),
            2,
            26,
            new BigDecimal(32),
            new BigDecimal(200));

    assertThat(sykefraværsstatistikkVirksomhetMedGradering.get(0)).isEqualTo(expected);
    assertThat(sykefraværsstatistikkVirksomhetMedGradering.get(1)).isEqualTo(expectedLinje2);
  }

  @Test
  public void
  hentSykefraværsstatistikkLand__returnerer_en_tom_liste_dersom_ingen_data_finnes_i_DVH() {
    insertSykefraværsstatistikkLandInDvhTabell(namedParameterJdbcTemplate, 2019, 1, 1, 1, 10);

    List<SykefraværsstatistikkLand> sykefraværsstatistikkLand =
        repository.hentSykefraværsstatistikkLand(new ÅrstallOgKvartal(2018, 4));

    assertTrue(sykefraværsstatistikkLand.isEmpty());
  }

  @Test
  public void hentAlleSektorer__returnerer_eksisterende_Sektor() {
    insertSektorInDvhTabell(namedParameterJdbcTemplate, "9", "Fylkeskommunal forvaltning");
    insertSektorInDvhTabell(namedParameterJdbcTemplate, "0", "Ukjent");

    List<Sektor> sektorer = repository.hentAlleSektorer();

    assertEquals(2, sektorer.size());
    assertTrue(sektorer.contains(new Sektor("0", "Ukjent")));
    assertTrue(sektorer.contains(new Sektor("9", "Fylkeskommunal forvaltning")));
  }

  @Test
  public void hentAlleNæringer__returnerer_eksisterende_Næring() {
    insertNæringInDvhTabell(
        namedParameterJdbcTemplate, "02", "01", "Skogbruk og tjenester tilknyttet skogbruk");
    insertNæringInDvhTabell(namedParameterJdbcTemplate, "11", "10", "Produksjon av drikkevarer");

    List<Næring> næringer = repository.hentAlleNæringer();

    assertTrue(næringer.contains(new Næring("02", "Skogbruk og tjenester tilknyttet skogbruk")));
    assertTrue(næringer.contains(new Næring("11", "Produksjon av drikkevarer")));
  }

  @Test
  public void hentVirksomhetMetadataEksportering__returnerer_virksomhetMetadataEksportering() {
    insertOrgenhetInDvhTabell(
        namedParameterJdbcTemplate,
        ORGNR_VIRKSOMHET_1,
        SEKTOR,
        NÆRINGSKODE_2SIFFER,
        "10.111",
        2020,
        3);

    List<Orgenhet> orgenhetList = repository.hentOrgenheter(new ÅrstallOgKvartal(2020, 3));

    assertTrue(
        orgenhetList.contains(
            new Orgenhet(
                new Orgnr(ORGNR_VIRKSOMHET_1),
                "",
                RECTYPE_FOR_VIRKSOMHET,
                SEKTOR,
                NÆRINGSKODE_2SIFFER,
                "10111",
                new ÅrstallOgKvartal(2020, 3))));
  }
}
