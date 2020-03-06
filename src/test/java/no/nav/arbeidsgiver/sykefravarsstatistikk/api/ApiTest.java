package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.sykefraværshistorikk.SykefraværshistorikkType;
import no.nav.security.oidc.test.support.JwtTokenGenerator;
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
    public void sykefraværshistorikk__skal_returnere_riktig_objekt() throws Exception {
        HttpResponse<String> response = newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/" + ORGNR_UNDERENHET + "/sykefravarshistorikk"))
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
                                SykefraværshistorikkType.LAND.toString(),
                                SykefraværshistorikkType.SEKTOR.toString(),
                                SykefraværshistorikkType.NÆRING.toString(),
                                SykefraværshistorikkType.VIRKSOMHET.toString(),
                                SykefraværshistorikkType.OVERORDNET_ENHET.toString()
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
                                + ORGNR_UNDERENHET_INGEN_TILGANG + "/sykefravarshistorikk"))
                        .header(AUTHORIZATION, "Bearer " + JwtTokenGenerator.signedJWTAsString("15008462396"))
                        .GET()
                        .build(),
                ofString()
        );
        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.body()).isEqualTo("{\"message\":\"You don't have access to this ressource\"}");
    }
}
