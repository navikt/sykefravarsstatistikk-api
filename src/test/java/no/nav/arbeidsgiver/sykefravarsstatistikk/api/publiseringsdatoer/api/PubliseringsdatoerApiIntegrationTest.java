package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.api;

import static java.net.http.HttpClient.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import common.SpringIntegrationTestbase;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.server.LocalServerPort;

public class PubliseringsdatoerApiIntegrationTest extends SpringIntegrationTestbase {

  @LocalServerPort
  private String port;


  @Test
  public void hentPubliseringsdatoer_skalReturnereResponsMedKorrektFormat()
      throws IOException, InterruptedException {
    HttpResponse<String> response = newBuilder().build().send(
        HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port
                + "/sykefravarsstatistikk-api/publiseringsdato"))
            .GET()
            .build(),
        ofString());

    String forventetRespons = ""
        + "{\"sistePubliseringsdato\":\"2022-06-02\","
        + "\"nestePubliseringsdato\":\"2022-09-08\","
        + "\"gjeldendePeriode\":{\"årstall\":2022,\"kvartal\":1}}";

    assertThat(response.body()).isEqualTo(forventetRespons);
  }
}

