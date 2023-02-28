package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static java.net.http.HttpClient.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestTokenUtil.SELVBETJENING_ISSUER_ID;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestTokenUtil.TOKENX_ISSUER_ID;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.PRODUKSJON_NYTELSESMIDLER;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.SISTE_PUBLISERTE_KVARTAL;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.opprettStatistikkForLand;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.opprettStatistikkForNæring;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.opprettStatistikkForSektor;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.opprettStatistikkForVirksomhet;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.skrivSisteImporttidspunktTilDb;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAllStatistikkFraDatabase;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAlleImporttidspunkt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.SpringIntegrationTestbase;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.context.WebApplicationContext;

public class ApiEndpointsIntegrationTest extends SpringIntegrationTestbase {

  private final int SISTE_ÅRSTALL = SISTE_PUBLISERTE_KVARTAL.getÅrstall();
  private final int SISTE_KVARTAL = SISTE_PUBLISERTE_KVARTAL.getKvartal();

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Autowired
  MockOAuth2Server mockOAuth2Server;

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  @LocalServerPort
  private String port;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private static final String ORGNR_UNDERENHET = "910969439";
  private static final String ORGNR_OVERORDNET_ENHET = "999263550";
  private static final String ORGNR_UNDERENHET_INGEN_TILGANG = "777777777";

  @BeforeEach
  public void setUp() {
    slettAllStatistikkFraDatabase(jdbcTemplate);
    slettAlleImporttidspunkt(jdbcTemplate);
    skrivSisteImporttidspunktTilDb(jdbcTemplate);
  }

  @Test
  public void sykefraværshistorikk__skal_returnere_riktig_objekt_med_en_selvbetjening_token()
      throws Exception {
    String jwtTokenIssuedByLoginservice =
        TestTokenUtil.createToken(mockOAuth2Server, "15008462396", SELVBETJENING_ISSUER_ID, "");
    opprettGenerellStatistikk();
    sjekkAtSykefraværshistorikkReturnereRiktigObjekt(jwtTokenIssuedByLoginservice);
  }

  @Test
  public void
  sykefraværshistorikk__skal_returnere_riktig_objekt_med_en_selvbetjening_token__issued_med_sub()
      throws Exception {
    String jwtTokenIssuedByLoginservice =
        TestTokenUtil.createToken(mockOAuth2Server, "", "15008462396", SELVBETJENING_ISSUER_ID, "");
    opprettGenerellStatistikk();
    sjekkAtSykefraværshistorikkReturnereRiktigObjekt(jwtTokenIssuedByLoginservice);
  }

  @Test
  public void
  sykefraværshistorikk__skal_returnere_riktig_objekt_med_en_token_fra_tokenx_og_opprinnelig_provider_er_idporten()
      throws Exception {
    String jwtToken =
        TestTokenUtil.createToken(
            mockOAuth2Server,
            "15008462396",
            TOKENX_ISSUER_ID,
            "https://oidc.difi.no/idporten-oidc-provider/");
    opprettGenerellStatistikk();
    sjekkAtSykefraværshistorikkReturnereRiktigObjekt(jwtToken);
  }

  @Test
  public void
  sykefraværshistorikk__skal_returnere_riktig_objekt_med_en_token_fra_tokenx_og_opprinnelig_provider_er_loginservice()
      throws Exception {
    String jwtToken =
        TestTokenUtil.createToken(
            mockOAuth2Server,
            "15008462396",
            TOKENX_ISSUER_ID,
            "https://navnob2c.b2clogin.com/something-unique-and-long/v2.0/");

    opprettGenerellStatistikk();

    sjekkAtSykefraværshistorikkReturnereRiktigObjekt(jwtToken);
  }

  private void opprettGenerellStatistikk() {
    opprettStatistikkForLand(jdbcTemplate);
    opprettStatistikkForSektor(jdbcTemplate);
    opprettStatistikkForNæring(
        jdbcTemplate, PRODUKSJON_NYTELSESMIDLER, SISTE_ÅRSTALL, SISTE_KVARTAL, 5, 100, 10);
    opprettStatistikkForVirksomhet(
        jdbcTemplate, ORGNR_UNDERENHET, SISTE_ÅRSTALL, SISTE_KVARTAL, 9, 200, 10);
    opprettStatistikkForVirksomhet(
        jdbcTemplate, ORGNR_OVERORDNET_ENHET, SISTE_ÅRSTALL, SISTE_KVARTAL, 7, 200, 10);
  }

  private void sjekkAtSykefraværshistorikkReturnereRiktigObjekt(String jwtToken) throws Exception {
    HttpResponse<String> response =
        newBuilder()
            .build()
            .send(
                HttpRequest.newBuilder()
                    .uri(
                        URI.create(
                            "http://localhost:"
                                + port
                                + "/sykefravarsstatistikk-api/"
                                + ORGNR_UNDERENHET
                                + "/sykefravarshistorikk/kvartalsvis"))
                    .header(AUTHORIZATION, "Bearer " + jwtToken)
                    .GET()
                    .build(),
                ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    JsonNode alleSykefraværshistorikk = objectMapper.readTree(response.body());

    assertThat(
        alleSykefraværshistorikk.findValues("type").stream()
            .map(v -> v.textValue())
            .collect(Collectors.toList()))
        .containsExactlyInAnyOrderElementsOf(
            Arrays.asList(
                Statistikkategori.LAND.toString(),
                Statistikkategori.SEKTOR.toString(),
                Statistikkategori.NÆRING.toString(),
                Statistikkategori.VIRKSOMHET.toString(),
                Statistikkategori.OVERORDNET_ENHET.toString()));

    assertThat(alleSykefraværshistorikk.get(0).get("label"))
        .isEqualTo(objectMapper.readTree("\"Norge\""));
    assertThat(alleSykefraværshistorikk.get(0).get("kvartalsvisSykefraværsprosent"))
        .contains(
            objectMapper.readTree(
                "{\"prosent\":4.0,\"tapteDagsverk\":4.0,\"muligeDagsverk\":100.0,"
                    + "\"erMaskert\":false,\"årstall\":"
                    + SISTE_ÅRSTALL
                    + ",\"kvartal\":"
                    + SISTE_KVARTAL
                    + "}"));
    assertThat(alleSykefraværshistorikk.get(1).get("label"))
        .isEqualTo(objectMapper.readTree("\"Statlig forvaltning\""));
    assertThat(alleSykefraværshistorikk.get(1).get("kvartalsvisSykefraværsprosent").get(0))
        .isEqualTo(
            objectMapper.readTree(
                "{\"prosent\":4.9,\"tapteDagsverk\":657853.3,\"muligeDagsverk\":1"
                    + ".35587109E7,\"erMaskert\":false,\"årstall\":"
                    + SISTE_ÅRSTALL
                    + ",\"kvartal\":"
                    + SISTE_KVARTAL
                    + "}"));
    assertThat(alleSykefraværshistorikk.get(2).get("label"))
        .isEqualTo(objectMapper.readTree("\"Produksjon av nærings- og nytelsesmidler\""));
    assertThat(alleSykefraværshistorikk.get(2).get("kvartalsvisSykefraværsprosent").get(0))
        .isEqualTo(
            objectMapper.readTree(
                "{\"prosent\":5.0,\"tapteDagsverk\":5.0,\"muligeDagsverk\":100.0,"
                    + "\"erMaskert\":false,\"årstall\":"
                    + SISTE_ÅRSTALL
                    + ",\"kvartal\":"
                    + SISTE_KVARTAL
                    + "}"));
    assertThat(alleSykefraværshistorikk.get(3).get("label"))
        .isEqualTo(objectMapper.readTree("\"NAV ARBEID OG YTELSER AVD OSLO\""));
    assertThat(alleSykefraværshistorikk.get(3).get("kvartalsvisSykefraværsprosent").get(0))
        .isEqualTo(
            objectMapper.readTree(
                "{\"prosent\":4.5,\"tapteDagsverk\":9.0,\"muligeDagsverk\":200.0,"
                    + "\"erMaskert\":false,\"årstall\":"
                    + SISTE_ÅRSTALL
                    + ",\"kvartal\":"
                    + SISTE_KVARTAL
                    + "}"));
    assertThat(alleSykefraværshistorikk.get(4).get("label"))
        .isEqualTo(objectMapper.readTree("\"NAV ARBEID OG YTELSER\""));
    assertThat(alleSykefraværshistorikk.get(4).get("kvartalsvisSykefraværsprosent").get(0))
        .isEqualTo(
            objectMapper.readTree(
                "{\"prosent\":3.5,\"tapteDagsverk\":7.0,\"muligeDagsverk\":200.0,"
                    + "\"erMaskert\":false,\"årstall\":"
                    + SISTE_ÅRSTALL
                    + ",\"kvartal\":"
                    + SISTE_KVARTAL
                    + "}"));
  }

  @Test
  public void sykefraværshistorikk__skal_IKKE_godkjenne_en_token_uten_sub_eller_pid()
      throws Exception {
    String jwtTokenIssuedByLoginservice =
        TestTokenUtil.createToken(mockOAuth2Server, "", "", SELVBETJENING_ISSUER_ID, "");

    HttpResponse<String> response =
        newBuilder()
            .build()
            .send(
                HttpRequest.newBuilder()
                    .uri(
                        URI.create(
                            "http://localhost:"
                                + port
                                + "/sykefravarsstatistikk-api/"
                                + ORGNR_UNDERENHET_INGEN_TILGANG
                                + "/sykefravarshistorikk/kvartalsvis"))
                    .header(AUTHORIZATION, jwtTokenIssuedByLoginservice)
                    .GET()
                    .build(),
                ofString());
    assertThat(response.statusCode()).isEqualTo(401);
    assertThat(response.body())
        .isEqualTo("{\"message\":\"You are not authorized to access this resource\"}");
  }

  @Test
  public void sykefraværshistorikk_sektor__skal_utføre_tilgangskontroll()
      throws IOException, InterruptedException {
    HttpResponse<String> response =
        newBuilder()
            .build()
            .send(
                HttpRequest.newBuilder()
                    .uri(
                        URI.create(
                            "http://localhost:"
                                + port
                                + "/sykefravarsstatistikk-api/"
                                + ORGNR_UNDERENHET_INGEN_TILGANG
                                + "/sykefravarshistorikk/kvartalsvis"))
                    .header(AUTHORIZATION, getBearerMedJwt())
                    .GET()
                    .build(),
                ofString());
    assertThat(response.statusCode()).isEqualTo(403);
    assertThat(response.body())
        .isEqualTo("{\"message\":\"You don't have access to this resource\"}");
  }

  @Test
  public void summert_sykefraværshistorikk_siste_4_kvartaler__skal_utføre_tilgangskontroll()
      throws IOException, InterruptedException {
    HttpResponse<String> response =
        newBuilder()
            .build()
            .send(
                HttpRequest.newBuilder()
                    .uri(
                        URI.create(
                            "http://localhost:"
                                + port
                                + "/sykefravarsstatistikk-api/"
                                + ORGNR_UNDERENHET_INGEN_TILGANG
                                + "/sykefravarshistorikk/summert?antallKvartaler=4"))
                    .header(AUTHORIZATION, getBearerMedJwt())
                    .GET()
                    .build(),
                ofString());
    assertThat(response.statusCode()).isEqualTo(403);
    assertThat(response.body())
        .isEqualTo("{\"message\":\"You don't have access to this resource\"}");
  }

  @Test
  public void legemeldtSykefraværsprosent__skal_returnere_riktig_objekt() throws Exception {
    opprettStatistikkForVirksomhet(
        jdbcTemplate, ORGNR_UNDERENHET, SISTE_ÅRSTALL, SISTE_KVARTAL, 12, 100, 10);

    HttpResponse<String> response =
        newBuilder()
            .build()
            .send(
                HttpRequest.newBuilder()
                    .uri(
                        URI.create(
                            "http://localhost:"
                                + port
                                + "/sykefravarsstatistikk-api/"
                                + ORGNR_UNDERENHET
                                + "/sykefravarshistorikk/legemeldtsykefravarsprosent"))
                    .header(AUTHORIZATION, getBearerMedJwt())
                    .GET()
                    .build(),
                ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    JsonNode alleSykefraværshistorikk = objectMapper.readTree(response.body());

    assertTrue(
        Stream.of(
                Statistikkategori.VIRKSOMHET.toString(),
                Statistikkategori.BRANSJE.toString(),
                Statistikkategori.NÆRING.toString())
            .anyMatch(alleSykefraværshistorikk.findValue("type").asText()::contains));
    assertTrue(alleSykefraværshistorikk.findValue("label").isTextual());
    assertTrue(alleSykefraværshistorikk.findValue("prosent").isNumber());
  }

  @Test
  public void legemeldt_sykefraværsprosent_siste_4_kvartaler__skal_utføre_tilgangskontroll()
      throws IOException, InterruptedException {
    HttpResponse<String> response =
        newBuilder()
            .build()
            .send(
                HttpRequest.newBuilder()
                    .uri(
                        URI.create(
                            "http://localhost:"
                                + port
                                + "/sykefravarsstatistikk-api/"
                                + ORGNR_UNDERENHET_INGEN_TILGANG
                                + "/sykefravarshistorikk/legemeldtsykefravarsprosent"))
                    .header(AUTHORIZATION, getBearerMedJwt())
                    .GET()
                    .build(),
                ofString());
    assertThat(response.statusCode()).isEqualTo(403);
    assertThat(response.body())
        .isEqualTo("{\"message\":\"You don't have access to this resource\"}");
  }

  @Test
  public void summert_sykefraværshistorikk_siste_4_kvartaler__skal_returnere_riktig_objekt()
      throws Exception {
    HttpResponse<String> response =
        newBuilder()
            .build()
            .send(
                HttpRequest.newBuilder()
                    .uri(
                        URI.create(
                            "http://localhost:"
                                + port
                                + "/sykefravarsstatistikk-api/"
                                + ORGNR_UNDERENHET
                                + "/sykefravarshistorikk/summert?antallKvartaler=4"))
                    .header(AUTHORIZATION, getBearerMedJwt())
                    .GET()
                    .build(),
                ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    JsonNode responseBody = objectMapper.readTree(response.body());
    assertThat(responseBody).isNotEmpty();
  }

  @Test
  public void legemeldtSykefraværsprosent__når_virksomhet_ikke_har_næring_returnerer_204()
      throws Exception {
    HttpResponse<String> response =
        newBuilder()
            .build()
            .send(
                HttpRequest.newBuilder()
                    .uri(
                        URI.create(
                            "http://localhost:"
                                + port
                                + "/sykefravarsstatistikk-api/"
                                + "555555555"
                                + "/sykefravarshistorikk/legemeldtsykefravarsprosent"))
                    .header(AUTHORIZATION, getBearerMedJwt())
                    .GET()
                    .build(),
                ofString());

    assertThat(response.statusCode()).isEqualTo(204);
    JsonNode body = objectMapper.readTree(response.body());

    assertThat(body).isNullOrEmpty();
  }

  @NotNull
  private String getBearerMedJwt() {
    return "Bearer "
        + TestTokenUtil.createToken(mockOAuth2Server, "15008462396", SELVBETJENING_ISSUER_ID, "");
  }
}
