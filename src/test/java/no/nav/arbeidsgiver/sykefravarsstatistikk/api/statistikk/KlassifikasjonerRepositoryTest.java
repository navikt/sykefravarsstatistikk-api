package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.AppConfigForJdbcTesterConfig;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.KlassifikasjonerRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfigForJdbcTesterConfig.class})
@DataJdbcTest(excludeAutoConfiguration = {TestDatabaseAutoConfiguration.class})
class KlassifikasjonerRepositoryTest {

  @Autowired private NamedParameterJdbcTemplate jdbcTemplate;

  private KlassifikasjonerRepository klassifikasjonerRepository;

  @BeforeEach
  public void setUp() {
    klassifikasjonerRepository = new KlassifikasjonerRepository(jdbcTemplate);
  }

  @Test
  void hentAlleNæringer() {
    opprettNæring(jdbcTemplate, "01", "Test næring 1");
    opprettNæring(jdbcTemplate, "02", "Test næring 2");
    opprettNæring(jdbcTemplate, "03", "Test næring 3");
    List<Næring> resultat = klassifikasjonerRepository.hentAlleNæringer();
    assertThat(resultat.size()).isEqualTo(3);
    assertThat(resultat)
        .containsExactlyInAnyOrderElementsOf(
            List.of(
                new Næring("01", "Test næring 1"),
                new Næring("02", "Test næring 2"),
                new Næring("03", "Test næring 3")));
  }

  // TODO fjerne denne dupliserte metode
  private static void opprettNæring(
      NamedParameterJdbcTemplate namedParameterJdbcTemplate, String næringkode, String næringnavn) {
    namedParameterJdbcTemplate.update(
        String.format(
            "insert into naring (kode, navn) " + "values('%s', '%s')", næringkode, næringnavn),
        new MapSqlParameterSource());
  }
}
