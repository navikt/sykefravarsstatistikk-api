package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.AppConfigForJdbcTesterConfig;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.InstitusjonellSektorkode;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.OverordnetEnhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.ArbeidsmiljøportalenBransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config.LocalOgUnitTestOidcConfiguration;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.GraderingTestUtils.insertDataMedGradering;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAllStatistikkFraDatabase;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.DatavarehusRepository.RECTYPE_FOR_FORETAK;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfigForJdbcTesterConfig.class})
@DataJdbcTest(
    excludeAutoConfiguration = {
      TestDatabaseAutoConfiguration.class,
      LocalOgUnitTestOidcConfiguration.class
    })
public class GraderingRepositoryJdbcTest {

  private static final Næring PRODUKSJON_AV_KLÆR = new Næring("14", "Produksjon av klær");
  private static final Næring PRODUKSJON_AV_LÆR_OG_LÆRVARER =
      new Næring("15", "Produksjon av lær og lærvarer");
  private static final Næring HELSETJENESTER = new Næring("86", "Helsetjenester");
  private static OverordnetEnhet OVERORDNETENHET_1_NÆRING_86 =
      OverordnetEnhet.builder()
          .navn("Hospital")
          .orgnr(new Orgnr("999999777"))
          .næringskode(new Næringskode5Siffer("86101", "Alminnelige somatiske sykehus"))
          .institusjonellSektorkode(new InstitusjonellSektorkode("7000", "Ideelle organisasjoner"))
          .build();

  private static Underenhet UNDERENHET_1_NÆRING_14 =
      Underenhet.builder()
          .orgnr(new Orgnr("999999999"))
          .næringskode(new Næringskode5Siffer("14120", "Produksjon av arbeidstøy"))
          .build();
  private static Underenhet UNDERENHET_2_NÆRING_15 =
      Underenhet.builder()
          .orgnr(new Orgnr("888888888"))
          .næringskode(new Næringskode5Siffer("15100", "andre_næringskode"))
          .build();
  private static Underenhet UNDERENHET_3_NÆRING_14 =
      Underenhet.builder()
          .orgnr(new Orgnr("777777777"))
          .næringskode(new Næringskode5Siffer("14120", "Produksjon av arbeidstøy"))
          .build();
  private static ÅrstallOgKvartal _2020_1 = new ÅrstallOgKvartal(2020, 1);
  private static ÅrstallOgKvartal _2019_4 = new ÅrstallOgKvartal(2019, 4);

  @Autowired private NamedParameterJdbcTemplate jdbcTemplate;

  private GraderingRepository graderingRepository;

  @BeforeEach
  public void setUp() {
    graderingRepository = new GraderingRepository(jdbcTemplate);
    slettAllStatistikkFraDatabase(jdbcTemplate);
  }

  @AfterEach
  public void tearDown() {
    slettAllStatistikkFraDatabase(jdbcTemplate);
  }

  @Test
  public void hentSykefraværForEttKvartalMedGradering__skal_returnere_riktig_sykefravær() {
    insertDataMedGradering(
        jdbcTemplate,
        UNDERENHET_1_NÆRING_14.getOrgnr().getVerdi(),
        PRODUKSJON_AV_KLÆR.getKode(),
        "14100",
        RECTYPE_FOR_VIRKSOMHET,
        _2019_4,
        7,
        new BigDecimal(10),
        new BigDecimal(20),
        new BigDecimal(100));
    insertDataMedGradering(
        jdbcTemplate,
        UNDERENHET_1_NÆRING_14.getOrgnr().getVerdi(),
        PRODUKSJON_AV_KLÆR.getKode(),
        "14222",
        RECTYPE_FOR_VIRKSOMHET,
        _2019_4,
        7,
        new BigDecimal(12),
        new BigDecimal(20),
        new BigDecimal(100));
    insertDataMedGradering(
        jdbcTemplate,
        UNDERENHET_1_NÆRING_14.getOrgnr().getVerdi(),
        PRODUKSJON_AV_KLÆR.getKode(),
        "14222",
        RECTYPE_FOR_VIRKSOMHET,
        _2020_1,
        15,
        new BigDecimal(25),
        new BigDecimal(50),
        new BigDecimal(300));

    List<UmaskertSykefraværForEttKvartal> resultat =
        graderingRepository.hentSykefraværMedGradering(UNDERENHET_1_NÆRING_14);

    assertThat(resultat.size()).isEqualTo(2);
    assertThat(resultat.get(0))
        .isEqualTo(
            new UmaskertSykefraværForEttKvartal(
                new ÅrstallOgKvartal(2019, 4), new BigDecimal(22), new BigDecimal(40), 14));
    assertThat(resultat.get(1))
        .isEqualTo(
            new UmaskertSykefraværForEttKvartal(
                new ÅrstallOgKvartal(2020, 1), new BigDecimal(25), new BigDecimal(50), 15));
  }

  @Test
  public void
      hentSykefraværForEttKvartalMedGradering__skal_returnere_riktig_underenhet_sykefravær() {
    insertDataMedGradering(
        jdbcTemplate,
        UNDERENHET_1_NÆRING_14.getOrgnr().getVerdi(),
        PRODUKSJON_AV_KLÆR.getKode(),
        UNDERENHET_1_NÆRING_14.getNæringskode().getKode(),
        RECTYPE_FOR_VIRKSOMHET,
        _2019_4,
        7,
        new BigDecimal(10),
        new BigDecimal(20),
        new BigDecimal(100));
    insertDataMedGradering(
        jdbcTemplate,
        UNDERENHET_1_NÆRING_14.getOrgnr().getVerdi(),
        PRODUKSJON_AV_KLÆR.getKode(),
        "14222",
        RECTYPE_FOR_VIRKSOMHET,
        _2020_1,
        7,
        new BigDecimal(12),
        new BigDecimal(20),
        new BigDecimal(100));
    insertDataMedGradering(
        jdbcTemplate,
        UNDERENHET_3_NÆRING_14.getOrgnr().getVerdi(),
        PRODUKSJON_AV_KLÆR.getKode(),
        "14222",
        RECTYPE_FOR_VIRKSOMHET,
        _2020_1,
        15,
        new BigDecimal(25),
        new BigDecimal(50),
        new BigDecimal(300));

    List<UmaskertSykefraværForEttKvartal> resultat =
        graderingRepository.hentSykefraværMedGradering(UNDERENHET_1_NÆRING_14);

    assertThat(resultat.size()).isEqualTo(2);
    assertThat(resultat.get(0))
        .isEqualTo(
            new UmaskertSykefraværForEttKvartal(
                new ÅrstallOgKvartal(2019, 4), new BigDecimal(10), new BigDecimal(20), 7));
    assertThat(resultat.get(1))
        .isEqualTo(
            new UmaskertSykefraværForEttKvartal(
                new ÅrstallOgKvartal(2020, 1), new BigDecimal(12), new BigDecimal(20), 7));
  }

  @Test
  public void
      hentSykefraværForEttKvartalMedGradering__skal_returnere_riktig_sykefravær_for_næring() {

    insertDataMedGradering(
        jdbcTemplate,
        UNDERENHET_1_NÆRING_14.getOrgnr().getVerdi(),
        PRODUKSJON_AV_KLÆR.getKode(),
        UNDERENHET_1_NÆRING_14.getNæringskode().getKode(),
        RECTYPE_FOR_VIRKSOMHET,
        _2019_4,
        7,
        new BigDecimal(10),
        new BigDecimal(20),
        new BigDecimal(100));
    insertDataMedGradering(
        jdbcTemplate,
        UNDERENHET_3_NÆRING_14.getOrgnr().getVerdi(),
        PRODUKSJON_AV_KLÆR.getKode(),
        "14222",
        RECTYPE_FOR_VIRKSOMHET,
        _2020_1,
        7,
        new BigDecimal(12),
        new BigDecimal(20),
        new BigDecimal(100));
    insertDataMedGradering(
        jdbcTemplate,
        UNDERENHET_2_NÆRING_15.getOrgnr().getVerdi(),
        PRODUKSJON_AV_LÆR_OG_LÆRVARER.getKode(),
        "15333",
        RECTYPE_FOR_VIRKSOMHET,
        _2020_1,
        15,
        new BigDecimal(25),
        new BigDecimal(50),
        new BigDecimal(300));

    List<UmaskertSykefraværForEttKvartal> resultat =
        graderingRepository.hentSykefraværMedGradering(PRODUKSJON_AV_KLÆR);

    assertThat(resultat.size()).isEqualTo(2);
    assertThat(resultat.get(0))
        .isEqualTo(
            new UmaskertSykefraværForEttKvartal(
                new ÅrstallOgKvartal(2019, 4), new BigDecimal(10), new BigDecimal(20), 7));
    assertThat(resultat.get(1))
        .isEqualTo(
            new UmaskertSykefraværForEttKvartal(
                new ÅrstallOgKvartal(2020, 1), new BigDecimal(12), new BigDecimal(20), 7));
  }

  @Test
  public void
      hentSykefraværForEttKvartalMedGradering__skal_returnere_riktig_sykefravær_for_bransje() {
    Næringskode5Siffer sykehus = new Næringskode5Siffer("86101", "Alminnelige somatiske sykehus");
    Næringskode5Siffer legetjeneste = new Næringskode5Siffer("86211", "Allmenn legetjeneste");
    Næringskode5Siffer næringskodeIkkeErFraBransje =
        new Næringskode5Siffer("86902", "Fysioterapitjeneste");

    insertDataMedGradering(
        jdbcTemplate,
        UNDERENHET_1_NÆRING_14.getOrgnr().getVerdi(),
        HELSETJENESTER.getKode(),
        sykehus.getKode(),
        RECTYPE_FOR_VIRKSOMHET,
        _2019_4,
        7,
        new BigDecimal(10),
        new BigDecimal(20),
        new BigDecimal(100));
    insertDataMedGradering(
        jdbcTemplate,
        UNDERENHET_3_NÆRING_14.getOrgnr().getVerdi(),
        HELSETJENESTER.getKode(),
        sykehus.getKode(),
        RECTYPE_FOR_VIRKSOMHET,
        _2020_1,
        7,
        new BigDecimal(12),
        new BigDecimal(20),
        new BigDecimal(100));
    insertDataMedGradering(
        jdbcTemplate,
        UNDERENHET_2_NÆRING_15.getOrgnr().getVerdi(),
        HELSETJENESTER.getKode(),
        legetjeneste.getKode(),
        RECTYPE_FOR_VIRKSOMHET,
        _2020_1,
        15,
        new BigDecimal(25),
        new BigDecimal(50),
        new BigDecimal(300));
    insertDataMedGradering(
        jdbcTemplate,
        UNDERENHET_2_NÆRING_15.getOrgnr().getVerdi(),
        HELSETJENESTER.getKode(),
        næringskodeIkkeErFraBransje.getKode(),
        RECTYPE_FOR_VIRKSOMHET,
        _2020_1,
        4,
        new BigDecimal(55),
        new BigDecimal(66),
        new BigDecimal(3000));

    List<UmaskertSykefraværForEttKvartal> resultat =
        graderingRepository.hentSykefraværMedGradering(
            new Bransje(ArbeidsmiljøportalenBransje.SYKEHUS, "sykehus", "86101", "86211"));

    assertThat(resultat.size()).isEqualTo(2);
    assertThat(resultat.get(0))
        .isEqualTo(
            new UmaskertSykefraværForEttKvartal(
                new ÅrstallOgKvartal(2019, 4), new BigDecimal(10), new BigDecimal(20), 7));
    assertThat(resultat.get(1))
        .isEqualTo(
            new UmaskertSykefraværForEttKvartal(
                new ÅrstallOgKvartal(2020, 1), new BigDecimal(37), new BigDecimal(70), 22));
  }

  @Test
  public void
      hentSykefraværForEttKvartalMedGradering__henterIkkeUtGradertSykefraværForOverordnetEnhet() {
    Næringskode5Siffer sykehus = new Næringskode5Siffer("86101", "Alminnelige somatiske sykehus");

    insertDataMedGradering(
        jdbcTemplate,
        OVERORDNETENHET_1_NÆRING_86.getOrgnr().getVerdi(),
        HELSETJENESTER.getKode(),
        sykehus.getKode(),
        RECTYPE_FOR_FORETAK,
        _2020_1,
        7,
        new BigDecimal(10),
        new BigDecimal(20),
        new BigDecimal(100));
    insertDataMedGradering(
        jdbcTemplate,
        UNDERENHET_3_NÆRING_14.getOrgnr().getVerdi(),
        HELSETJENESTER.getKode(),
        sykehus.getKode(),
        RECTYPE_FOR_VIRKSOMHET,
        _2020_1,
        7,
        new BigDecimal(12),
        new BigDecimal(20),
        new BigDecimal(100));

    List<UmaskertSykefraværForEttKvartal> resultat =
        graderingRepository.hentSykefraværMedGradering(
            new Bransje(ArbeidsmiljøportalenBransje.SYKEHUS, "sykehus", "86101", "86211"));

    assertThat(resultat.size()).isEqualTo(1);
    assertThat(resultat.get(0))
        .isEqualTo(
            new UmaskertSykefraværForEttKvartal(
                new ÅrstallOgKvartal(2020, 1), new BigDecimal(12), new BigDecimal(20), 7));
  }
}
