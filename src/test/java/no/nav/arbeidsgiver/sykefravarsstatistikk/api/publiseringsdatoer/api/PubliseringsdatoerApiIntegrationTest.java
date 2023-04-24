package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.api;

import static java.net.http.HttpClient.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.skrivSisteImporttidspunktTilDb;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import common.SpringIntegrationTestbase;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.PubliseringsdatoerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class PubliseringsdatoerApiIntegrationTest extends SpringIntegrationTestbase {

  @LocalServerPort private String port;

  @Autowired PubliseringsdatoerRepository publiseringsdatoerRepository;

  @Autowired NamedParameterJdbcTemplate jdbcTemplate;

  @BeforeEach
  public void setUp() {
    TestUtils.slettAlleImporttidspunkt(jdbcTemplate);
  }

  @AfterEach
  public void tearDown() {
    TestUtils.slettAlleImporttidspunkt(jdbcTemplate);
  }

  @Test
  public void hentPubliseringsdatoer_skalReturnereResponsMedKorrektFormat()
      throws IOException, InterruptedException {

    skrivSisteImporttidspunktTilDb(jdbcTemplate);

    HttpResponse<String> response =
        newBuilder()
            .build()
            .send(
                HttpRequest.newBuilder()
                    .uri(
                        URI.create(
                            "http://localhost:"
                                + port
                                + "/sykefravarsstatistikk-api/publiseringsdato"))
                    .GET()
                    .build(),
                ofString());

    String forventetRespons =
        ""
            + "{\"sistePubliseringsdato\":\"2022-06-02\","
            + "\"nestePubliseringsdato\":\"2022-09-08\","
            + "\"gjeldendePeriode\":{\"årstall\":2022,\"kvartal\":1}}";

    assertThat(response.body()).isEqualTo(forventetRespons);
  }
}
