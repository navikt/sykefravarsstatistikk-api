package no.nav.arbeidsgiver.sykefravarsstatistikk.api.apiIntegrationTest;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static java.net.http.HttpClient.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.PRODUKSJON_NYTELSESMIDLER;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.opprettStatistikkForLand;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.opprettStatistikkForNæring2Siffer;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.opprettStatistikkForNæring5Siffer;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.opprettStatistikkForVirksomhet;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAllStatistikkFraDatabase;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.BRANSJE;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.LAND;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.NÆRING;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.VIRKSOMHET;
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
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;


public class AggregertApiIntegrationTest extends SpringIntegrationTestbase {

    private final static String ORGNR_UNDERENHET = "910969439";
    private final static String ORGNR_UNDERENHET_UTEN_IA_RETTIGHETER = "910825518";
    private final static String ORGNR_UNDERENHET_INGEN_TILGANG = "777777777";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    MockOAuth2Server mockOAuth2Server;

    @BeforeEach
    public void setUp() {
        slettAllStatistikkFraDatabase(jdbcTemplate);
    }

    @AfterEach
    public void tearDown() {
        slettAllStatistikkFraDatabase(jdbcTemplate);
    }

    @LocalServerPort
    private String port;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Næringskode5Siffer BARNEHAGER = new Næringskode5Siffer("88911", "Barnehager");


    @Test
    public void hentAgreggertStatistikk_skalReturnere403NaarBrukerIkkeRepresentererBedriften()
            throws Exception {
        HttpResponse<String> response = utførAutorisertKall(ORGNR_UNDERENHET_INGEN_TILGANG);

        assertThat(response.statusCode()).isEqualTo(403);
    }


    @Test
    public void hentAgreggertStatistikk_skalReturnereStatus200SelvOmDetIkkeFinnesData()
            throws Exception {
        HttpResponse<String> response = utførAutorisertKall(ORGNR_UNDERENHET);

        JsonNode responseBody = objectMapper.readTree(response.body());
        assertThat(responseBody.get("prosentSiste4Kvartaler")).isEmpty();
        assertThat(responseBody.get("trend")).isEmpty();

        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    public void hentAgreggertStatistikk_returnererForventedeTyperForBedriftSomHarAlleTyperData()
            throws Exception {
        opprettStatistikkForLand(jdbcTemplate);
        opprettStatistikkForNæring2Siffer(jdbcTemplate, PRODUKSJON_NYTELSESMIDLER, 2022, 1, 5, 100,
                10);
        opprettStatistikkForNæring2Siffer(jdbcTemplate, PRODUKSJON_NYTELSESMIDLER, 2021, 1, 20, 100,
                10);
        opprettStatistikkForVirksomhet(jdbcTemplate, ORGNR_UNDERENHET, 2022, 1, 5, 100, 10);

        HttpResponse<String> response = utførAutorisertKall(ORGNR_UNDERENHET);
        assertThat(response.statusCode()).isEqualTo(200);

        JsonNode responseBody = objectMapper.readTree(response.body());
        JsonNode prosentSiste4Kvartaler = responseBody.get("prosentSiste4Kvartaler");

        assertThat(responseBody.get("trend").findValuesAsText("statistikkategori"))
                .containsExactly(NÆRING.toString());

        assertThat(prosentSiste4Kvartaler.findValuesAsText("statistikkategori"))
                .containsExactlyInAnyOrderElementsOf(
                        List.of(
                                VIRKSOMHET.toString(),
                                NÆRING.toString(),
                                LAND.toString()
                        ));

        assertThat(prosentSiste4Kvartaler.get(0).get("label").textValue())
                .isEqualTo("NAV ARBEID OG YTELSER AVD OSLO");
    }

    @Test
    public void hentAgreggertStatistikk_returnererIkkeVirksomhetstatistikkTilBrukerSomManglerIaRettigheter()
            throws Exception {
        opprettStatistikkForNæring5Siffer(jdbcTemplate, BARNEHAGER, 2022, 1, 5, 100, 10);
        opprettStatistikkForNæring5Siffer(jdbcTemplate, BARNEHAGER, 2021, 1, 1, 100, 10);
        opprettStatistikkForLand(jdbcTemplate);

        HttpResponse<String> response = utførAutorisertKall(ORGNR_UNDERENHET_UTEN_IA_RETTIGHETER);
        assertThat(response.statusCode()).isEqualTo(200);

        JsonNode responseBody = objectMapper.readTree(response.body());

        assertThat(responseBody.get("prosentSiste4Kvartaler").findValuesAsText("statistikkategori"))
                .containsExactlyInAnyOrderElementsOf(
                        List.of(
                                BRANSJE.toString(),
                                LAND.toString()
                        ));

        assertThat(responseBody.get("trend").findValuesAsText("statistikkategori"))
                .containsExactly(BRANSJE.toString());
    }

    private HttpResponse<String> utførAutorisertKall(String orgnr)
            throws IOException, InterruptedException {
        String jwtToken = TestTokenUtil.createMockIdportenTokenXToken(mockOAuth2Server);
        return newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/"
                                + orgnr +
                                "/sykefravarshistorikk/aggregert/v1'")
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