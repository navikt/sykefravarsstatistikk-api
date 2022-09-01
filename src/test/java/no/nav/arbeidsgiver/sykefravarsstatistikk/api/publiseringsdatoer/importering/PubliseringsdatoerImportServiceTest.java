package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.importering;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.DatavarehusRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.PubliseringsdatoDbDto;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.PubliseringsdatoerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PubliseringsdatoerImportServiceTest {
    private PubliseringsdatoerImportService serviceUnderTest;
    @Mock
    private PubliseringsdatoerRepository mockPubliseringsdatoerRepository;
    @Mock
    private DatavarehusRepository mockDatavarehusRepository;


    @BeforeEach
    void setUp() {
        serviceUnderTest = new PubliseringsdatoerImportService(
              mockPubliseringsdatoerRepository,
              mockDatavarehusRepository
        );
    }

    @AfterEach
    void tearDown() {
        reset(
              mockPubliseringsdatoerRepository,
              mockDatavarehusRepository
        );
    }

    @Test
    void importerDatoerFraDatavarehus_oppdaterPubliseringsdatoerBlirKjørtEnGang() {
        List<PubliseringsdatoDbDto> publiseringsdatoDbDtoListe = List.of(new PubliseringsdatoDbDto(
              202202,
              Date.valueOf(LocalDate.MIN),
              Date.valueOf(LocalDate.MIN),
              "sykefravær for en periode"
        ));
        when(mockDatavarehusRepository.hentPubliseringsdatoerFraDvh()).thenReturn(publiseringsdatoDbDtoListe);
        serviceUnderTest.importerDatoerFraDatavarehus();
        verify(mockPubliseringsdatoerRepository, times(1))
              .oppdaterPubliseringsdatoer(publiseringsdatoDbDtoListe);

        // TODO: Sjekk at også publiseringssstatustabellen blir opdatert riktig
    }
}