package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.importering;

import static org.assertj.core.api.Assertions.assertThat;

import common.SpringIntegrationTestbase;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.SykefraværsstatistikkImporteringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.ImporttidspunktDto;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.PubliseringsdatoerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class ImporttidspunktTest extends SpringIntegrationTestbase {

  @Autowired
  PubliseringsdatoerRepository publiseringsdatoerRepository;

  @Autowired
  SykefraværsstatistikkImporteringService sykefraværsstatistikkImporteringService;

  @Autowired
  NamedParameterJdbcTemplate jdbcTemplate;

  @BeforeEach
  public void setUp() {
    TestUtils.slettAlleImporttidspunkt(jdbcTemplate);
  }

  @AfterEach
  public void tearDown() {
    TestUtils.slettAlleImporttidspunkt(jdbcTemplate);
  }


  @Test
  public void importerHvisDetFinnesNyStatistikk_oppdatererImporttidspunkt() {

    ImporttidspunktDto gammeltImporttidspunkt = new ImporttidspunktDto(
        Timestamp.valueOf("2022-06-02 00:00:00"),
        new ÅrstallOgKvartal(2022, 1));
    TestUtils.skrivImporttidspunktTilDb(jdbcTemplate, gammeltImporttidspunkt);

    ImporttidspunktDto sistePubliseringsdatoPreImport =
        publiseringsdatoerRepository.hentSisteImporttidspunkt().get();

    assertThat(sistePubliseringsdatoPreImport).isEqualTo(gammeltImporttidspunkt);

    sykefraværsstatistikkImporteringService.importerHvisDetFinnesNyStatistikk();

    ImporttidspunktDto sistePubliseringsdatoPostImport =
        publiseringsdatoerRepository.hentSisteImporttidspunkt().get();

    assertThat(sistePubliseringsdatoPostImport.getSistImportertTidspunkt().toLocalDateTime()
        .toLocalDate()).isEqualTo(
        Timestamp.valueOf(LocalDateTime.now()).toLocalDateTime().toLocalDate());

    assertThat(sistePubliseringsdatoPostImport.getGjeldendePeriode()).isEqualTo(
        new ÅrstallOgKvartal(2022, 2));
  }
}

