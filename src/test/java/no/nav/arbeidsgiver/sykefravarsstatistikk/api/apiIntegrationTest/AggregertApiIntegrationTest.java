package no.nav.arbeidsgiver.sykefravarsstatistikk.api.apiIntegrationTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.SpringIntegrationTestbase;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestTokenUtil;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static java.net.http.HttpClient.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.GraderingTestUtils.insertDataMedGradering;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.*;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


public class AggregertApiIntegrationTest extends SpringIntegrationTestbase {

    private final static String ORGNR_UNDERENHET = "910969439";
    private final static String ORGNR_UNDERENHET_UTEN_IA_RETTIGHETER = "910825518";
    private final static String ORGNR_UNDERENHET_INGEN_TILGANG = "777777777";
    /*private final Underenhet enProduksjonVirksomhetsNæring = Underenhet.builder()
          .orgnr(new Orgnr(ORGNR_UNDERENHET))
          .navn("En Produksjon VirksomhetsNæring")
          .næringskode(new Næringskode5Siffer("10258", PRODUKSJON_NYTELSESMIDLER.getNavn()))
          .antallAnsatte(10)
          .overordnetEnhetOrgnr(new Orgnr("1111111111")).build();
*/
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    MockOAuth2Server mockOAuth2Server;

    @Mock
    private EnhetsregisteretClient mockEnhetsregisteretClient;

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
        assertThat(responseBody.get("gradertProsentSiste4Kvartaler")).isEmpty();
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

        insertDataMedGradering(
              jdbcTemplate,
              ORGNR_UNDERENHET,
              "10", "10300",
              RECTYPE_FOR_VIRKSOMHET, new ÅrstallOgKvartal(2022,1),
              5,
              9,
              7,
              new BigDecimal(10),
              new BigDecimal(20),
              new BigDecimal(100)
        );
        insertDataMedGradering(
              jdbcTemplate,
              ORGNR_UNDERENHET,
              "10", "10300",
              RECTYPE_FOR_VIRKSOMHET, new ÅrstallOgKvartal(2021,4),
              2,
              9,
              7,
              new BigDecimal(12),
              new BigDecimal(20),
              new BigDecimal(100)
        );
        insertDataMedGradering(
              jdbcTemplate,
              ORGNR_UNDERENHET,
              "10", "10300",
              RECTYPE_FOR_VIRKSOMHET, new ÅrstallOgKvartal(2021,3),
              19,
              30,
              15,
              new BigDecimal(25),
              new BigDecimal(50),
              new BigDecimal(300)
        );

        //when(mockEnhetsregisteretClient.hentInformasjonOmUnderenhet(any())).thenReturn(enProduksjonVirksomhetsNæring);
        HttpResponse<String> response = utførAutorisertKall(ORGNR_UNDERENHET);
        assertThat(response.statusCode()).isEqualTo(200);

        JsonNode responseBody = objectMapper.readTree(response.body());
        JsonNode prosentSiste4Kvartaler = responseBody.get("prosentSiste4Kvartaler");

        JsonNode gradertProsentSiste4Kvartaler = responseBody.get("gradertProsentSiste4Kvartaler");

        assertThat(responseBody.get("trend").findValuesAsText("statistikkategori"))
              .containsExactly(NÆRING.toString());

        assertThat(prosentSiste4Kvartaler.findValuesAsText("statistikkategori"))
              .containsExactlyInAnyOrderElementsOf(
                    List.of(
                          VIRKSOMHET.toString(),
                          NÆRING.toString(),
                          LAND.toString()
                    ));
        assertThat(gradertProsentSiste4Kvartaler.findValuesAsText("statistikkategori"))
              .containsExactlyInAnyOrderElementsOf(
                    List.of(
                          VIRKSOMHET.toString(),
                          BRANSJE.toString()
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


    @Test
    public void hentAgreggertStatistikk_viserNavnetTilBransjenEllerNæringenSomLabel()
          throws Exception {
        opprettStatistikkForNæring5Siffer(jdbcTemplate, BARNEHAGER, 2022, 1, 5, 100, 10);

        HttpResponse<String> response = utførAutorisertKall(ORGNR_UNDERENHET_UTEN_IA_RETTIGHETER);
        JsonNode responseBody = objectMapper.readTree(response.body());

        JsonNode barnehageJson = responseBody.get("prosentSiste4Kvartaler").get(0);
        assertThat(barnehageJson.get("label").toString()).isEqualTo("\"Barnehager\"");
    }


    private HttpResponse<String> utførAutorisertKall(String orgnr)
          throws IOException, InterruptedException {
        String jwtToken = TestTokenUtil.createMockIdportenTokenXToken(mockOAuth2Server);
        return newBuilder().build().send(
              HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/"
                          + orgnr +
                          "/v1/sykefravarshistorikk/aggregert")
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
