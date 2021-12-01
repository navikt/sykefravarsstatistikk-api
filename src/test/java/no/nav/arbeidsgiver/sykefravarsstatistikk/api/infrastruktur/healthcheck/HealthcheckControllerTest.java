package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.healthcheck;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.SykefraværsstatistikkLocalApplication;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.net.http.HttpClient.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("mvc-test")
@EnableMockOAuth2Server
@SpringBootTest(
        classes = SykefraværsstatistikkLocalApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@TestPropertySource(properties = {"wiremock.mock.port=8086"})
public class HealthcheckControllerTest {

    @LocalServerPort
    private String port;

    @Test
    public void healthcheck_returnerer_OK__når_applikasjon_kjører() throws Exception {
        HttpResponse<String> response = newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/internal/healthcheck"))
                        .GET()
                        .build(),
                ofString()
        );

        assertThat(response.statusCode()).isEqualTo(200);

    }

}
