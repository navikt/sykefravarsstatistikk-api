package no.nav.arbeidsgiver.sykefravarsstatistikk.api.apiIntegrationTest;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static java.net.http.HttpClient.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.Aggregeringstype.PROSENT_SISTE_4_KVARTALER_LAND;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.Aggregeringstype.PROSENT_SISTE_4_KVARTALER_NÆRING;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.Aggregeringstype.PROSENT_SISTE_4_KVARTALER_VIRKSOMHET;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.Aggregeringstype.TREND_NÆRING;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.SpringIntegrationTestbase;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestTokenUtil;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;

public class AggregertApiIntegrationTest extends SpringIntegrationTestbase {

    private final static String ORGNR_UNDERENHET = "910969439";
    private final static String ORGNR_UNDERENHET_INGEN_TILGANG = "777777777";

    @Autowired
    MockOAuth2Server mockOAuth2Server;

    @LocalServerPort
    private String port;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void hentAgreggertStatistikk_skal_returnere_403_naar_bruker_mangler_tilgang()
          throws Exception {
        String jwtToken = TestTokenUtil.createMockIdportenTokenXToken(mockOAuth2Server);

        HttpResponse<String> response = utførAutorisertKall(ORGNR_UNDERENHET_INGEN_TILGANG, jwtToken);
        assertThat(response.statusCode()).isEqualTo(403);
    }

    @Test
    public void hentAgreggertStatistikk_returnererForventedeTyperForBedriftSomHarAlleTyperData()
          throws Exception {
        String jwtToken = TestTokenUtil.createMockIdportenTokenXToken(mockOAuth2Server);

        HttpResponse<String> response = utførAutorisertKall(ORGNR_UNDERENHET, jwtToken);
        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode responseBody = objectMapper.readTree(response.body());

        assertThat(responseBody.findValuesAsText("type"))
              .containsExactlyInAnyOrderElementsOf(
                    List.of(
                          PROSENT_SISTE_4_KVARTALER_VIRKSOMHET.toString(),
                          PROSENT_SISTE_4_KVARTALER_NÆRING.toString(),
                          PROSENT_SISTE_4_KVARTALER_LAND.toString(),
                          TREND_NÆRING.toString()
                    ));
    }

    private HttpResponse<String> utførAutorisertKall(String orgnr, String jwtToken)
          throws IOException, InterruptedException {
        return newBuilder().build().send(
              HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/"
                          + orgnr +
                          "/sykefravarshistorikk/aggregert/siste")
                    )
                    .header(
                          AUTHORIZATION,
                          "Bearer " + jwtToken
                    )
                    .GET()
                    .build(),
              ofString()
        );
    }
}
