package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.api

import config.SpringIntegrationTestbase
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.server.LocalServerPort
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

class InternalControllerTestSpring : SpringIntegrationTestbase() {
    @LocalServerPort
    private val port: String? = null
    @Test
    @Throws(Exception::class)
    fun healthcheck_returnerer_OK__når_applikasjon_kjører() {
        val response = HttpClient.newBuilder()
            .build()
            .send(
                HttpRequest.newBuilder()
                    .uri(
                        URI.create(
                            "http://localhost:"
                                    + port
                                    + "/sykefravarsstatistikk-api/internal/liveness"
                        )
                    )
                    .GET()
                    .build(),
                BodyHandlers.ofString()
            )
        Assertions.assertThat(response.statusCode()).isEqualTo(200)
    }
}
