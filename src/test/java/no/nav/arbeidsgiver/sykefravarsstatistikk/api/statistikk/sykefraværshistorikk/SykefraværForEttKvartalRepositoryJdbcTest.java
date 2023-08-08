package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.SISTE_PUBLISERTE_KVARTAL;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.opprettStatistikkForLand;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAllStatistikkFraDatabase;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori._1_DAG_TIL_7_DAGER;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori._8_UKER_TIL_20_UKER;
import static org.assertj.core.api.Java6Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.AppConfigForJdbcTesterConfig;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.Sektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.UnderenhetLegacy;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.bransjeprogram.ArbeidsmiljøportalenBransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis.KvartalsvisSykefraværRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfigForJdbcTesterConfig.class})
@DataJdbcTest(excludeAutoConfiguration = {TestDatabaseAutoConfiguration.class})
public class SykefraværForEttKvartalRepositoryJdbcTest {
  @Autowired private NamedParameterJdbcTemplate jdbcTemplate;

  private KvartalsvisSykefraværRepository kvartalsvisSykefraværprosentRepository;

  @BeforeEach
  public void setUp() {
    kvartalsvisSykefraværprosentRepository = new KvartalsvisSykefraværRepository(jdbcTemplate);
    slettAllStatistikkFraDatabase(jdbcTemplate);
  }

  @AfterEach
  public void tearDown() {
    slettAllStatistikkFraDatabase(jdbcTemplate);
  }

  @Test
  public void hentSykefraværprosentLand__skal_returnere_riktig_sykefravær() {
    opprettStatistikkForLand(jdbcTemplate);
    List<SykefraværForEttKvartal> resultat =
        kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentLand();
    assertThat(resultat.size()).isEqualTo(3);
    assertThat(resultat.get(0))
        .isEqualTo(
            new SykefraværForEttKvartal(
                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2),
                new BigDecimal(6),
                new BigDecimal(100),
                10));
  }

  @Test
  public void hentSykefraværprosentSektor__skal_returnere_riktig_sykefravær() {
    Sektor statligForvaltning = new Sektor("1", "Statlig forvaltning");
    jdbcTemplate.update(
        "insert into sykefravar_statistikk_sektor (sektor_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
            + "VALUES (:sektor_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
        parametre(statligForvaltning, 2019, 2, 10, 2, 100));
    jdbcTemplate.update(
        "insert into sykefravar_statistikk_sektor (sektor_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
            + "VALUES (:sektor_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
        parametre(statligForvaltning, 2019, 1, 10, 3, 100));
    jdbcTemplate.update(
        "insert into sykefravar_statistikk_sektor (sektor_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
            + "VALUES (:sektor_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
        parametre(statligForvaltning, 2018, 4, 10, 4, 100));
    jdbcTemplate.update(
        "insert into sykefravar_statistikk_sektor (sektor_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
            + "VALUES (:sektor_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
        parametre(new Sektor("2", "Kommunal forvaltning"), 2018, 4, 10, 5, 100));

    List<SykefraværForEttKvartal> resultat =
        kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentSektor(
            statligForvaltning);
    assertThat(resultat.size()).isEqualTo(3);
    assertThat(resultat.get(0))
        .isEqualTo(
            new SykefraværForEttKvartal(
                new ÅrstallOgKvartal(2018, 4), new BigDecimal(4), new BigDecimal(100), 10));
  }

  @Test
  public void hentSykefraværprosentNæring__skal_returnere_riktig_sykefravær() {
    Næring produksjonAvKlær = new Næring("14", "Produksjon av klær");
    jdbcTemplate.update(
        "insert into sykefravar_statistikk_naring (naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
            + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
        parametre(produksjonAvKlær, 2019, 2, 10, 2, 100));
    jdbcTemplate.update(
        "insert into sykefravar_statistikk_naring (naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
            + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
        parametre(produksjonAvKlær, 2019, 1, 10, 3, 100));
    jdbcTemplate.update(
        "insert into sykefravar_statistikk_naring (naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
            + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
        parametre(new Næring("85", "Undervisning"), 2018, 4, 10, 5, 100));

    List<SykefraværForEttKvartal> resultat =
        kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentNæring(
            produksjonAvKlær);
    assertThat(resultat.size()).isEqualTo(2);
    assertThat(resultat.get(0))
        .isEqualTo(
            new SykefraværForEttKvartal(
                new ÅrstallOgKvartal(2019, 1), new BigDecimal(3), new BigDecimal(100), 10));
  }

  @Test
  public void hentSykefraværprosentBransje__skal_returnere_riktig_sykefravær() {
    Næring produksjonAvKlær = new Næring("14", "Produksjon av klær");
    jdbcTemplate.update(
        "insert into sykefravar_statistikk_naring5siffer "
            + "(naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
            + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
        parametre(new Næring("87101", "Somatiske spesialsykehjem"), 2019, 2, 10, 5, 100));

    jdbcTemplate.update(
        "insert into sykefravar_statistikk_naring5siffer "
            + "(naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
            + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
        parametre(new Næring("87101", "Somatiske spesialsykehjem"), 2019, 1, 10, 1, 100));

    jdbcTemplate.update(
        "insert into sykefravar_statistikk_naring5siffer "
            + "(naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
            + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
        parametre(new Næring("87102", "Somatiske sykehjem"), 2019, 1, 10, 7, 100));

    jdbcTemplate.update(
        "insert into sykefravar_statistikk_naring5siffer "
            + "(naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
            + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
        parametre(new Næring("87301", "Aldershjem"), 2018, 4, 10, 6, 100));

    Bransje sykehjem =
        new Bransje(ArbeidsmiljøportalenBransje.SYKEHUS, "Sykehjem", "87101", "87102");
    List<SykefraværForEttKvartal> resultat =
        kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentBransje(sykehjem);
    assertThat(resultat.size()).isEqualTo(2);
    assertThat(resultat.get(0))
        .isEqualTo(
            new SykefraværForEttKvartal(
                new ÅrstallOgKvartal(2019, 1), new BigDecimal(8), new BigDecimal(200), 20));
  }

  @Test
  public void hentSykefraværprosentVirksomhet__skal_returnere_riktig_sykefravær() {
    UnderenhetLegacy barnehage =
        new UnderenhetLegacy(
            new Orgnr("999999999"),
            new Orgnr("1111111111"),
            "test Barnehage",
            new Næringskode5Siffer("88911", "Barnehage"),
            10);
    jdbcTemplate.update(
        "insert into sykefravar_statistikk_virksomhet (orgnr, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
            + "VALUES (:orgnr, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
        parametre(barnehage.getOrgnr(), 2019, 2, 10, 2, 100, _1_DAG_TIL_7_DAGER));
    jdbcTemplate.update(
        "insert into sykefravar_statistikk_virksomhet (orgnr, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
            + "VALUES (:orgnr, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
        parametre(new Orgnr("987654321"), 2019, 1, 10, 3, 100, _1_DAG_TIL_7_DAGER));
    jdbcTemplate.update(
        "insert into sykefravar_statistikk_virksomhet (orgnr, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
            + "VALUES (:orgnr, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
        parametre(barnehage.getOrgnr(), 2018, 4, 10, 5, 100, _1_DAG_TIL_7_DAGER));

    List<SykefraværForEttKvartal> resultat =
        kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentVirksomhet(
            barnehage);
    assertThat(resultat.size()).isEqualTo(2);
    assertThat(resultat.get(0))
        .isEqualTo(
            new SykefraværForEttKvartal(
                new ÅrstallOgKvartal(2018, 4), new BigDecimal(5), new BigDecimal(100), 10));
  }

  @Test
  public void hentSykefraværprosentVirksomhet__skal_summere_sykefravær_på_varighet() {
    UnderenhetLegacy barnehage =
        new UnderenhetLegacy(
            new Orgnr("999999999"),
            new Orgnr("1111111111"),
            "test Barnehage",
            new Næringskode5Siffer("88911", "Barnehage"),
            10);
    jdbcTemplate.update(
        "insert into sykefravar_statistikk_virksomhet (orgnr, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk, varighet) "
            + "VALUES (:orgnr, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk, :varighet)",
        parametre(barnehage.getOrgnr(), 2019, 2, 10, 2, 100, _1_DAG_TIL_7_DAGER));
    jdbcTemplate.update(
        "insert into sykefravar_statistikk_virksomhet (orgnr, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk, varighet) "
            + "VALUES (:orgnr, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk, :varighet)",
        parametre(barnehage.getOrgnr(), 2019, 2, 10, 5, 100, _8_UKER_TIL_20_UKER));
    List<SykefraværForEttKvartal> resultat =
        kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentVirksomhet(
            barnehage);
    assertThat(resultat)
        .isEqualTo(
            Arrays.asList(
                new SykefraværForEttKvartal(
                    new ÅrstallOgKvartal(2019, 2), new BigDecimal(7), new BigDecimal(200), 20)));
  }

  @Test
  public void hentSykefraværprosentLand__maskerer_sf_dersom_antall_ansatte_er_for_lav() {
    jdbcTemplate.update(
        "insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
            + "VALUES (:arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
        parametre(2019, 2, 4, 4, 100));
    jdbcTemplate.update(
        "insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
            + "VALUES (:arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
        parametre(2019, 1, 10, 5, 100));

    List<SykefraværForEttKvartal> resultat =
        kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentLand();

    assertThat(resultat.size()).isEqualTo(2);

    SykefraværForEttKvartal ikkeMaskertSykefraværForEttKvartal = resultat.get(0);
    assertThat(ikkeMaskertSykefraværForEttKvartal.isErMaskert()).isFalse();
    assertThat(ikkeMaskertSykefraværForEttKvartal.getProsent().setScale(2))
        .isEqualTo(new BigDecimal(5).setScale(2));

    SykefraværForEttKvartal maskertSykefraværForEttKvartal = resultat.get(1);
    assertThat(maskertSykefraværForEttKvartal.isErMaskert()).isTrue();
    assertThat(maskertSykefraværForEttKvartal.getProsent()).isNull();
  }

  private MapSqlParameterSource parametre(
      int årstall, int kvartal, int antallPersoner, int tapteDagsverk, int muligeDagsverk) {
    return new MapSqlParameterSource()
        .addValue("arstall", årstall)
        .addValue("kvartal", kvartal)
        .addValue("antall_personer", antallPersoner)
        .addValue("tapte_dagsverk", tapteDagsverk)
        .addValue("mulige_dagsverk", muligeDagsverk);
  }

  private MapSqlParameterSource parametre(
      Sektor sektor,
      int årstall,
      int kvartal,
      int antallPersoner,
      int tapteDagsverk,
      int muligeDagsverk) {
    return parametre(årstall, kvartal, antallPersoner, tapteDagsverk, muligeDagsverk)
        .addValue("sektor_kode", sektor.getKode());
  }

  private MapSqlParameterSource parametre(
      Næring næring,
      int årstall,
      int kvartal,
      int antallPersoner,
      int tapteDagsverk,
      int muligeDagsverk) {
    return parametre(årstall, kvartal, antallPersoner, tapteDagsverk, muligeDagsverk)
        .addValue("naring_kode", næring.getKode());
  }

  private MapSqlParameterSource parametre(
      Orgnr orgnr,
      int årstall,
      int kvartal,
      int antallPersoner,
      int tapteDagsverk,
      int muligeDagsverk,
      Varighetskategori varighet) {
    return parametre(årstall, kvartal, antallPersoner, tapteDagsverk, muligeDagsverk)
        .addValue("orgnr", orgnr.getVerdi())
        .addValue("varighet", varighet.kode);
  }
}
