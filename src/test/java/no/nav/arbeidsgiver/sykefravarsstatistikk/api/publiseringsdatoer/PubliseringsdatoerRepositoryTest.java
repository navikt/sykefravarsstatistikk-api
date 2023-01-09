package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import common.StaticAppender;
import io.vavr.control.Option;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@ExtendWith(MockitoExtension.class)
class PubliseringsdatoerRepositoryTest {
  PubliseringsdatoerRepository publiseringsdatoerRepository;

  @Mock NamedParameterJdbcTemplate mockJdbcTemplate;

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
    when(mockJdbcTemplate.query(anyString(), anyMap(), anyRowMapper())).thenReturn(List.of());

    Option<ImporttidspunktDto> faktisk = publiseringsdatoerRepository.hentSisteImporttidspunkt();
    assertTrue(faktisk.isEmpty());
    ILoggingEvent errormelding = StaticAppender.getLastLoggedEvent();

    assertThat(errormelding.getLevel()).isEqualTo(Level.ERROR);
    assertThat(errormelding.getMessage())
        .isEqualTo(
            "Klarte ikke hente ut siste importtidspunkt: java.lang.ArrayIndexOutOfBoundsException: "
                + "Index 0 out of bounds for length 0");
  }

  private RowMapper<?> anyRowMapper() {
    return Mockito.any();
  }
}
