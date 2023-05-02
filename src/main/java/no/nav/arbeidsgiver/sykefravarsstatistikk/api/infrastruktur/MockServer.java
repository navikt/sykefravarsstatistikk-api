package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur;

import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.matching.AnythingPattern;
import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import io.micrometer.core.instrument.util.IOUtils;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({"local", "mvc-test"})
public class MockServer {
  public static final boolean AKTIVER_VERBOSE_LOGGING_I_KONSOLEN = false;
  private final WireMockServer server;

  public MockServer(
      @Value("${wiremock.mock.port}") Integer port,
      @Value("${altinn.url}") String altinnUrl,
      @Value("${altinn.proxy.url}") String altinnProxyUrl,
      @Value("${enhetsregisteret.url}") String enhetsregisteretUrl,
      @Value("${unleash.url}") String unleashUrl,
      Environment environment) {
    log.info("Starter mock-server på port " + port);

    this.server =
        new WireMockServer(
            WireMockConfiguration.wireMockConfig()
                .port(port)
                .notifier(new ConsoleNotifier(AKTIVER_VERBOSE_LOGGING_I_KONSOLEN)));

    if (Arrays.asList(environment.getActiveProfiles()).contains("local")
        || Arrays.asList(environment.getActiveProfiles()).contains("mvc-test")) {
      log.info("Mocker kall fra Altinn");
      mockKall(altinnProxyUrl + "v2/organisasjoner", HttpStatus.NOT_FOUND);
      mockKallUtenParametereFraFil(altinnProxyUrl + "v2/organisasjoner", "altinnReportees.json");
      mockKallMedParametereFraFil(
          altinnProxyUrl + "v2/organisasjoner", "altinnReporteesMedIARettigheter.json");
    }

    log.info("Mocker kall fra Enhetsregisteret");
    mockKallFraEnhetsregisteret(StringUtils.removeEnd(enhetsregisteretUrl, "/"));

    log.info("Mocker kall fra Unleash");
    mockKallFraUnleash(unleashUrl);

    server.start();
  }

  @SneakyThrows
  private void mockKallFraUnleash(String unleashUrl) {
    String path = new URL(unleashUrl).getPath();
    server.stubFor(
        WireMock.post(path + "client/register")
            .willReturn(
                WireMock.aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(HttpStatus.OK.value())
                    .withBody("[]")));
    server.stubFor(
        WireMock.get(path + "client/features")
            .willReturn(
                WireMock.aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(HttpStatus.OK.value())
                    .withBody("[]")));
    server.stubFor(
        WireMock.post(path + "client/metrics")
            .willReturn(
                WireMock.aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(HttpStatus.OK.value())
                    .withBody("[]")));

    mockKall(WireMock.urlPathMatching(path + "/unleash/client/features"), "[]");
  }

  @SneakyThrows
  private void mockKallFraEnhetsregisteret(String enhetsregisteretUrl) {
    String path = new URL(enhetsregisteretUrl).getPath();
    mockKall(
        WireMock.urlPathMatching(path + "/underenheter/[0-9]{9}"),
        lesFilSomString("enhetsregisteretUnderenhet.json"));
    mockKall(
        WireMock.urlPathMatching(path + "/underenheter/444444444"),
        lesFilSomString("enhetsregisteretUnderenhet_444444444.json"));
    mockKall(
        WireMock.urlPathMatching(path + "/underenheter/555555555"),
        lesFilSomString("enhetsregisteretUnderenhet_555555555.json"));
    mockKall(
        WireMock.urlPathMatching(path + "/underenheter/910562452"),
        lesFilSomString("dev_enhetsregisteretUnderenhet_910562452.json"));
    mockKall(
        WireMock.urlPathMatching(path + "/underenheter/910825518"),
        lesFilSomString("dev_enhetsregisteretUnderenhet_910825518.json"));
    mockKall(
        WireMock.urlPathMatching(path + "/underenheter/910562436"),
        lesFilSomString("dev_enhetsregisteretUnderenhet_910562436.json"));
    mockKall(
        WireMock.urlPathMatching(path + "/underenheter/311874411"),
        lesFilSomString("dev_enhetsregisteretUnderenhet_311874411.json"));
    mockKall(
        WireMock.urlPathMatching(path + "/underenheter/315829062"),
        lesFilSomString("dev_enhetsregisteretUnderenhet_315829062.json"));
    mockKall(
        WireMock.urlPathMatching(path + "/enheter/[0-9]{9}"),
        lesFilSomString("enhetsregisteretEnhet.json"));
    mockKall(
        WireMock.urlPathMatching(path + "/enheter/910562223"),
        lesFilSomString("dev_enhetsregisteretEnhet.json"));
    mockKall(
        WireMock.urlPathMatching(path + "/enheter/310529915"),
        lesFilSomString("dev_enhetsregisteretEnhet_310529915.json"));
    mockKall(
        WireMock.urlPathMatching(path + "/enheter/313068420"),
        lesFilSomString("dev_enhetsregisteretEnhet_313068420.json"));
  }

  private void mockKallFraFil(String url, String filnavn) {
    mockKall(url, lesFilSomString(filnavn));
  }

  @SneakyThrows
  private void mockKall(String url, String body) {
    String path = new URL(url).getPath();
    mockKall(urlPathEqualTo(path), body);
  }

  @SneakyThrows
  private void mockKall(UrlPathPattern urlPathPattern, String body) {
    server.stubFor(
        WireMock.get(urlPathPattern)
            .willReturn(
                WireMock.aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(HttpStatus.OK.value())
                    .withBody(body)));
  }

  @SneakyThrows
  private void mockKallMedParametereFraFil(String url, String filnavn) {
    String body = lesFilSomString(filnavn);
    String path = new URL(url).getPath();
    Map<String, StringValuePattern> params = getParams();
    params.put("serviceCode", new ContainsPattern("3403"));
    params.put("serviceEdition", new AnythingPattern());

    server.stubFor(
        WireMock.get(urlPathEqualTo(path))
            .withQueryParams(params)
            .willReturn(
                WireMock.aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(HttpStatus.OK.value())
                    .withBody(body)));
  }

  @SneakyThrows
  private void mockKallUtenParametereFraFil(String url, String filnavn) {
    String body = lesFilSomString(filnavn);
    String path = new URL(url).getPath();
    Map<String, StringValuePattern> params = getParams();

    server.stubFor(
        WireMock.get(urlPathEqualTo(path))
            .withQueryParams(params)
            .willReturn(
                WireMock.aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(HttpStatus.OK.value())
                    .withBody(body)));
  }

  @NotNull
  private Map<String, StringValuePattern> getParams() {
    Map<String, StringValuePattern> params = new HashMap<>();
    params.put("filter", new AnythingPattern());
    params.put("top", new AnythingPattern());
    params.put("skip", new AnythingPattern());
    return params;
  }

  @SneakyThrows
  private void mockKall(String url, HttpStatus status) {
    String path = new URL(url).getPath();
    UrlPattern urlMatching = WireMock.urlMatching(".*" + path + ".*");
    server.stubFor(
        WireMock.get(urlMatching).willReturn(WireMock.aResponse().withStatus(status.value())));
  }

  @SneakyThrows
  private String lesFilSomString(String filnavn) {
    return IOUtils.toString(
        this.getClass().getClassLoader().getResourceAsStream("mock/" + filnavn), UTF_8);
  }
}
