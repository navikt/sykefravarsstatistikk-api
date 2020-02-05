package no.nav.tag.sykefravarsstatistikk.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.security.oidc.test.support.JwtTokenGenerator;
import no.nav.tag.sykefravarsstatistikk.api.sykefravarprosenthistrorikk.SykefraværsstatistikkType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.stream.Collectors;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static java.net.http.HttpClient.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"wiremock.mock.port=8083"})
public class ApiTest {

    @LocalServerPort
    private String port;

    private ObjectMapper objectMapper = new ObjectMapper();

    private final static String ORGNR_UNDERENHET = "910969439";
    private final static String ORGNR_UNDERENHET_INGEN_TILGANG = "777777777";


    @Test
    public void sykefraværprosenthistorikk_sektor___skal_returnere_riktig_objekt() throws Exception {
        HttpResponse<String> response = newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/" + ORGNR_UNDERENHET + "/sykefravarprosenthistorikk"))
                        .header(AUTHORIZATION, "Bearer " + JwtTokenGenerator.signedJWTAsString("15008462396"))
                        .GET()
                        .build(),
                ofString()
        );

        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode alleSykefraværprosentHistorikk = objectMapper.readTree(response.body());

        assertThat(
                alleSykefraværprosentHistorikk.findValues("sykefraværsstatistikkType")
                        .stream()
                        .map(v -> v.textValue())
                        .collect(Collectors.toList()))
                .containsExactlyInAnyOrderElementsOf(
                        Arrays.asList(
                                SykefraværsstatistikkType.LAND.toString(),
                                SykefraværsstatistikkType.SEKTOR.toString(),
                                SykefraværsstatistikkType.NÆRING.toString()
                        )
                );

        assertThat(alleSykefraværprosentHistorikk.get(0).get("label")).isEqualTo(objectMapper.readTree("\"Norge\""));
        assertThat(alleSykefraværprosentHistorikk.get(0).get("kvartalsvisSykefraværProsent").get(0))
                .isEqualTo(objectMapper.readTree(
                        "{\"erMaskert\": false,\"kvartal\": 4,\"årstall\": 2014,\"prosent\": 5.4}"
                        )
                );
        assertThat(alleSykefraværprosentHistorikk.get(1).get("label")).isEqualTo(objectMapper.readTree("\"Statlig forvaltning\""));
        assertThat(alleSykefraværprosentHistorikk.get(1).get("kvartalsvisSykefraværProsent").get(0))
                .isEqualTo(objectMapper.readTree(
                        "{\"erMaskert\": false,\"kvartal\": 4,\"årstall\": 2014,\"prosent\": 5.0}"
                        )
                );
        assertThat(alleSykefraværprosentHistorikk.get(2).get("label")).isEqualTo(objectMapper.readTree("\"Produksjon av nærings- og nytelsesmidler\""));
        assertThat(alleSykefraværprosentHistorikk.get(2).get("kvartalsvisSykefraværProsent").get(0))
                .isEqualTo(objectMapper.readTree(
                        "{\"erMaskert\": false,\"kvartal\": 4,\"årstall\": 2017,\"prosent\": 5.4}"
                        )
                );

    }

    @Test
    public void sykefraværprosenthistorikk_sektor__skal_utføre_tilgangskontroll() throws IOException, InterruptedException {
        HttpResponse<String> response = newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/"
                                + ORGNR_UNDERENHET_INGEN_TILGANG + "/sykefravarprosenthistorikk"))
                        .header(AUTHORIZATION, "Bearer " + JwtTokenGenerator.signedJWTAsString("15008462396"))
                        .GET()
                        .build(),
                ofString()
        );
        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.body()).isEqualTo("{\"message\":\"You don't have access to this ressource\"}");
    }


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
                "  \"næring\": null," +
                "  \"bransje\": {" +
                "    \"label\": \"Næringsmiddelsindustrien\"," +
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

    @Test
    public void sammenligning__skal_utføre_tilgangskontroll() throws Exception {
        HttpResponse<String> response = newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/" + ORGNR_UNDERENHET_INGEN_TILGANG + "/sammenligning"))
                        .header(AUTHORIZATION, "Bearer " + JwtTokenGenerator.signedJWTAsString("15008462396"))
                        .GET()
                        .build(),
                ofString()
        );
        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.body()).isEqualTo("{\"message\":\"You don't have access to this ressource\"}");
    }

    @Test
    public void tapteDgsverk__skal_returnere_riktig_object() throws IOException, InterruptedException {
        HttpResponse<String> response = newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/" + ORGNR_UNDERENHET + "/tapteDagsverk"))
                        .header(AUTHORIZATION, "Bearer " + JwtTokenGenerator.signedJWTAsString("15008462396"))
                        .GET()
                        .build(),
                ofString()
        );
        JsonNode ønsketResponseJson = objectMapper.readTree("[" +
                "  {" +
                "    \"tapteDagsverk\": 154.175982," +
                "    \"årstall\": 2018," +
                "    \"kvartal\": 3" +
                "  }," +
                "  {" +
                "    \"tapteDagsverk\": 195.948185," +
                "    \"årstall\": 2018," +
                "    \"kvartal\": 4" +
                "  }," +
                "  {" +
                "    \"tapteDagsverk\": 251.441100," +
                "    \"årstall\": 2019," +
                "    \"kvartal\": 1" +
                "  }," +
                "  {" +
                "    \"tapteDagsverk\": 240.323100," +
                "    \"årstall\": 2019," +
                "    \"kvartal\": 2" +
                "  }" +
                "]"
        );

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(response.body())).isEqualTo(ønsketResponseJson);
    }

    @Test
    public void tapteDgsverk__skal_utføre_tilgangskontroll() throws IOException, InterruptedException {
        HttpResponse<String> response = newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/" + ORGNR_UNDERENHET_INGEN_TILGANG + "/tapteDagsverk"))
                        .header(AUTHORIZATION, "Bearer " + JwtTokenGenerator.signedJWTAsString("15008462396"))
                        .GET()
                        .build(),
                ofString()
        );
        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.body()).isEqualTo("{\"message\":\"You don't have access to this ressource\"}");
    }
}
