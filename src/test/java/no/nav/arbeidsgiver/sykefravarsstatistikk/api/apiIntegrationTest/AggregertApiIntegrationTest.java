package no.nav.arbeidsgiver.sykefravarsstatistikk.api.apiIntegrationTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.SpringIntegrationTestbase;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestTokenUtil;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static java.net.http.HttpClient.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestTokenUtil.SELVBETJENING_ISSUER_ID;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestTokenUtil.TOKENX_ISSUER_ID;
import static org.assertj.core.api.Assertions.assertThat;

public class AggregertApiIntegrationTest extends SpringIntegrationTestbase {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    MockOAuth2Server mockOAuth2Server;

    @LocalServerPort
    private String port;

    private ObjectMapper objectMapper = new ObjectMapper();

    private final static String ORGNR_UNDERENHET = "910969439";
    private final static String ORGNR_UNDERENHET_INGEN_TILGANG = "777777777";




    @Test
    public void hentAgreggertStatistikk_skalReturnere200OK() throws Exception {
        String jwtToken = TestTokenUtil.createToken(
                mockOAuth2Server,
                "15008462396",
                TOKENX_ISSUER_ID,
                "https://oidc.difi.no/idporten-oidc-provider/"
        );

        HttpResponse<String> response = newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/" + ORGNR_UNDERENHET +
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
        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    public void hentAgreggertStatistikk_skal_returnere_403_naar_bruker_mangler_tilgang() throws Exception {
        String jwtToken = TestTokenUtil.createToken(
                mockOAuth2Server,
                "15008462396",
                TOKENX_ISSUER_ID,
                "https://oidc.difi.no/idporten-oidc-provider/"
        );

        HttpResponse<String> response = newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/" + ORGNR_UNDERENHET_INGEN_TILGANG +
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
        assertThat(response.statusCode()).isEqualTo(403);
    }
@Test
    public void hentAgreggertStatistikk_skal_returnere_virksomhetsstatistikk_og_andre_typer() throws Exception {
        String jwtToken = TestTokenUtil.createToken(
                mockOAuth2Server,
                "15008462396",
                TOKENX_ISSUER_ID,
                "https://oidc.difi.no/idporten-oidc-provider/"
        );

        HttpResponse<String> response = newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/" + ORGNR_UNDERENHET +
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
        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode responseBody = objectMapper.readTree(response.body());
        // TODO fullføre me
        assertThat(responseBody.size()).isEqualTo(4);
        //assertThat(responseBody.get(0).get("label")).isEqualTo(objectMapper.readTree("\"Næring\""));
    }
}
