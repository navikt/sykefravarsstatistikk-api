package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.importering;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.PubliseringsdatoerImportService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.PubliseringsdatoDbDto;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.PubliseringsdatoerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PubliseringsdatoerImportServiceTest {

  private PubliseringsdatoerImportService serviceUnderTest;
  @Mock private PubliseringsdatoerRepository mockPubliseringsdatoerRepository;
  @Mock private DatavarehusRepository mockDatavarehusRepository;

  @BeforeEach
  void setUp() {
    serviceUnderTest =
        new PubliseringsdatoerImportService(
            mockPubliseringsdatoerRepository, mockDatavarehusRepository);
  }

  @AfterEach
  void tearDown() {
    reset(mockPubliseringsdatoerRepository, mockDatavarehusRepository);
  }

  @Test
  void importerDatoerFraDatavarehus_oppdaterPubliseringsdatoerBlirKjørtEnGang() {
    List<PubliseringsdatoDbDto> publiseringsdatoDbDtoListe =
        List.of(
            new PubliseringsdatoDbDto(
                202202,
                Date.valueOf(LocalDate.MIN),
                Date.valueOf(LocalDate.MIN),
                "sykefravær for en periode"));
    when(mockDatavarehusRepository.hentPubliseringsdatoerFraDvh())
        .thenReturn(publiseringsdatoDbDtoListe);
    serviceUnderTest.importerDatoerFraDatavarehus();
    verify(mockPubliseringsdatoerRepository, times(1))
        .oppdaterPubliseringsdatoer(publiseringsdatoDbDtoListe);
  }
}
