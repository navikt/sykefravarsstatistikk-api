package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.healthcheck;

import common.SpringIntegrationTestbase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.net.http.HttpClient.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.Assertions.assertThat;

public class HealthcheckControllerTestSpring extends SpringIntegrationTestbase {

  @LocalServerPort private String port;

  @Test
  public void healthcheck_returnerer_OK__når_applikasjon_kjører() throws Exception {
    HttpResponse<String> response =
        newBuilder()
            .build()
            .send(
                HttpRequest.newBuilder()
                    .uri(
                        URI.create(
                            "http://localhost:"
                                + port
                                + "/sykefravarsstatistikk-api/internal/healthcheck"))
                    .GET()
                    .build(),
                ofString());

    assertThat(response.statusCode()).isEqualTo(200);
  }
}
