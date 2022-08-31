package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.api;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.ImporttidspunktDto;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.PubliseringsdatoDbDto;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.PubliseringsdatoerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PubliseringsdatoerServiceTest {

    private final List<PubliseringsdatoDbDto> publiseringsdatoer = List.of(
          new PubliseringsdatoDbDto(
                202201,
                Date.valueOf("2022-06-02"),
                Date.valueOf("2021-11-01"),
                "Sykefravær pr 1. kvartal 2022"
          ),
          new PubliseringsdatoDbDto(
                202203,
                Date.valueOf("2022-12-01"),
                Date.valueOf("2021-11-01"),
                "Sykefravær pr 3. kvartal 2022"
          ),
          new PubliseringsdatoDbDto(
                202202,
                Date.valueOf("2022-09-08"),
                Date.valueOf("2021-11-01"),
                "Sykefravær pr 2. kvartal 2022"
          )
    );
    private PubliseringsdatoerService serviceUnderTest;
    @Mock
    private PubliseringsdatoerRepository mockPubliseringsdatoerRepository;


    @BeforeEach
    void setUp() {
        serviceUnderTest = new PubliseringsdatoerService(
              mockPubliseringsdatoerRepository
        );
    }

    @AfterEach
    void tearDown() {
        reset(
              mockPubliseringsdatoerRepository
        );
    }

    @Test
    void hentPubliseringsdatoer() {
        final ImporttidspunktDto importtidspunktDto = new ImporttidspunktDto(
              Timestamp.valueOf(LocalDateTime.of(2022, 9, 8, 8, 0)),
              "2022",
              "2"
        );

        when(mockPubliseringsdatoerRepository.hentSisteImporttidspunktMedPeriode()).thenReturn(importtidspunktDto);
        when(mockPubliseringsdatoerRepository.hentPubliseringsdatoer()).thenReturn(publiseringsdatoer);

        Publiseringsdatoer publiseringsdatoer = serviceUnderTest.hentPubliseringsdatoer();
        assertThat(publiseringsdatoer).isEqualTo(new Publiseringsdatoer(
              "2022",
              "2",
              "2022-09-08",
              "2022-12-01"
        ));
    }
}