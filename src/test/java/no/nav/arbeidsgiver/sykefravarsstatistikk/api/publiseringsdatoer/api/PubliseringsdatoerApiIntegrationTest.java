package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.api;

import common.SpringIntegrationTestbase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.server.LocalServerPort;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.net.http.HttpClient.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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

        String forventetRespons = "{"
              + "\"gjeldendeÅrstall\":\"2022\","
              + "\"gjeldendeKvartal\":\"02\","
              + "\"forrigePubliseringsdato\":\"2022-09-08\","
              + "\"nestePubliseringsdato\":\"2022-12-01\""
              + "}";

        assertThat(response.body()).isEqualTo(forventetRespons);
    }
}

