package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.SpringIntegrationTestbase;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class ApiEndpointsIntegrationTest extends SpringIntegrationTestbase {

    private final int SISTE_ÅRSTALL = ÅrstallOgKvartal.sisteKvartal().getÅrstall();
    private final int SISTE_KVARTAL = ÅrstallOgKvartal.sisteKvartal().getKvartal();

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    MockOAuth2Server mockOAuth2Server;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @LocalServerPort
    private String port;

    private ObjectMapper objectMapper = new ObjectMapper();

    private final static String ORGNR_UNDERENHET = "910969439";
    private final static String ORGNR_OVERORDNET_ENHET = "999263550";
    private final static String ORGNR_UNDERENHET_INGEN_TILGANG = "777777777";

    @BeforeEach
    public void setUp() {
        slettAllStatistikkFraDatabase(jdbcTemplate);
    }

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
              SELVBETJENING_ISSUER_ID,
              ""
        );
        opprettGenerellStatistikk();
        sjekkAtSykefraværshistorikkReturnereRiktigObjekt(jwtTokenIssuedByLoginservice);
    }

    @Test
    public void sykefraværshistorikk__skal_returnere_riktig_objekt_med_en_selvbetjening_token__issued_med_sub() throws Exception {
        String jwtTokenIssuedByLoginservice = TestTokenUtil.createToken(
              mockOAuth2Server,
              "",
              "15008462396",
              SELVBETJENING_ISSUER_ID,
              ""
        );
        opprettGenerellStatistikk();
        sjekkAtSykefraværshistorikkReturnereRiktigObjekt(jwtTokenIssuedByLoginservice);
    }

    @Test
    public void sykefraværshistorikk__skal_returnere_riktig_objekt_med_en_token_fra_tokenx_og_opprinnelig_provider_er_idporten() throws Exception {
        String jwtToken = TestTokenUtil.createToken(
              mockOAuth2Server,
              "15008462396",
              TOKENX_ISSUER_ID,
              "https://oidc.difi.no/idporten-oidc-provider/"
        );
        opprettGenerellStatistikk();
        sjekkAtSykefraværshistorikkReturnereRiktigObjekt(jwtToken);
    }



    @Test
    public void hentAgreggertStatistikk_skalFunke() throws Exception {
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

        opprettStatistikkForLand(jdbcTemplate);
        opprettStatistikkForNæring2Siffer(
              jdbcTemplate,
              new Næring("10", "Produksjon av nærings- og nytelsesmidler"),
              SISTE_ÅRSTALL, SISTE_KVARTAL,
              5,
              100,
              10
        );
        opprettStatistikkForNæring2Siffer(
              jdbcTemplate,
              new Næring("10", "Produksjon av nærings- og nytelsesmidler"),
              SISTE_ÅRSTALL - 1, SISTE_KVARTAL,
              8,
              100,
              10
        );
        opprettStatistikkForVirksomhet(
              jdbcTemplate,
              ORGNR_UNDERENHET,
              SISTE_ÅRSTALL, SISTE_KVARTAL,
              6,
              100,
              10
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
        JsonNode prosentSiste4KvartalerVirksomhet = responseBody.get("prosentSiste4Kvartaler").get(0);
        JsonNode prosentSiste4KvartalerNæring = responseBody.get("prosentSiste4Kvartaler").get(1);
        JsonNode prosentSiste4KvartalerLand = responseBody.get("prosentSiste4Kvartaler").get(2);
        JsonNode trendNæring = responseBody.get("trend").get(0);

        assertThat(prosentSiste4KvartalerVirksomhet.get("label").textValue())
              .isEqualTo("NAV ARBEID OG YTELSER AVD OSLO");
        assertThat(prosentSiste4KvartalerNæring.get("label").textValue())
              .isEqualTo("Næring(kode=10, navn=Produksjon av nærings- og nytelsesmidler)");
        assertThat(prosentSiste4KvartalerLand.get("label").textValue())
              .isEqualTo("Norge");
        assertThat(trendNæring.get("label").textValue())
              .isEqualTo("Næring(kode=10, navn=Produksjon av nærings- og nytelsesmidler)");
    }

    @Test
    public void sykefraværshistorikk__skal_returnere_riktig_objekt_med_en_token_fra_tokenx_og_opprinnelig_provider_er_loginservice() throws Exception {
        String jwtToken = TestTokenUtil.createToken(
              mockOAuth2Server,
              "15008462396",
              TOKENX_ISSUER_ID,
              "https://navnob2c.b2clogin.com/something-unique-and-long/v2.0/"
        );

        opprettGenerellStatistikk();

        sjekkAtSykefraværshistorikkReturnereRiktigObjekt(jwtToken);
    }

    private void opprettGenerellStatistikk() {
        opprettStatistikkForLand(jdbcTemplate);
        opprettStatistikkForSektor(jdbcTemplate);
        opprettStatistikkForNæring2Siffer(jdbcTemplate, PRODUKSJON, SISTE_ÅRSTALL, SISTE_KVARTAL, 5, 100, 10);
        opprettStatistikkForVirksomhet(jdbcTemplate, ORGNR_UNDERENHET, SISTE_ÅRSTALL, SISTE_KVARTAL, 9, 200, 10);
        opprettStatistikkForVirksomhet(jdbcTemplate, ORGNR_OVERORDNET_ENHET, SISTE_ÅRSTALL, SISTE_KVARTAL, 7, 200, 10);
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
        assertThat(alleSykefraværshistorikk.get(0).get("kvartalsvisSykefraværsprosent")).contains(
              objectMapper.readTree(
                    "{\"prosent\":4.0,\"tapteDagsverk\":4.0,\"muligeDagsverk\":100.0,\"erMaskert\":false,\"årstall\":" + SISTE_ÅRSTALL + ",\"kvartal\":" + SISTE_KVARTAL + "}"
              ));
        assertThat(alleSykefraværshistorikk.get(1).get("label")).isEqualTo(objectMapper.readTree("\"Statlig forvaltning\""));
        assertThat(alleSykefraværshistorikk.get(1).get("kvartalsvisSykefraværsprosent").get(0))
              .isEqualTo(objectMapper.readTree(
                          "{\"prosent\":4.9,\"tapteDagsverk\":657853.3,\"muligeDagsverk\":1.35587109E7,\"erMaskert\":false,\"årstall\":" + SISTE_ÅRSTALL + ",\"kvartal\":" + SISTE_KVARTAL + "}"
                    )
              );
        assertThat(alleSykefraværshistorikk.get(2).get("label")).isEqualTo(objectMapper.readTree("\"Produksjon av nærings- og nytelsesmidler\""));
        assertThat(alleSykefraværshistorikk.get(2).get("kvartalsvisSykefraværsprosent").get(0))
              .isEqualTo(objectMapper.readTree(
                          "{\"prosent\":5.0,\"tapteDagsverk\":5.0,\"muligeDagsverk\":100.0,\"erMaskert\":false,\"årstall\":" + SISTE_ÅRSTALL + ",\"kvartal\":" + SISTE_KVARTAL + "}"
                    )
              );
        assertThat(alleSykefraværshistorikk.get(3).get("label")).isEqualTo(objectMapper.readTree("\"NAV ARBEID OG YTELSER AVD OSLO\""));
        assertThat(alleSykefraværshistorikk.get(3).get("kvartalsvisSykefraværsprosent").get(0))
              .isEqualTo(objectMapper.readTree(
                          "{\"prosent\":4.5,\"tapteDagsverk\":9.0,\"muligeDagsverk\":200.0,\"erMaskert\":false,\"årstall\":" + SISTE_ÅRSTALL + ",\"kvartal\":" + SISTE_KVARTAL + "}"
                    )
              );
        assertThat(alleSykefraværshistorikk.get(4).get("label")).isEqualTo(objectMapper.readTree("\"NAV ARBEID OG YTELSER\""));
        assertThat(alleSykefraværshistorikk.get(4).get("kvartalsvisSykefraværsprosent").get(0))
              .isEqualTo(objectMapper.readTree(
                          "{\"prosent\":3.5,\"tapteDagsverk\":7.0,\"muligeDagsverk\":200.0,\"erMaskert\":false,\"årstall\":" + SISTE_ÅRSTALL + ",\"kvartal\":" + SISTE_KVARTAL + "}"
                    )
              );

    }

    @Test
    public void sykefraværshistorikk__skal_IKKE_godkjenne_en_token_uten_sub_eller_pid() throws Exception {
        String jwtTokenIssuedByLoginservice = TestTokenUtil.createToken(
              mockOAuth2Server,
              "",
              "",
              SELVBETJENING_ISSUER_ID,
              ""
        );

        HttpResponse<String> response = newBuilder().build().send(
              HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/"
                          + ORGNR_UNDERENHET_INGEN_TILGANG + "/sykefravarshistorikk/kvartalsvis"))
                    .header(
                          AUTHORIZATION,
                          jwtTokenIssuedByLoginservice
                    )
                    .GET()
                    .build(),
              ofString()
        );
        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body()).isEqualTo("{\"message\":\"You are not authorized to access this ressource\"}");
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
        opprettStatistikkForVirksomhet(
              jdbcTemplate,
              ORGNR_UNDERENHET,
              SISTE_ÅRSTALL, SISTE_KVARTAL,
              12,
              100,
              10);

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
        assertThat(responseBody).isNotEmpty();
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
    private String getBearerMedJwt(String fnr) {
        return "Bearer "
              + TestTokenUtil.createToken(
              mockOAuth2Server,
              fnr,
              SELVBETJENING_ISSUER_ID,
              ""
        );
    }

}
