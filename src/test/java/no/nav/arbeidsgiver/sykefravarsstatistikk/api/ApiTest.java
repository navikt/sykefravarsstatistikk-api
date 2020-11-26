package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.security.token.support.test.JwtTokenGenerator;
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
    public void bedriftsmetrikker__skal_returnere_riktig_objekt() throws Exception {
        HttpResponse<String> response = newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/" + ORGNR_UNDERENHET + "/bedriftsmetrikker"))
                        .header(AUTHORIZATION, "Bearer " + JwtTokenGenerator.signedJWTAsString("15008462396"))
                        .GET()
                        .build(),
                ofString()
        );

        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode bedriftsmetrikker = objectMapper.readTree(response.body());

        assertThat(bedriftsmetrikker.get("næringskode5Siffer"))
                .isEqualTo(objectMapper.readTree("{" +
                        "        \"kode\": \"10300\"," +
                        "        \"beskrivelse\": \"Trygdeordninger underlagt offentlig forvaltning\"" +
                        "    }")
                );

        assertThat(bedriftsmetrikker.get("antallAnsatte")).isEqualTo(objectMapper.readTree("143"));
    }

    @Test
    public void sykefraværshistorikk__skal_returnere_riktig_objekt() throws Exception {
        HttpResponse<String> response = newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/" + ORGNR_UNDERENHET +
                                "/sykefravarshistorikk/kvartalsvis")
                        )
                        .header(AUTHORIZATION, "Bearer " + JwtTokenGenerator.signedJWTAsString("15008462396"))
                        .GET()
                        .build(),
                ofString()
        );

        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode alleSykefraværshistorikk = objectMapper.readTree(response.body());

        assertThat(
                alleSykefraværshistorikk.findValues("type")
                        .stream()
                        .map(v -> v.textValue())
                        .collect(Collectors.toList()))
                .containsExactlyInAnyOrderElementsOf(
                        Arrays.asList(
                                Statistikkategori.LAND.toString(),
                                Statistikkategori.SEKTOR.toString(),
                                Statistikkategori.NÆRING.toString(),
                                Statistikkategori.VIRKSOMHET.toString(),
                                Statistikkategori.OVERORDNET_ENHET.toString()
                        )
                );

        assertThat(alleSykefraværshistorikk.get(0).get("label")).isEqualTo(objectMapper.readTree("\"Norge\""));
        assertThat(alleSykefraværshistorikk.get(0).get("kvartalsvisSykefraværsprosent").get(0))
                .isEqualTo(objectMapper.readTree(
                        "{\"tapteDagsverk\":5884917.3,\"muligeDagsverk\":1.125256909E8,\"prosent\":5.2,\"erMaskert\":false,\"årstall\":2014,\"kvartal\":2}"
                        )
                );
        assertThat(alleSykefraværshistorikk.get(1).get("label")).isEqualTo(objectMapper.readTree("\"Statlig forvaltning\""));
        assertThat(alleSykefraværshistorikk.get(1).get("kvartalsvisSykefraværsprosent").get(0))
                .isEqualTo(objectMapper.readTree(
                        "{\"tapteDagsverk\":657853.3,\"muligeDagsverk\":1.35587109E7,\"prosent\":4.9,\"årstall\":2014,\"kvartal\":2,\"erMaskert\":false}"
                        )
                );
        assertThat(alleSykefraværshistorikk.get(2).get("label")).isEqualTo(objectMapper.readTree("\"Produksjon av nærings- og nytelsesmidler\""));
        assertThat(alleSykefraværshistorikk.get(2).get("kvartalsvisSykefraværsprosent").get(0))
                .isEqualTo(objectMapper.readTree(
                        "{\"tapteDagsverk\":144324.8,\"muligeDagsverk\":2562076.9,\"prosent\":5.6,\"årstall\":2017,\"kvartal\":1,\"erMaskert\":false}"
                        )
                );
        assertThat(alleSykefraværshistorikk.get(3).get("label")).isEqualTo(objectMapper.readTree("\"NAV ARBEID OG YTELSER AVD OSLO\""));
        assertThat(alleSykefraværshistorikk.get(3).get("kvartalsvisSykefraværsprosent").get(0))
                .isEqualTo(objectMapper.readTree(
                        "{\"tapteDagsverk\":235.3,\"muligeDagsverk\":929.3,\"prosent\":25.3,\"årstall\":2014,\"kvartal\":2,\"erMaskert\":false}"
                        )
                );
        assertThat(alleSykefraværshistorikk.get(4).get("label")).isEqualTo(objectMapper.readTree("\"NAV ARBEID OG YTELSER\""));
        assertThat(alleSykefraværshistorikk.get(4).get("kvartalsvisSykefraværsprosent").get(0))
                .isEqualTo(objectMapper.readTree(
                        "{\"tapteDagsverk\":2000.3,\"muligeDagsverk\":9290.3,\"prosent\":21.5,\"årstall\":2014,\"kvartal\":2,\"erMaskert\":false}"
                        )
                );

    }

    @Test
    public void sykefraværshistorikk_sektor__skal_utføre_tilgangskontroll() throws IOException, InterruptedException {
        HttpResponse<String> response = newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/"
                                + ORGNR_UNDERENHET_INGEN_TILGANG + "/sykefravarshistorikk/kvartalsvis"))
                        .header(AUTHORIZATION, "Bearer " + JwtTokenGenerator.signedJWTAsString("15008462396"))
                        .GET()
                        .build(),
                ofString()
        );
        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.body()).isEqualTo("{\"message\":\"You don't have access to this ressource\"}");
    }


    @Test
    public void summert_sykefraværshistorikk_siste_4_kvartaler_V2__skal_utføre_tilgangskontroll() throws IOException, InterruptedException {
        HttpResponse<String> response = newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/"
                                + ORGNR_UNDERENHET_INGEN_TILGANG + "/sykefravarshistorikk/summert/v2?antallKvartaler=4"))
                        .header(AUTHORIZATION, "Bearer " + JwtTokenGenerator.signedJWTAsString("15008462396"))
                        .GET()
                        .build(),
                ofString()
        );
        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.body()).isEqualTo("{\"message\":\"You don't have access to this ressource\"}");
    }

    @Test
    public void summert_sykefraværshistorikk_siste_4_kvartaler_v2__skal_returnere_riktig_objekt() throws Exception {
        HttpResponse<String> response = newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/" + ORGNR_UNDERENHET
                                + "/sykefravarshistorikk/summert/v2?antallKvartaler=4"))
                        .header(AUTHORIZATION, "Bearer " + JwtTokenGenerator.signedJWTAsString("15008462396"))
                        .GET()
                        .build(),
                ofString()
        );

        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode responseBody = objectMapper.readTree(response.body());


        assertThat(responseBody).isEqualTo(objectMapper.readTree(getSummertSykefraværshistorikkResponseBody()));
    }
@Test
    public void summert_sykefraværshistorikk_siste_4_kvartaler__skal_utføre_tilgangskontroll() throws IOException, InterruptedException {
        HttpResponse<String> response = newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/"
                                + ORGNR_UNDERENHET_INGEN_TILGANG + "/sykefravarshistorikk/summert?antallKvartaler=4"))
                        .header(AUTHORIZATION, "Bearer " + JwtTokenGenerator.signedJWTAsString("15008462396"))
                        .GET()
                        .build(),
                ofString()
        );
        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.body()).isEqualTo("{\"message\":\"You don't have access to this ressource\"}");
    }

    @Test
    public void summert_sykefraværshistorikk_siste_4_kvartaler__skal_returnere_riktig_objekt() throws Exception {
        HttpResponse<String> response = newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/" + ORGNR_UNDERENHET
                                + "/sykefravarshistorikk/summert?antallKvartaler=4"))
                        .header(AUTHORIZATION, "Bearer " + JwtTokenGenerator.signedJWTAsString("15008462396"))
                        .GET()
                        .build(),
                ofString()
        );

        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode responseBody = objectMapper.readTree(response.body());


        assertThat(responseBody).isEqualTo(objectMapper.readTree(getSummertSykefraværshistorikkResponseBody()));
    }


    private static String getSummertSykefraværshistorikkResponseBody() {
        return
                "[" +
                        "  {" +
                        "    \"type\": \"VIRKSOMHET\"," +
                        "    \"label\": \"NAV ARBEID OG YTELSER AVD OSLO\"," +
                        "    \"summertKorttidsOgLangtidsfravær\": {" +
                        "      \"summertKorttidsfravær\": {" +
                        "        \"prosent\": 3.7," +
                        "        \"tapteDagsverk\": 148.9," +
                        "        \"muligeDagsverk\": 3979.6," +
                        "        \"erMaskert\": false," +
                        "        \"kvartaler\": [" +
                        "          {" +
                        "            \"årstall\": 2019," +
                        "            \"kvartal\": 3" +
                        "          }," +
                        "          {" +
                        "            \"årstall\": 2020," +
                        "            \"kvartal\": 1" +
                        "          }" +
                        "        ]" +
                        "      }," +
                        "      \"summertLangtidsfravær\": {" +
                        "        \"prosent\": 3.0," +
                        "        \"tapteDagsverk\": 121.3," +
                        "        \"muligeDagsverk\": 3979.6," +
                        "        \"erMaskert\": false," +
                        "        \"kvartaler\": [" +
                        "          {" +
                        "            \"årstall\": 2019," +
                        "            \"kvartal\": 3" +
                        "          }," +
                        "          {" +
                        "            \"årstall\": 2020," +
                        "            \"kvartal\": 1" +
                        "          }" +
                        "        ]" +
                        "      }" +
                        "    }" +
                        "  }," +
                        "  {" +
                        "    \"type\": \"NÆRING\"," +
                        "    \"label\": \"Produksjon av nærings- og nytelsesmidler\"," +
                        "    \"summertKorttidsOgLangtidsfravær\": {" +
                        "      \"summertKorttidsfravær\": {" +
                        "        \"prosent\": 9.7," +
                        "        \"tapteDagsverk\": 394.1," +
                        "        \"muligeDagsverk\": 4082.8," +
                        "        \"erMaskert\": false," +
                        "        \"kvartaler\": [" +
                        "          {" +
                        "            \"årstall\": 2019," +
                        "            \"kvartal\": 3" +
                        "          }," +
                        "          {" +
                        "            \"årstall\": 2019," +
                        "            \"kvartal\": 4" +
                        "          }," +
                        "          {" +
                        "            \"årstall\": 2020," +
                        "            \"kvartal\": 1" +
                        "          }," +
                        "          {" +
                        "            \"årstall\": 2020," +
                        "            \"kvartal\": 2" +
                        "          }" +
                        "        ]" +
                        "      }," +
                        "      \"summertLangtidsfravær\": {" +
                        "        \"prosent\": 10.1," +
                        "        \"tapteDagsverk\": 411.1," +
                        "        \"muligeDagsverk\": 4082.8," +
                        "        \"erMaskert\": false," +
                        "        \"kvartaler\": [" +
                        "          {" +
                        "            \"årstall\": 2019," +
                        "            \"kvartal\": 3" +
                        "          }," +
                        "          {" +
                        "            \"årstall\": 2019," +
                        "            \"kvartal\": 4" +
                        "          }," +
                        "          {" +
                        "            \"årstall\": 2020," +
                        "            \"kvartal\": 1" +
                        "          }," +
                        "          {" +
                        "            \"årstall\": 2020," +
                        "            \"kvartal\": 2" +
                        "          }" +
                        "        ]" +
                        "      }" +
                        "    }" +
                        "  }" +
                        "]";
    }
}
