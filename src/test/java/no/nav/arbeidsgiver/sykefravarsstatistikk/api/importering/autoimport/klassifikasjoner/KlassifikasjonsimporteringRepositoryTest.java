package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.klassifikasjoner;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.AppConfigForJdbcTesterConfig;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Klassifikasjonskilde;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Sektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Virksomhetsklassifikasjon;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.KlassifikasjonsimporteringRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfigForJdbcTesterConfig.class})
@DataJdbcTest(excludeAutoConfiguration = {TestDatabaseAutoConfiguration.class})
public class KlassifikasjonsimporteringRepositoryTest {
  @Autowired private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  private KlassifikasjonsimporteringRepository repository;

  @BeforeEach
  public void setUp() {
    repository = new KlassifikasjonsimporteringRepository(namedParameterJdbcTemplate);
    cleanUpLokalTestDb(namedParameterJdbcTemplate);
  }

  @AfterEach
  public void cleanUp() {
    cleanUpLokalTestDb(namedParameterJdbcTemplate);
  }

  @Test
  public void opprett__skal_lagre_næring_i_næringstabellen() {
    repository.opprett(new Næring("01", "Jordbruk"), Klassifikasjonskilde.NÆRING);

    List<Næring> næringList = hentNæring(namedParameterJdbcTemplate);
    assertThat(næringList.size()).isEqualTo(1);
    assertThat(næringList.get(0)).isEqualTo((new Næring("01", "Jordbruk")));
  }

  @Test
  public void hent__skal_hente_næring_fra_næringstabellen() {
    opprettNæring(namedParameterJdbcTemplate, "01", "Jordbruk");

    Optional<Virksomhetsklassifikasjon> næring =
        repository.hent(new Næring("01", "Jordbruk"), Klassifikasjonskilde.NÆRING);

    assertThat(næring.isPresent()).isTrue();
    assertThat(næring.get()).isEqualTo((new Næring("01", "Jordbruk")));
  }

  @Test
  public void oppdater__skal_oppdatere_data_i_næringstabellen() {
    opprettNæring(namedParameterJdbcTemplate, "01", "Jordbruk");

    int antallOppdatert =
        repository.oppdater(
            new Næring("01", "Jordbruk og andre ting"), Klassifikasjonskilde.NÆRING);

    Næring oppdatertNæring = hentNæring(namedParameterJdbcTemplate).get(0);
    assertThat(antallOppdatert).isEqualTo(1);
    assertThat(oppdatertNæring).isEqualTo((new Næring("01", "Jordbruk og andre ting")));
  }

  @Test
  public void opprett__skal_lagre_sektor_i_sektortabellen() {
    repository.opprett(new Sektor("1111", "offentlig"), Klassifikasjonskilde.SEKTOR);

    List<Sektor> sektorList = hentSektor(namedParameterJdbcTemplate);
    assertThat(sektorList.size()).isEqualTo(1);
    assertThat(sektorList.get(0)).isEqualTo((new Sektor("1111", "offentlig")));
  }

  @Test
  public void hent__skal_hente_sektor_fra_sektorstabellen() {
    opprettSektor(namedParameterJdbcTemplate, "2222", "privat");

    Optional<Virksomhetsklassifikasjon> sektor =
        repository.hent(new Næring("2222", "privat"), Klassifikasjonskilde.SEKTOR);

    assertThat(sektor.isPresent()).isTrue();
    assertThat(sektor.get()).isEqualTo((new Sektor("2222", "privat")));
  }

  @Test
  public void oppdater__skal_oppdatere_data_i_sektortabellen() {
    opprettSektor(namedParameterJdbcTemplate, "3333", "privat");

    int antallOppdatert =
        repository.oppdater(new Sektor("3333", "offentlig"), Klassifikasjonskilde.SEKTOR);

    Sektor oppdatertSektor = hentSektor(namedParameterJdbcTemplate).get(0);
    assertThat(antallOppdatert).isEqualTo(1);
    assertThat(oppdatertSektor).isEqualTo((new Sektor("3333", "offentlig")));
  }

  private static void cleanUpLokalTestDb(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    delete(namedParameterJdbcTemplate, "naring");
    delete(namedParameterJdbcTemplate, "sektor");
  }

  private static void delete(NamedParameterJdbcTemplate namedParameterJdbcTemplate, String tabell) {
    namedParameterJdbcTemplate.update(
        String.format("delete from %s", tabell), new MapSqlParameterSource());
  }

  private static List<Næring> hentNæring(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {

    return namedParameterJdbcTemplate.query(
        "select * from naring",
        new MapSqlParameterSource(),
        (rs, rowNum) -> new Næring(rs.getString("kode"), rs.getString("navn")));
  }

  private static void opprettNæring(
      NamedParameterJdbcTemplate namedParameterJdbcTemplate, String næringkode, String næringnavn) {
    namedParameterJdbcTemplate.update(
        String.format(
            "insert into naring (kode, navn) " + "values('%s', '%s')", næringkode, næringnavn),
        new MapSqlParameterSource());
  }

  private static List<Sektor> hentSektor(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {

    return namedParameterJdbcTemplate.query(
        "select * from sektor",
        new MapSqlParameterSource(),
        (rs, rowNum) -> new Sektor(rs.getString("kode"), rs.getString("navn")));
  }

  private static void opprettSektor(
      NamedParameterJdbcTemplate namedParameterJdbcTemplate, String kode, String navn) {
    namedParameterJdbcTemplate.update(
        String.format("insert into sektor (kode, navn) " + "values('%s', '%s')", kode, navn),
        new MapSqlParameterSource());
  }
}
