package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import no.nav.security.oidc.test.support.JwtTokenGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static java.net.http.HttpClient.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"wiremock.mock.port=8083"})
public class ApiErrorMappingTest {

    @LocalServerPort
    private String port;

    private final static String ORGNR_UNDERENHET_UTEN_NÆRING = "555555555";


    @Test
    public void bedriftsmetrikker__skal_returnere_BAD_REQUEST_dersom_underenhet_ikke_har_noe_næringskode()
            throws Exception {
        HttpResponse<String> response = newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        "http://localhost:" + port
                                                + "/sykefravarsstatistikk-api/" + ORGNR_UNDERENHET_UTEN_NÆRING
                                                + "/bedriftsmetrikker"
                                )
                        )
                        .header(
                                AUTHORIZATION,
                                "Bearer " + JwtTokenGenerator.signedJWTAsString("15008462396")
                        )
                        .GET()
                        .build(),
                ofString()
        );

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).isEqualTo("");
    }
}
