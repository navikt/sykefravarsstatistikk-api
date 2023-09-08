package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.SpringIntegrationTestbase;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Statistikkategori;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.stream.Collectors;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static java.net.http.HttpClient.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestTokenUtil.TOKENX_ISSUER_ID;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ApiEndpointsIntegrationTest extends SpringIntegrationTestbase {

    private final int SISTE_ÅRSTALL = SISTE_PUBLISERTE_KVARTAL.getÅrstall();
    private final int SISTE_KVARTAL = SISTE_PUBLISERTE_KVARTAL.getKvartal();

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
    public void sykefraværshistorikk__skal_ikke_tillate_selvbetjening_token()
            throws Exception {
        String jwtTokenIssuedByLoginservice =
                TestTokenUtil.createToken(mockOAuth2Server, "15008462396", "selvbetjening", "");
        opprettGenerellStatistikk();
        HttpResponse<String> respons = gjørKallMotKvartalsvis(ORGNR_UNDERENHET, jwtTokenIssuedByLoginservice);
        assertThat(respons.statusCode()).isEqualTo(401);
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
        HttpResponse<String> response = gjørKallMotKvartalsvis(ORGNR_UNDERENHET, "Bearer " + jwtToken);

        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode alleSykefraværshistorikk = objectMapper.readTree(response.body());

        assertThat(
                alleSykefraværshistorikk.findValues("type").stream()
                        .map(JsonNode::textValue)
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
    public void sykefraværshistorikk_sektor__skal_utføre_tilgangskontroll()
            throws IOException, InterruptedException {
        HttpResponse<String> response = gjørKallMotKvartalsvis(ORGNR_UNDERENHET_INGEN_TILGANG, getBearerMedJwt());
        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.body())
                .isEqualTo("{\"message\":\"You don't have access to this resource\"}");
    }

    @Test
    public void sykefraværshistorikk__skal_IKKE_godkjenne_en_token_uten_sub_eller_pid()
            throws Exception {
        String jwtToken =
                TestTokenUtil.createToken(mockOAuth2Server, "", "", TOKENX_ISSUER_ID, "");

        HttpResponse<String> response = gjørKallMotKvartalsvis(ORGNR_UNDERENHET_INGEN_TILGANG, jwtToken);
        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body())
                .isEqualTo("{\"message\":\"You are not authorized to access this resource\"}");
    }

    private HttpResponse<String> gjørKallMotKvartalsvis(String orgnr, String jwtToken) throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(
                        URI.create(
                                "http://localhost:"
                                        + port
                                        + "/sykefravarsstatistikk-api/"
                                        + orgnr
                                        + "/sykefravarshistorikk/kvartalsvis"))
                .header(AUTHORIZATION, jwtToken)
                .GET()
                .build();

        return newBuilder()
                .build()
                .send(httpRequest, ofString());
    }

    private String getBearerMedJwt() {
        return "Bearer "
                + TestTokenUtil.createToken(mockOAuth2Server, "15008462396", TOKENX_ISSUER_ID, "");
    }
}
