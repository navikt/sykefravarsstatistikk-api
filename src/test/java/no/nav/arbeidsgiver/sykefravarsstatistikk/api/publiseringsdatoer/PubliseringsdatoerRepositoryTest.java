package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import common.StaticAppender;
import io.vavr.control.Option;
import java.util.List;
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

  @Mock
  NamedParameterJdbcTemplate mockJdbcTemplate;

  @BeforeEach
  public void clearLoggingStatements() {
    StaticAppender.clearEvents();
  }

  @BeforeEach
  void setUp() {
    publiseringsdatoerRepository = new PubliseringsdatoerRepository(mockJdbcTemplate);
  }

  @Test
  void hentSistePubliseringstidspunkt_nårPubliseringsdatoIkkeBlirFunnet_skalReturnereTomOptionalOgLoggeError() {
    when(mockJdbcTemplate.query(anyString(), anyMap(), anyRowMapper())).thenReturn(List.of());

    Option<ImporttidspunktDto> faktisk = publiseringsdatoerRepository.hentSisteImporttidspunkt();
    assertTrue(faktisk.isEmpty());
    assertThat(StaticAppender.getEvents()).extracting("message");
  }

  private RowMapper<?> anyRowMapper() {
    return Mockito.any();
  }
}
