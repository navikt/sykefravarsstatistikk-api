package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import com.github.tomakehurst.wiremock.matching.UrlPathPattern
import io.micrometer.core.instrument.util.IOUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.net.URL
import java.nio.charset.StandardCharsets

@Component
@Profile("local", "mvc-test")
class MockServer(
    @Value("\${wiremock.mock.port}") port: Int,
    @Value("\${altinn.proxy.url}") altinnProxyUrl: String,
    @Value("\${enhetsregisteret.url}") enhetsregisteretUrl: String?,
    environment: Environment
) {
    private val server: WireMockServer
    private val log = LoggerFactory.getLogger(this::class.java)

    init {
        log.info("Starter mock-server på port $port")
        server = WireMockServer(
            WireMockConfiguration.wireMockConfig()
                .port(port)
                .notifier(ConsoleNotifier(AKTIVER_VERBOSE_LOGGING_I_KONSOLEN))
        )
        if (listOf<String>(*environment.activeProfiles).contains("local")
            || listOf<String>(*environment.activeProfiles).contains("mvc-test")
        ) {
            log.info("Mocker kall fra Altinn")
            mockKall(altinnProxyUrl + "v2/organisasjoner", HttpStatus.NOT_FOUND)
            mockKallUtenParametereFraFil(altinnProxyUrl + "v2/organisasjoner", "altinnReportees.json")
            mockKallMedParametereFraFil(
                altinnProxyUrl + "v2/organisasjoner", "altinnReporteesMedIARettigheter.json"
            )
        }
        log.info("Mocker kall fra Enhetsregisteret")
        mockKallFraEnhetsregisteret(StringUtils.removeEnd(enhetsregisteretUrl, "/"))
        server.start()
    }

    private fun mockKallFraEnhetsregisteret(enhetsregisteretUrl: String) {
        val path = URL(enhetsregisteretUrl).path
        mockKall(
            WireMock.urlPathMatching("$path/underenheter/[0-9]{9}"),
            lesFilSomString("enhetsregisteretUnderenhet.json")
        )
        mockKall(
            WireMock.urlPathMatching("$path/underenheter/444444444"),
            lesFilSomString("enhetsregisteretUnderenhet_444444444.json")
        )
        mockKall(
            WireMock.urlPathMatching("$path/underenheter/555555555"),
            lesFilSomString("enhetsregisteretUnderenhet_555555555.json")
        )
        mockKall(
            WireMock.urlPathMatching("$path/underenheter/910562452"),
            lesFilSomString("dev_enhetsregisteretUnderenhet_910562452.json")
        )
        mockKall(
            WireMock.urlPathMatching("$path/underenheter/910825518"),
            lesFilSomString("dev_enhetsregisteretUnderenhet_910825518.json")
        )
        mockKall(
            WireMock.urlPathMatching("$path/underenheter/910562436"),
            lesFilSomString("dev_enhetsregisteretUnderenhet_910562436.json")
        )
        mockKall(
            WireMock.urlPathMatching("$path/underenheter/311874411"),
            lesFilSomString("dev_enhetsregisteretUnderenhet_311874411.json")
        )
        mockKall(
            WireMock.urlPathMatching("$path/underenheter/315829062"),
            lesFilSomString("dev_enhetsregisteretUnderenhet_315829062.json")
        )
        mockKall(
            WireMock.urlPathMatching("$path/enheter/[0-9]{9}"),
            lesFilSomString("enhetsregisteretEnhet.json")
        )
        mockKall(
            WireMock.urlPathMatching("$path/enheter/910562223"),
            lesFilSomString("dev_enhetsregisteretEnhet.json")
        )
        mockKall(
            WireMock.urlPathMatching("$path/enheter/310529915"),
            lesFilSomString("dev_enhetsregisteretEnhet_310529915.json")
        )
        mockKall(
            WireMock.urlPathMatching("$path/enheter/313068420"),
            lesFilSomString("dev_enhetsregisteretEnhet_313068420.json")
        )
    }

    private fun mockKall(urlPathPattern: UrlPathPattern, body: String) {
        server.stubFor(
            WireMock.get(urlPathPattern)
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.OK.value())
                        .withBody(body)
                )
        )
    }

    private fun mockKallMedParametereFraFil(url: String, filnavn: String) {
        val body = lesFilSomString(filnavn)
        val path = URL(url).path
        val params = params
        params["serviceCode"] = ContainsPattern("3403")
        params["serviceEdition"] = AnythingPattern()
        server.stubFor(
            WireMock.get(WireMock.urlPathEqualTo(path))
                .withQueryParams(params)
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.OK.value())
                        .withBody(body)
                )
        )
    }

    private fun mockKallUtenParametereFraFil(url: String, filnavn: String) {
        val body = lesFilSomString(filnavn)
        val path = URL(url).path
        val params: Map<String, StringValuePattern> = params
        server.stubFor(
            WireMock.get(WireMock.urlPathEqualTo(path))
                .withQueryParams(params)
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.OK.value())
                        .withBody(body)
                )
        )
    }

    private val params: MutableMap<String, StringValuePattern>
        get() {
            val params: MutableMap<String, StringValuePattern> = HashMap()
            params["filter"] = AnythingPattern()
            params["top"] = AnythingPattern()
            params["skip"] = AnythingPattern()
            return params
        }

    private fun mockKall(url: String, status: HttpStatus) {
        val path = URL(url).path
        val urlMatching = WireMock.urlMatching(".*$path.*")
        server.stubFor(
            WireMock.get(urlMatching).willReturn(WireMock.aResponse().withStatus(status.value()))
        )
    }

    private fun lesFilSomString(filnavn: String): String {
        return IOUtils.toString(
            this.javaClass.getClassLoader().getResourceAsStream("mock/$filnavn"), StandardCharsets.UTF_8
        )
    }

    companion object {
        const val AKTIVER_VERBOSE_LOGGING_I_KONSOLEN = false
    }
}
