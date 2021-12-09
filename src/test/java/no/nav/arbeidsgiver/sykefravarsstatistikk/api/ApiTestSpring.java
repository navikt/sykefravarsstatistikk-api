package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.SpringIntegrationTestbase;
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

public class ApiTestSpring extends SpringIntegrationTestbase {

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
    public void bedriftsmetrikker__skal_returnere_riktig_objekt() throws Exception {
        HttpResponse<String> response = newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/" + ORGNR_UNDERENHET + "/bedriftsmetrikker"))
                        .header(
                                AUTHORIZATION,
                                getBearerMedJwt("15008462396")
                        )
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
    public void sykefraværshistorikk__skal_returnere_riktig_objekt_med_en_selvbetjening_token() throws Exception {
        String jwtTokenIssuedByLoginservice = TestTokenUtil.createToken(
                mockOAuth2Server,
                "15008462396",
                SELVBETJENING_ISSUER_ID
        );

        sjekkAtSykefraværshistorikkReturnereRiktigObjekt(jwtTokenIssuedByLoginservice);
    }

    @Test
    public void sykefraværshistorikk__skal_returnere_riktig_objekt_med_en_token_fra_tokenx_og_opprinnelig_provider_er_idporten() throws Exception {
        String jwtToken = TestTokenUtil.createToken(
                mockOAuth2Server,
                "dette-er-ikke-et-fnr",
                TOKENX_ISSUER_ID,
                "https://oidc.difi.no/idporten-oidc-provider/",
                "15008462396"
        );

        sjekkAtSykefraværshistorikkReturnereRiktigObjekt(jwtToken);
    }

    @Test
    public void sykefraværshistorikk__skal_returnere_riktig_objekt_med_en_token_fra_tokenx_og_opprinnelig_provider_er_loginservice() throws Exception {
        String jwtToken = TestTokenUtil.createToken(
                mockOAuth2Server,
                "15008462396",
                TOKENX_ISSUER_ID,
                "https://navnob2c.b2clogin.com/something-unique-and-long/v2.0/",
                null
        );

        sjekkAtSykefraværshistorikkReturnereRiktigObjekt(jwtToken);
    }


    private void sjekkAtSykefraværshistorikkReturnereRiktigObjekt(String jwtToken) throws Exception {
        HttpResponse<String> response = newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/" + ORGNR_UNDERENHET +
                                "/sykefravarshistorikk/kvartalsvis")
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
                        .header(
                                AUTHORIZATION,
                                getBearerMedJwt("15008462396")
                        )
                        .GET()
                        .build(),
                ofString()
        );
        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.body()).isEqualTo("{\"message\":\"You don't have access to this ressource\"}");
    }


    @Test
    public void summert_sykefraværshistorikk_siste_4_kvartaler__skal_utføre_tilgangskontroll() throws IOException, InterruptedException {
        HttpResponse<String> response = newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/"
                                + ORGNR_UNDERENHET_INGEN_TILGANG + "/sykefravarshistorikk/summert?antallKvartaler=4"))
                        .header(
                                AUTHORIZATION,
                                getBearerMedJwt("15008462396")
                        )
                        .GET()
                        .build(),
                ofString()
        );
        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.body()).isEqualTo("{\"message\":\"You don't have access to this ressource\"}");
    }

    @Test
    public void legemeldtSykefraværsprosent__skal_returnere_riktig_objekt() throws Exception {
        HttpResponse<String> response = newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/" + ORGNR_UNDERENHET +
                                "/sykefravarshistorikk/legemeldtsykefravarsprosent")
                        )
                        .header(
                                AUTHORIZATION,
                                getBearerMedJwt("15008462396")
                        )
                        .GET()
                        .build(),
                ofString()
        );

        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode alleSykefraværshistorikk = objectMapper.readTree(response.body());

        assertThat(
                Stream.of(
                        Statistikkategori.VIRKSOMHET.toString(),
                        Statistikkategori.BRANSJE.toString(),
                        Statistikkategori.NÆRING.toString()
                ).anyMatch(alleSykefraværshistorikk.findValue("type").asText()::contains)
        );
        assertThat(alleSykefraværshistorikk.findValue("label").isTextual());
        assertThat(alleSykefraværshistorikk.findValue("prosent").isNumber());
    }

    @Test
    public void legemeldt_sykefraværsprosent_siste_4_kvartaler__skal_utføre_tilgangskontroll() throws IOException, InterruptedException {
        HttpResponse<String> response = newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/"
                                + ORGNR_UNDERENHET_INGEN_TILGANG + "/sykefravarshistorikk/legemeldtsykefravarsprosent"))
                        .header(
                                AUTHORIZATION,
                                getBearerMedJwt("15008462396")
                        )
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
                        .header(
                                AUTHORIZATION,
                                getBearerMedJwt("15008462396")
                        )
                        .GET()
                        .build(),
                ofString()
        );

        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode responseBody = objectMapper.readTree(response.body());


        assertThat(responseBody).isEqualTo(objectMapper.readTree(getSummertSykefraværshistorikkResponseBody()));
    }

    @Test
    public void legemeldtSykefraværsprosent__når_virksomhet_ikke_har_næring_returnerer_204() throws Exception {
        HttpResponse<String> response = newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/" + "555555555" +
                                "/sykefravarshistorikk/legemeldtsykefravarsprosent")
                        )
                        .header(
                                AUTHORIZATION,
                                getBearerMedJwt("15008462396")
                        )
                        .GET()
                        .build(),
                ofString()
        );

        assertThat(response.statusCode()).isEqualTo(204);
        JsonNode body = objectMapper.readTree(response.body());

        assertThat(body).isNullOrEmpty();
    }

    @NotNull
    private String getBearerMedJwt(String subject) {
        return "Bearer "
                + TestTokenUtil.createToken(
                mockOAuth2Server,
                subject,
                SELVBETJENING_ISSUER_ID,
                "",
                ""
        );
    }

    private static String getSummertSykefraværshistorikkResponseBody() {
        return
                "[" +
                        "  {" +
                        "    \"type\": \"VIRKSOMHET\"," +
                        "    \"label\": \"NAV ARBEID OG YTELSER AVD OSLO\"," +
                        "    \"summertKorttidsOgLangtidsfravær\": {" +
                        "      \"summertKorttidsfravær\": {" +
                        "        \"prosent\": 5.5," +
                        "        \"tapteDagsverk\": 110.3," +
                        "        \"muligeDagsverk\": 1990.3," +
                        "        \"erMaskert\": false," +
                        "        \"kvartaler\": [" +
                        "          {" +
                        "            \"årstall\": 2020," +
                        "            \"kvartal\": 3" +
                        "          }" +
                        "        ]" +
                        "      }," +
                        "      \"summertLangtidsfravær\": {" +
                        "        \"prosent\": 2.8," +
                        "        \"tapteDagsverk\": 55.6," +
                        "        \"muligeDagsverk\": 1990.3," +
                        "        \"erMaskert\": false," +
                        "        \"kvartaler\": [" +
                        "          {" +
                        "            \"årstall\": 2020," +
                        "            \"kvartal\": 3" +
                        "          }" +
                        "        ]" +
                        "      }" +
                        "    }," +
                        "    \"summertGradertFravær\": {" +
                        "      \"prosent\": 0.8," +
                        "      \"tapteDagsverk\": 4.7," +
                        "      \"muligeDagsverk\": 574.8," +
                        "      \"erMaskert\": false," +
                        "      \"kvartaler\": [" +
                        "        {" +
                        "          \"årstall\": 2020," +
                        "          \"kvartal\": 3" +
                        "        }" +
                        "      ]" +
                        "    }" +
                        "  }," +
                        "  {" +
                        "    \"type\": \"NÆRING\"," +
                        "    \"label\": \"Produksjon av nærings- og nytelsesmidler\"," +
                        "    \"summertKorttidsOgLangtidsfravær\": {" +
                        "      \"summertKorttidsfravær\": {" +
                        "        \"prosent\": 3.3," +
                        "        \"tapteDagsverk\": 33.2," +
                        "        \"muligeDagsverk\": 999.1," +
                        "        \"erMaskert\": false," +
                        "        \"kvartaler\": [" +
                        "          {" +
                        "            \"årstall\": 2020," +
                        "            \"kvartal\": 3" +
                        "          }" +
                        "        ]" +
                        "      }," +
                        "      \"summertLangtidsfravær\": {" +
                        "        \"prosent\": 13.8," +
                        "        \"tapteDagsverk\": 137.8," +
                        "        \"muligeDagsverk\": 999.1," +
                        "        \"erMaskert\": false," +
                        "        \"kvartaler\": [" +
                        "          {" +
                        "            \"årstall\": 2020," +
                        "            \"kvartal\": 3" +
                        "          }" +
                        "        ]" +
                        "      }" +
                        "    }," +
                        "    \"summertGradertFravær\": {" +
                        "      \"prosent\": 1.5," +
                        "      \"tapteDagsverk\": 17.7," +
                        "      \"muligeDagsverk\": 1149.6," +
                        "      \"erMaskert\": false," +
                        "      \"kvartaler\": [" +
                        "        {" +
                        "          \"årstall\": 2020," +
                        "          \"kvartal\": 3" +
                        "        }" +
                        "      ]" +
                        "    }" +
                        "  }" +
                        "]";
    }
}
