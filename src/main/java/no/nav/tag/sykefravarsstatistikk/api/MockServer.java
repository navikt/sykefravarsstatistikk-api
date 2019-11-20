package no.nav.tag.sykefravarsstatistikk.api;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
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
@Profile({"local", "dev"})
public class MockServer {
    private final WireMockServer server;

    public MockServer(
            @Value("${mock.port}") Integer port,
            @Value("${altinn.url}") String altinnUrl,
            @Value("${enhetsregisteret.url}") String enhetsregisteretUrl,
            @Value("${spring.profiles.active}") String springProfileActive
    ) {
        log.info("Starter mock-server på port " + port);

        this.server = new WireMockServer(port);

        if ("local".equals(springProfileActive)) {
            System.out.println("Mocker kall fra Altinn");
            mockKallFraFil(altinnUrl + "ekstern/altinn/api/serviceowner/reportees", "altinnReportees.json");
        }

        System.out.println("Mocker kall fra Enhetsregisteret");
        mockKallFraEnhetsregisteret(enhetsregisteretUrl);

        server.start();
    }

    @SneakyThrows
    private void mockKallFraEnhetsregisteret(String enhetsregisteretUrl) {
        String path = new URL(enhetsregisteretUrl).getPath();
        mockKall(WireMock.urlPathMatching(path + "underenheter/[0-9]{9}"), lesFilSomString("enhetsregisteretUnderenhet.json"));
        mockKall(WireMock.urlPathMatching(path + "underenheter/910562452"), lesFilSomString("dev_enhetsregisteretUnderenhet.json"));
        mockKall(WireMock.urlPathMatching(path + "enheter/[0-9]{9}"), lesFilSomString("enhetsregisteretEnhet.json"));
        mockKall(WireMock.urlPathMatching(path + "enheter/910562223"), lesFilSomString("dev_enhetsregisteretEnhet.json"));
    }

    private void mockKallFraFil(String url, String filnavn) {
        mockKall(url, lesFilSomString(filnavn));
    }

    @SneakyThrows
    private void mockKall(String url, String body) {
        String path = new URL(url).getPath();
        mockKall(WireMock.urlPathEqualTo(path), body);
    }

    @SneakyThrows
    private void mockKall(UrlPathPattern urlPathPattern, String body) {
        server.stubFor(
                WireMock.get(urlPathPattern).willReturn(WireMock.aResponse()
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
