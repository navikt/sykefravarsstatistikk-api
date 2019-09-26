package no.nav.tag.sykefravarsstatistikk.api;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.micrometer.core.instrument.util.IOUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.URL;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Component
@Profile("local")
public class MockServer {
    private final WireMockServer server;

    public MockServer(
            @Value("${mock.port}") Integer port,
            @Value("${altinn.url}") String altinnUrl
    ) {
        log.info("Starter mock-server på port " + port);

        this.server = new WireMockServer(port);

        mockKallFraFil(altinnUrl + "ekstern/altinn/api/serviceowner/reportees", "altinnReportees.json");

        server.start();
    }

    private void mockKallFraFil(String url, String filnavn) {
        mockKall(url, lesFilSomString(filnavn));
    }

    @SneakyThrows
    private void mockKall(String url, String body) {
        String path = new URL(url).getPath();
        server.stubFor(
                WireMock.get(WireMock.urlPathEqualTo(path)).willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.OK.value())
                        .withBody(body)
                )
        );
    }

    @SneakyThrows
    private String lesFilSomString(String filnavn) {
        return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("mock/" + filnavn), UTF_8);
    }
}
