package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.importering;

import static org.assertj.core.api.Assertions.assertThat;

import common.SpringIntegrationTestbase;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.SykefraværsstatistikkImporteringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.ImporttidspunktDto;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.PubliseringsdatoerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ImporttidspunktTest extends SpringIntegrationTestbase {

  @Autowired
  PubliseringsdatoerRepository publiseringsdatoerRepository;

  @Autowired
  SykefraværsstatistikkImporteringService sykefraværsstatistikkImporteringService;


  @Test
  public void hentPubliseringsdatoer_skalReturnereResponsMedKorrektFormat() {
    ImporttidspunktDto sistePubliseringsdatoPreImport = publiseringsdatoerRepository.hentSisteImporttidspunkt()
        .get();
    assertThat(sistePubliseringsdatoPreImport).isEqualTo(
        new ImporttidspunktDto(Timestamp.valueOf("2022-06-02 00:00:00"),
            new ÅrstallOgKvartal(2022, 1)));

    sykefraværsstatistikkImporteringService.importerHvisDetFinnesNyStatistikk();

    ImporttidspunktDto sistePubliseringsdatoPostImport = publiseringsdatoerRepository.hentSisteImporttidspunkt()
        .get();

    assertThat(sistePubliseringsdatoPostImport.getSistImportertTidspunkt().toLocalDateTime()
        .toLocalDate()).isEqualTo(
        Timestamp.valueOf(LocalDateTime.now()).toLocalDateTime().toLocalDate());

    assertThat(sistePubliseringsdatoPostImport.getGjeldendePeriode()).isEqualTo(
        new ÅrstallOgKvartal(2022, 2));
  }
}

