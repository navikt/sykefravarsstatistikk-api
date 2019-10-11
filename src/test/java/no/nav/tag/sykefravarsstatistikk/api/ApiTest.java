package no.nav.tag.sykefravarsstatistikk.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.net.http.HttpClient.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"mock.port=8083"})
public class ApiTest {

    @LocalServerPort
    private String port;

    private ObjectMapper objectMapper = new ObjectMapper();

    // TODO Får å kjøre denne testen må vi få TokenGeneratorController til å fungere
    @Ignore
    @Test
    public void sammenligning__skal_returnere_riktig_objekt() throws Exception {
        HttpResponse<String> response = newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/sykefravarsstatistikk-api/sammenligning"))
                        .GET()
                        .build(),
                ofString()
        );

        JsonNode ønsketResponseJson = objectMapper.readTree("{" +
                "\"årstall\":2019," +
                "\"kvartal\":1," +
                "\"virksomhet\":{" +
                "\"label\":\"Fisk og Fulg AS\"," +
                "\"prosent\":12.8" +
                "}," +
                "\"næring\":{" +
                "\"label\":\"Offentlig næringsvirksomhet\"," +
                "\"prosent\":3.0" +
                "}," +
                "\"sektor\":{" +
                "\"label\":\"Tjenester tilknyttet informasjonsteknologi\"," +
                "\"prosent\":5.6" +
                "}," +
                "\"land\":{" +
                "\"label\":\"Norge\"," +
                "\"prosent\":5.5" +
                "}" +
                "}"
        );

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(response.body())).isEqualTo(ønsketResponseJson);
    }

}
