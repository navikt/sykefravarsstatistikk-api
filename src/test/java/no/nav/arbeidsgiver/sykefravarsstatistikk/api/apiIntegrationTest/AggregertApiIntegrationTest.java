package no.nav.arbeidsgiver.sykefravarsstatistikk.api.apiIntegrationTest;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static java.net.http.HttpClient.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.GraderingTestUtils.insertDataMedGradering;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.PRODUKSJON_NYTELSESMIDLER;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.SISTE_PUBLISERTE_KVARTAL;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.opprettStatistikkForLand;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.opprettStatistikkForNæring;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.opprettStatistikkForNæring5Siffer;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.opprettStatistikkForVirksomhet;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.skrivSisteImporttidspunktTilDb;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAllStatistikkFraDatabase;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAlleImporttidspunkt;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Statistikkategori.BRANSJE;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Statistikkategori.LAND;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Statistikkategori.VIRKSOMHET;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.SpringIntegrationTestbase;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestTokenUtil;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.VarighetTestUtils;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.BedreNæringskode;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Varighetskategori;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class AggregertApiIntegrationTest extends SpringIntegrationTestbase {

  private static final String ORGNR_UNDERENHET = "910969439";
  private static final String ORGNR_UNDERENHET_UTEN_IA_RETTIGHETER = "910825518";
  private static final String ORGNR_UNDERENHET_INGEN_TILGANG = "777777777";

  @Autowired private NamedParameterJdbcTemplate jdbcTemplate;

  @Autowired MockOAuth2Server mockOAuth2Server;

  @BeforeEach
  public void setUp() {
    slettAllStatistikkFraDatabase(jdbcTemplate);
    slettAlleImporttidspunkt(jdbcTemplate);
    skrivSisteImporttidspunktTilDb(jdbcTemplate);
  }

  @AfterEach
  public void tearDown() {
    slettAllStatistikkFraDatabase(jdbcTemplate);
    slettAlleImporttidspunkt(jdbcTemplate);
  }

  @LocalServerPort private String port;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final BedreNæringskode BARNEHAGER = new BedreNæringskode("88911");

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

    assertThat(response.statusCode()).isEqualTo(200);

    JsonNode responseBody = objectMapper.readTree(response.body());
    assertThat(responseBody.get("prosentSiste4KvartalerTotalt")).isEmpty();
    assertThat(responseBody.get("prosentSiste4KvartalerGradert")).isEmpty();
    assertThat(responseBody.get("trendTotalt")).isEmpty();
    assertThat(responseBody.get("prosentSiste4KvartalerKorttid")).isEmpty();
    assertThat(responseBody.get("prosentSiste4KvartalerLangtid")).isEmpty();
    assertThat(responseBody.get("trendTotalt")).isEmpty();
  }

  @Test
  public void hentAgreggertStatistikk_returnererForventedeTyperForBedriftSomHarAlleTyperData()
      throws Exception {
    ÅrstallOgKvartal ettÅrSiden = SISTE_PUBLISERTE_KVARTAL.minusEttÅr();
    opprettStatistikkForLand(jdbcTemplate);
    opprettStatistikkForNæring(
        jdbcTemplate,
        PRODUKSJON_NYTELSESMIDLER,
        SISTE_PUBLISERTE_KVARTAL.getÅrstall(),
        SISTE_PUBLISERTE_KVARTAL.getKvartal(),
        5,
        100,
        10);
    opprettStatistikkForNæring(
        jdbcTemplate,
        PRODUKSJON_NYTELSESMIDLER,
        ettÅrSiden.getÅrstall(),
        ettÅrSiden.getKvartal(),
        20,
        100,
        10);
    opprettStatistikkForVirksomhet(
        jdbcTemplate,
        ORGNR_UNDERENHET,
        SISTE_PUBLISERTE_KVARTAL.getÅrstall(),
        SISTE_PUBLISERTE_KVARTAL.getKvartal(),
        5,
        100,
        10);

    insertDataMedGradering(
        jdbcTemplate,
        ORGNR_UNDERENHET,
        "10",
        "10300",
        RECTYPE_FOR_VIRKSOMHET,
        SISTE_PUBLISERTE_KVARTAL,
        7,
        new BigDecimal(10),
        new BigDecimal(20),
        new BigDecimal(100));
    insertDataMedGradering(
        jdbcTemplate,
        ORGNR_UNDERENHET,
        "10",
        "10300",
        RECTYPE_FOR_VIRKSOMHET,
        SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1),
        7,
        new BigDecimal(12),
        new BigDecimal(20),
        new BigDecimal(100));
    insertDataMedGradering(
        jdbcTemplate,
        ORGNR_UNDERENHET,
        "10",
        "10300",
        RECTYPE_FOR_VIRKSOMHET,
        SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2),
        15,
        new BigDecimal(25),
        new BigDecimal(50),
        new BigDecimal(300));

    HttpResponse<String> response = utførAutorisertKall(ORGNR_UNDERENHET);
    assertThat(response.statusCode()).isEqualTo(200);

    JsonNode responseBody = objectMapper.readTree(response.body());
    JsonNode prosentSiste4Kvartaler = responseBody.get("prosentSiste4KvartalerTotalt");

    JsonNode gradertProsentSiste4Kvartaler = responseBody.get("prosentSiste4KvartalerGradert");

    assertThat(responseBody.get("trendTotalt").findValuesAsText("statistikkategori"))
        .containsExactly(BRANSJE.toString());

    assertThat(prosentSiste4Kvartaler.findValuesAsText("statistikkategori"))
        .containsExactlyInAnyOrderElementsOf(
            List.of(VIRKSOMHET.toString(), BRANSJE.toString(), LAND.toString()));
    assertThat(gradertProsentSiste4Kvartaler.findValuesAsText("statistikkategori"))
        .containsExactlyInAnyOrderElementsOf(List.of(VIRKSOMHET.toString(), BRANSJE.toString()));

    assertThat(prosentSiste4Kvartaler.get(0).get("label").textValue())
        .isEqualTo("NAV ARBEID OG YTELSER AVD OSLO");
  }

  @Test
  public void hentAggregertStatistikk_returnererLangtidOgKorttidForVirksomhetOgBransje()
      throws Exception {
    VarighetTestUtils.leggTilVirksomhetsstatistikkMedVarighet(
        jdbcTemplate,
        ORGNR_UNDERENHET,
        new ÅrstallOgKvartal(2022, 1),
        Varighetskategori._1_DAG_TIL_7_DAGER,
        0,
        2,
        0);
    VarighetTestUtils.leggTilVirksomhetsstatistikkMedVarighet(
        jdbcTemplate,
        ORGNR_UNDERENHET,
        new ÅrstallOgKvartal(2022, 1),
        Varighetskategori._8_UKER_TIL_20_UKER,
        0,
        4,
        0);
    VarighetTestUtils.leggTilVirksomhetsstatistikkMedVarighet(
        jdbcTemplate,
        ORGNR_UNDERENHET,
        new ÅrstallOgKvartal(2022, 1),
        Varighetskategori.TOTAL,
        10,
        0,
        100);
    VarighetTestUtils.leggTilStatisitkkNæringMedVarighetForTotalVarighetskategori(
        jdbcTemplate, new BedreNæringskode("10300"), new ÅrstallOgKvartal(2022, 1), 10, 100);
    VarighetTestUtils.leggTilStatisitkkNæringMedVarighet(
        jdbcTemplate,
        new BedreNæringskode("10300"),
        new ÅrstallOgKvartal(2022, 1),
        Varighetskategori._1_DAG_TIL_7_DAGER,
        10);
    HttpResponse<String> response = utførAutorisertKall(ORGNR_UNDERENHET);
    assertThat(response.statusCode()).isEqualTo(200);

    JsonNode responseBody = objectMapper.readTree(response.body());
    JsonNode korttidProsentSiste4Kvartaler = responseBody.get("prosentSiste4KvartalerKorttid");
    JsonNode LangtidProsentSiste4Kvartaler = responseBody.get("prosentSiste4KvartalerLangtid");

    assertThat(korttidProsentSiste4Kvartaler.findValuesAsText("statistikkategori"))
        .containsExactlyInAnyOrderElementsOf(List.of(VIRKSOMHET.toString(), BRANSJE.toString()));
    assertThat(LangtidProsentSiste4Kvartaler.findValuesAsText("statistikkategori"))
        .containsExactlyInAnyOrderElementsOf(List.of(VIRKSOMHET.toString(), BRANSJE.toString()));
  }

  @Test
  public void
      hentAgreggertStatistikk_returnererIkkeVirksomhetstatistikkTilBrukerSomManglerIaRettigheter()
          throws Exception {
    ÅrstallOgKvartal ettÅrSiden = SISTE_PUBLISERTE_KVARTAL.minusEttÅr();
    opprettStatistikkForNæring5Siffer(
        jdbcTemplate,
        BARNEHAGER,
        SISTE_PUBLISERTE_KVARTAL.getÅrstall(),
        SISTE_PUBLISERTE_KVARTAL.getKvartal(),
        5,
        100,
        10);
    opprettStatistikkForNæring5Siffer(
        jdbcTemplate, BARNEHAGER, ettÅrSiden.getÅrstall(), ettÅrSiden.getKvartal(), 1, 100, 10);
    opprettStatistikkForLand(jdbcTemplate);

    HttpResponse<String> response = utførAutorisertKall(ORGNR_UNDERENHET_UTEN_IA_RETTIGHETER);
    JsonNode responseBody = objectMapper.readTree(response.body());

    assertThat(
            responseBody.get("prosentSiste4KvartalerTotalt").findValuesAsText("statistikkategori"))
        .containsExactlyInAnyOrderElementsOf(List.of(BRANSJE.toString(), LAND.toString()));

    assertThat(responseBody.get("trendTotalt").findValuesAsText("statistikkategori"))
        .containsExactly(BRANSJE.toString());
  }

  @Test
  public void hentAgreggertStatistikk_kræsjerIkkeDersomMuligeDagsverkErZero() throws Exception {
    opprettStatistikkForVirksomhet(
        jdbcTemplate,
        ORGNR_UNDERENHET,
        SISTE_PUBLISERTE_KVARTAL.getÅrstall(),
        SISTE_PUBLISERTE_KVARTAL.getKvartal(),
        5,
        0,
        10);

    HttpResponse<String> response = utførAutorisertKall(ORGNR_UNDERENHET);
    JsonNode responseBody = objectMapper.readTree(response.body());

    assertThat(
            responseBody.get("prosentSiste4KvartalerTotalt").findValuesAsText("statistikkategori"))
        .isEmpty();
  }

  @Test
  public void hentAgreggertStatistikk_viserNavnetTilBransjenEllerNæringenSomLabel()
      throws Exception {
    opprettStatistikkForNæring5Siffer(jdbcTemplate, BARNEHAGER, 2022, 1, 5, 100, 10);

    HttpResponse<String> response = utførAutorisertKall(ORGNR_UNDERENHET_UTEN_IA_RETTIGHETER);
    JsonNode responseBody = objectMapper.readTree(response.body());

    JsonNode barnehageJson = responseBody.get("prosentSiste4KvartalerTotalt").get(0);
    assertThat(barnehageJson.get("label").toString()).isEqualTo("\"Barnehager\"");
  }

  private HttpResponse<String> utførAutorisertKall(String orgnr)
      throws IOException, InterruptedException {
    String jwtToken = TestTokenUtil.createMockIdportenTokenXToken(mockOAuth2Server);
    return newBuilder()
        .build()
        .send(
            HttpRequest.newBuilder()
                .GET()
                .uri(
                    URI.create(
                        "http://localhost:"
                            + port
                            + "/sykefravarsstatistikk-api/"
                            + orgnr
                            + "/v1/sykefravarshistorikk/aggregert"))
                .header(AUTHORIZATION, "Bearer " + jwtToken)
                .build(),
            ofString());
  }
}
