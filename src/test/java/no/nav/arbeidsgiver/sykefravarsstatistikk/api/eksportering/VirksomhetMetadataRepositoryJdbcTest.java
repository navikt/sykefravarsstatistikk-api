package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.AppConfigForJdbcTesterConfig;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.NæringOgNæringskode5siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.VirksomhetMetadata;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.VirksomhetMetadataNæringskode5siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.VirksomhetMetadataRepository;
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

import java.util.Arrays;
import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.NÆRINGSKODE_2SIFFER;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.NÆRINGSKODE_5SIFFER;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.ORGNR_VIRKSOMHET_1;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.ORGNR_VIRKSOMHET_2;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.ORGNR_VIRKSOMHET_3;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.SEKTOR;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfigForJdbcTesterConfig.class})
@DataJdbcTest(excludeAutoConfiguration = {TestDatabaseAutoConfiguration.class})
class VirksomhetMetadataRepositoryJdbcTest {

  @Autowired private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  private VirksomhetMetadataRepository repository;

  @BeforeEach
  public void setUp() {
    repository = new VirksomhetMetadataRepository(namedParameterJdbcTemplate);
    cleanUpTestDb(namedParameterJdbcTemplate);
  }

  @AfterEach
  public void tearDown() {
    cleanUpTestDb(namedParameterJdbcTemplate);
  }

  @Test
  public void slettNæringOgNæringskode5siffer__sletter_VirksomhetMetaData() {
    opprettTestVirksomhetMetaData(2020, 3);

    int antallSlettet = repository.slettNæringOgNæringskode5siffer();

    assertThat(antallSlettet).isEqualTo(2);
  }

  @Test
  public void slettVirksomhetMetadata__sletter_VirksomhetMetaData() {
    opprettTestVirksomhetMetaData(2020, 3);

    int antallSlettet = repository.slettVirksomhetMetadata();

    assertThat(antallSlettet).isEqualTo(3);
  }

  @Test
  public void
      opprettVirksomhetMetadataNæringskode5siffer__oppretter_riktig_metadataNæringskode5siffer() {
    VirksomhetMetadataNæringskode5siffer virksomhetMetadataNæringskode5siffer1 =
        new VirksomhetMetadataNæringskode5siffer(
            new Orgnr(ORGNR_VIRKSOMHET_1),
            new ÅrstallOgKvartal(2020, 3),
            new NæringOgNæringskode5siffer(NÆRINGSKODE_2SIFFER, "10001"));
    VirksomhetMetadataNæringskode5siffer virksomhetMetadataNæringskode5siffer2 =
        new VirksomhetMetadataNæringskode5siffer(
            new Orgnr(ORGNR_VIRKSOMHET_1),
            new ÅrstallOgKvartal(2020, 3),
            new NæringOgNæringskode5siffer(NÆRINGSKODE_2SIFFER, "10002"));

    repository.opprettVirksomhetMetadataNæringskode5siffer(
        Arrays.asList(
            virksomhetMetadataNæringskode5siffer1, virksomhetMetadataNæringskode5siffer2));

    List<VirksomhetMetadataNæringskode5siffer> results =
        hentAlleVirksomhetMetadataNæringskode5siffer(namedParameterJdbcTemplate);
    assertThat(results.get(0)).isEqualTo(virksomhetMetadataNæringskode5siffer1);
    assertThat(results.get(1)).isEqualTo(virksomhetMetadataNæringskode5siffer2);
  }

  @Test
  public void opprettVirksomhetMetadata__oppretter_riktig_metadata() {
    VirksomhetMetadata virksomhetMetadataVirksomhet1 =
        new VirksomhetMetadata(
            new Orgnr(ORGNR_VIRKSOMHET_1),
            "Virksomhet 1",
            RECTYPE_FOR_VIRKSOMHET,
            SEKTOR,
            NÆRINGSKODE_2SIFFER,
            NÆRINGSKODE_5SIFFER,
            new ÅrstallOgKvartal(2020, 3));
    VirksomhetMetadata virksomhetMetadataVirksomhet2 =
        new VirksomhetMetadata(
            new Orgnr(ORGNR_VIRKSOMHET_2),
            "Virksomhet 2",
            RECTYPE_FOR_VIRKSOMHET,
            SEKTOR,
            NÆRINGSKODE_2SIFFER,
            NÆRINGSKODE_5SIFFER,
            new ÅrstallOgKvartal(2020, 3));

    repository.opprettVirksomhetMetadata(
        Arrays.asList(virksomhetMetadataVirksomhet1, virksomhetMetadataVirksomhet2));

    List<VirksomhetMetadata> results = hentAlleVirksomhetMetadata(namedParameterJdbcTemplate);
    assertThat(results.get(0)).isEqualTo(virksomhetMetadataVirksomhet1);
    assertThat(results.get(1)).isEqualTo(virksomhetMetadataVirksomhet2);
  }

  @Test
  public void hentVirksomhetMetadata_returnerer_riktige_metdata_for_en_gitt_årstall_og_kvartal() {
    opprettTestVirksomhetMetaData(2020, 2);

    List<VirksomhetMetadata> results =
        repository.hentVirksomhetMetadataMedNæringskoder(new ÅrstallOgKvartal(2019, 2));

    assertThat(results.size()).isEqualTo(0);
  }

  @Test
  public void hentVirksomhetMetadata_returnerer_riktige_metdata() {
    opprettTestVirksomhetMetaData(2020, 3);

    List<VirksomhetMetadata> results =
        repository.hentVirksomhetMetadataMedNæringskoder(new ÅrstallOgKvartal(2020, 3));

    assertThat(results.size()).isEqualTo(3);
    VirksomhetMetadata virksomhetMetadataVirksomhet1 =
        results.stream().filter(r -> ORGNR_VIRKSOMHET_1.equals(r.getOrgnr())).findFirst().get();
    List<NæringOgNæringskode5siffer> næringOgNæringskode5siffer =
        virksomhetMetadataVirksomhet1.getNæringOgNæringskode5siffer();
    assertThat(næringOgNæringskode5siffer.contains(new NæringOgNæringskode5siffer("71", "71001")))
        .isTrue();
    assertThat(næringOgNæringskode5siffer.contains(new NæringOgNæringskode5siffer("71", "71002")))
        .isTrue();
  }

  private List<VirksomhetMetadata> hentAlleVirksomhetMetadata(
      NamedParameterJdbcTemplate jdbcTemplate) {
    return jdbcTemplate.query(
        "select * from virksomhet_metadata",
        new MapSqlParameterSource(),
        (rs, rowNum) ->
            new VirksomhetMetadata(
                new Orgnr(rs.getString("orgnr")),
                rs.getString("navn"),
                rs.getString("rectype"),
                rs.getString("sektor"),
                rs.getString("primarnaring"),
                rs.getString("primarnaringskode"),
                new ÅrstallOgKvartal(rs.getInt("arstall"), rs.getInt("kvartal"))));
  }

  private List<VirksomhetMetadataNæringskode5siffer> hentAlleVirksomhetMetadataNæringskode5siffer(
      NamedParameterJdbcTemplate jdbcTemplate) {
    return jdbcTemplate.query(
        "select * from virksomhet_metadata_naring_kode_5siffer",
        new MapSqlParameterSource(),
        (rs, rowNum) ->
            new VirksomhetMetadataNæringskode5siffer(
                new Orgnr(rs.getString("orgnr")),
                new ÅrstallOgKvartal(rs.getInt("arstall"), rs.getInt("kvartal")),
                new NæringOgNæringskode5siffer(
                    rs.getString("naring_kode"), rs.getString("naring_kode_5siffer"))));
  }

  private int opprettTestVirksomhetMetaData(int årstall, int kvartal) {
    namedParameterJdbcTemplate.update(
        "insert into virksomhet_metadata (orgnr, navn, rectype, sektor, primarnaring, primarnaringskode, arstall, kvartal) "
            + "VALUES (:orgnr, :navn, :rectype, :sektor, :primarnaring, :primarnaringskode, :årstall, :kvartal)",
        parametreViksomhetMetadata(
            ORGNR_VIRKSOMHET_1, "Virksomhet 1", "2", "3", "71", "71000", årstall, kvartal));

    namedParameterJdbcTemplate.update(
        "insert into virksomhet_metadata_naring_kode_5siffer (orgnr, naring_kode, naring_kode_5siffer, arstall, kvartal) "
            + "VALUES (:orgnr, :naring_kode, :naring_kode_5siffer, :årstall, :kvartal)",
        parametreViksomhetMetadataNæring5Siffer(
            ORGNR_VIRKSOMHET_1, "71", "71001", årstall, kvartal));

    namedParameterJdbcTemplate.update(
        "insert into virksomhet_metadata_naring_kode_5siffer (orgnr, naring_kode, naring_kode_5siffer, arstall, kvartal) "
            + "VALUES (:orgnr, :naring_kode, :naring_kode_5siffer, :årstall, :kvartal)",
        parametreViksomhetMetadataNæring5Siffer(
            ORGNR_VIRKSOMHET_1, "71", "71002", årstall, kvartal));

    namedParameterJdbcTemplate.update(
        "insert into virksomhet_metadata (orgnr, navn, rectype, sektor, primarnaring, primarnaringskode, arstall, kvartal) "
            + "VALUES (:orgnr, :navn, :rectype, :sektor, :primarnaring, :primarnaringskode, :årstall, :kvartal)",
        parametreViksomhetMetadata(
            ORGNR_VIRKSOMHET_2, "Virksomhet 2", "2", "3", "10", "10000", årstall, kvartal));
    namedParameterJdbcTemplate.update(
        "insert into virksomhet_metadata (orgnr, navn, rectype, sektor, primarnaring, primarnaringskode, arstall, kvartal) "
            + "VALUES (:orgnr, :navn, :rectype, :sektor, :primarnaring, :primarnaringskode, :årstall, :kvartal)",
        parametreViksomhetMetadata(
            ORGNR_VIRKSOMHET_3, "Virksomhet 3", "2", "3", "10", "10000", årstall, kvartal));
    return 0;
  }

  private static void cleanUpTestDb(NamedParameterJdbcTemplate jdbcTemplate) {
    jdbcTemplate.update("delete from virksomhet_metadata", new MapSqlParameterSource());
  }

  private MapSqlParameterSource parametreViksomhetMetadata(
      String orgnr,
      String navn,
      String rectype,
      String sektor,
      String primarnaring,
      String primarnaringskode,
      int årstall,
      int kvartal) {
    return new MapSqlParameterSource()
        .addValue("orgnr", orgnr)
        .addValue("navn", navn)
        .addValue("rectype", rectype)
        .addValue("sektor", sektor)
        .addValue("primarnaring", primarnaring)
        .addValue("primarnaringskode", primarnaringskode)
        .addValue("årstall", årstall)
        .addValue("kvartal", kvartal);
  }

  private MapSqlParameterSource parametreViksomhetMetadataNæring5Siffer(
      String orgnr,
      String næringskode2Siffer,
      String næringskode5Siffer,
      int årstall,
      int kvartal) {
    return new MapSqlParameterSource()
        .addValue("orgnr", orgnr)
        .addValue("naring_kode", næringskode2Siffer)
        .addValue("naring_kode_5siffer", næringskode5Siffer)
        .addValue("årstall", årstall)
        .addValue("kvartal", kvartal);
  }
}
