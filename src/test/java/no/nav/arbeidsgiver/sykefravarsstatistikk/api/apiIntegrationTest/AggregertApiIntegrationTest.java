package no.nav.arbeidsgiver.sykefravarsstatistikk.api.apiIntegrationTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.SpringIntegrationTestbase;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestTokenUtil;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis.KvartalsvisSykefraværRepository;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static java.net.http.HttpClient.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.oppretteStatistikkForLand;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAllStatistikkFraDatabase;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.Aggregeringstype.*;
import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    public void hentAgreggertStatistikk_skal_returnere_403_naar_bruker_ikke_representerer_bedriften()
          throws Exception {
        String jwtToken = TestTokenUtil.createMockIdportenTokenXToken(mockOAuth2Server);

        HttpResponse<String> response = utførAutorisertKall(ORGNR_UNDERENHET_INGEN_TILGANG, jwtToken);
        assertThat(response.statusCode()).isEqualTo(403);
    }

    @Test
    public void hentAgreggertStatistikk_returnererForventedeTyperForBedriftSomHarAlleTyperData()
          throws Exception {
        String jwtToken = TestTokenUtil.createMockIdportenTokenXToken(mockOAuth2Server);

        HttpResponse<String> response = utførAutorisertKall(ORGNR_UNDERENHET, jwtToken);
        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode responseBody = objectMapper.readTree(response.body());

        assertThat(responseBody.findValuesAsText("type"))
              .containsExactlyInAnyOrderElementsOf(
                    List.of(
                          PROSENT_SISTE_4_KVARTALER_VIRKSOMHET.toString(),
                          PROSENT_SISTE_4_KVARTALER_NÆRING.toString(),
                          PROSENT_SISTE_4_KVARTALER_LAND.toString(),
                          TREND_NÆRING.toString()
                    ));
        /*assertThat(responseBody.findValuesAsText("label"))
              .containsExactlyInAnyOrderElementsOf(
                    List.of(
                          "NAV ARBEID OG YTELSER AVD OSLO",
                          "Trygdeordninger underlagt offentlig forvaltning",
                          PROSENT_SISTE_4_KVARTALER_LAND.toString(),
                          TREND_NÆRING.toString()
                    ));*/
    }
@Test
    public void hentAgreggertStatistikk_returnerer_ikke_virksomhet_statistikk_til_bruker_som_mangler_IA_rettigheter()
          throws Exception {
    oppretteStatistikkForLand(jdbcTemplate);
    jdbcTemplate.update(
            "insert into sykefravar_statistikk_naring5siffer " +
                    "(naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
            parametre(new Næring("88911", "Somatiske spesialsykehjem"), 2022, 1, 10, 5, 100)
    );

    jdbcTemplate.update(
            "insert into sykefravar_statistikk_naring5siffer " +
                    "(naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
            parametre(new Næring("88911", "Somatiske spesialsykehjem"), 2021, 4, 10, 1, 100)
    );
    jdbcTemplate.update(
            "insert into sykefravar_statistikk_naring5siffer " +
                    "(naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
            parametre(new Næring("88911", "Somatiske spesialsykehjem"), 2021, 3, 10, 1, 100)
    );
    jdbcTemplate.update(
            "insert into sykefravar_statistikk_naring5siffer " +
                    "(naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
            parametre(new Næring("88911", "Somatiske spesialsykehjem"), 2021, 2, 10, 1, 100)
    );
    jdbcTemplate.update(
            "insert into sykefravar_statistikk_naring5siffer " +
                    "(naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
            parametre(new Næring("88911", "Somatiske spesialsykehjem"), 2021, 1, 10, 1, 100)
    );

    String jwtToken = TestTokenUtil.createMockIdportenTokenXToken(mockOAuth2Server);

        HttpResponse<String> response = utførAutorisertKall(ORGNR_UNDERENHET_UTEN_IA_RETTIGHETER, jwtToken);
        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode responseBody = objectMapper.readTree(response.body());

        assertThat(responseBody.findValuesAsText("type"))
              .containsExactlyInAnyOrderElementsOf(
                    List.of(
                          PROSENT_SISTE_4_KVARTALER_BRANSJE.toString(),
                          PROSENT_SISTE_4_KVARTALER_LAND.toString(),
                          TREND_BRANSJE.toString()
                    ));

    }

    private HttpResponse<String> utførAutorisertKall(String orgnr, String jwtToken)
          throws IOException, InterruptedException {
        return newBuilder().build().send(
              HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/"
                          + orgnr +
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
    }

    private MapSqlParameterSource parametre(int årstall, int kvartal, int antallPersoner, int tapteDagsverk, int muligeDagsverk) {
        return new MapSqlParameterSource()
                .addValue("arstall", årstall)
                .addValue("kvartal", kvartal)
                .addValue("antall_personer", antallPersoner)
                .addValue("tapte_dagsverk", tapteDagsverk)
                .addValue("mulige_dagsverk", muligeDagsverk);
    }
    private MapSqlParameterSource parametre(Næring næring, int årstall, int kvartal, int antallPersoner, int tapteDagsverk, int muligeDagsverk) {
        return parametre(årstall, kvartal, antallPersoner, tapteDagsverk, muligeDagsverk)
                .addValue("naring_kode", næring.getKode());
    }

    private MapSqlParameterSource parametre(
            Orgnr orgnr,
            int årstall,
            int kvartal,
            int antallPersoner,
            int tapteDagsverk,
            int muligeDagsverk,
            Varighetskategori varighet
    ) {
        return parametre(årstall, kvartal, antallPersoner, tapteDagsverk, muligeDagsverk)
                .addValue("orgnr", orgnr.getVerdi())
                .addValue("varighet", varighet.kode);
    }
}
