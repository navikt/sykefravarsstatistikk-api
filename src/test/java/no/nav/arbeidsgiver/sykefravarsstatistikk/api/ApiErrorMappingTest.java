package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import common.SpringIntegrationTestbase;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static java.net.http.HttpClient.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestTokenUtil.SELVBETJENING_ISSUER_ID;
import static org.assertj.core.api.Assertions.assertThat;

public class ApiErrorMappingTest extends SpringIntegrationTestbase {

    @Autowired
    private MockOAuth2Server mockOAuth2Server;

    @LocalServerPort
    private String port;

    private final static String ORGNR_UNDERENHET_UTEN_NÆRING = "555555555";


    @Test
    public void sykefraværshistorikk__skal_returnere_SERVER_ERROR_med_causedBy_dersom_underenhet_ikke_har_noe_næringskode()
            throws Exception {
        HttpResponse<String> response = newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        "http://localhost:" + port
                                                + "/sykefravarsstatistikk-api/" + ORGNR_UNDERENHET_UTEN_NÆRING
                                                + "/sykefravarshistorikk/summert?antallKvartaler=4"
                                )
                        )
                        .header(
                                AUTHORIZATION,
                                "Bearer "
                                        + TestTokenUtil.createToken(
                                        mockOAuth2Server,
                                        "15008462396",
                                        SELVBETJENING_ISSUER_ID,
                                        "",
                                        ""
                                )
                        )
                        .GET()
                        .build(),
                ofString()
        );

        assertThat(response.statusCode()).isEqualTo(500);
        assertThat(response.body()).isEqualTo("{\"message\":\"Internal error\",\"causedBy\":\"INGEN_NÆRING\"}");
    }
}
