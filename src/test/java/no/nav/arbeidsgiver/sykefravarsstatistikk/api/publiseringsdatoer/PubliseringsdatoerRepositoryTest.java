package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import common.StaticAppender;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ImporttidspunktDto;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.PubliseringsdatoerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PubliseringsdatoerRepositoryTest {
    PubliseringsdatoerRepository publiseringsdatoerRepository;

    @Mock
    NamedParameterJdbcTemplate mockJdbcTemplate;

    @BeforeEach
    void setUp() {
        publiseringsdatoerRepository = new PubliseringsdatoerRepository(mockJdbcTemplate);
        StaticAppender.clearEvents();
    }

    @AfterEach
    void tearDown() {
        reset(mockJdbcTemplate);
    }

    @Test
    void
    hentSistePubliseringstidspunkt_nårPubliseringsdatoIkkeBlirFunnet_skalReturnereTomOptionalOgLoggeError() {
        when(mockJdbcTemplate.query(anyString(), anyRowMapper())).thenReturn(List.of());

        ImporttidspunktDto faktisk = publiseringsdatoerRepository.hentSisteImporttidspunkt();
        assertNull(faktisk);
    }

    private RowMapper<?> anyRowMapper() {
        return Mockito.any();
    }
}
