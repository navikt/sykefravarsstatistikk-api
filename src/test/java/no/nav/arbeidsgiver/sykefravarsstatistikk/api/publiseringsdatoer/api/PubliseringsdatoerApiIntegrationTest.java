package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.api;

import static java.net.http.HttpClient.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.SpringIntegrationTestbase;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

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

        String forventetRespons = "{\n"
              + "  \"gjeldendeÅrstall\": \"2022\",\n"
              + "  \"gjeldendeKvartal\": \"1\",\n"
              + "  \"forrigePubliseringsdato\": \"2022-02-06\", \n"
              + "  \"nestePubliseringsdato\": \"2022-09-08\"\n"
              + "}";

        assertThat(response.body()).isEqualTo(forventetRespons);
    }
}

