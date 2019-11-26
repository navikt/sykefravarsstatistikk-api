package no.nav.tag.sykefravarsstatistikk.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"wiremock.mock.port=8083"})
public class ApiTest {

    @LocalServerPort
    private String port;

    private ObjectMapper objectMapper = new ObjectMapper();

    private final static String ORGNR_UNDERENHET = "822565212";

    @Test
    public void sammenligning__skal_returnere_riktig_objekt() throws Exception {
        HttpResponse<String> response = newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/" + ORGNR_UNDERENHET + "/sammenligning"))
                        .header(AUTHORIZATION, "Bearer " + JwtTokenGenerator.signedJWTAsString("15008462396"))
                        .GET()
                        .build(),
                ofString()
        );

        JsonNode ønsketResponseJson = objectMapper.readTree("{" +
                "  \"kvartal\": 2," +
                "  \"årstall\": 2019," +
                "  \"virksomhet\": {" +
                "    \"label\": \"NAV ARBEID OG YTELSER AVD OSLO\"," +
                "    \"prosent\": 12.0," +
                "    \"erMaskert\": false" +
                "  }," +
                "  \"næring\": {" +
                "    \"label\": \"Produksjon av nærings- og nytelsesmidler\"," +
                "    \"prosent\": 5.1," +
                "    \"erMaskert\": false" +
                "  }," +
                "  \"sektor\": {" +
                "    \"label\": \"Statlig forvaltning\"," +
                "    \"prosent\": 5.2," +
                "    \"erMaskert\": false" +
                "  }," +
                "  \"land\": {" +
                "    \"label\": \"Norge\"," +
                "    \"prosent\": 4.5," +
                "    \"erMaskert\": false" +
                "  }" +
                "}"
        );

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(response.body())).isEqualTo(ønsketResponseJson);
    }

}
