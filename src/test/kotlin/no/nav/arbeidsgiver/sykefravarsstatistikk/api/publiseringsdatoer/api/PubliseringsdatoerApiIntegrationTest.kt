package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.api

import common.SpringIntegrationTestbase
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.skrivSisteImporttidspunktTilDb
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAlleImporttidspunkt
import org.assertj.core.api.AssertionsForClassTypes
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

class PubliseringsdatoerApiIntegrationTest : SpringIntegrationTestbase() {
    @LocalServerPort
    private val port: String? = null

    @Autowired
    var jdbcTemplate: NamedParameterJdbcTemplate? = null

    @BeforeEach
    fun setUp() {
        slettAlleImporttidspunkt(jdbcTemplate!!)
    }

    @AfterEach
    fun tearDown() {
        slettAlleImporttidspunkt(jdbcTemplate!!)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun hentPubliseringsdatoer_skalReturnereResponsMedKorrektFormat() {
        skrivSisteImporttidspunktTilDb(jdbcTemplate!!)
        val response = HttpClient.newBuilder()
            .build()
            .send(
                HttpRequest.newBuilder()
                    .uri(
                        URI.create(
                            "http://127.0.0.1:"
                                    + port
                                    + "/sykefravarsstatistikk-api/publiseringsdato"
                        )
                    )
                    .GET()
                    .build(),
                BodyHandlers.ofString()
            )
        val forventetRespons = (""
                + "{\"sistePubliseringsdato\":\"2022-06-02\","
                + "\"nestePubliseringsdato\":\"2022-09-08\","
                + "\"gjeldendePeriode\":{\"årstall\":2022,\"kvartal\":1}}")
        AssertionsForClassTypes.assertThat(response.body()).isEqualTo(forventetRespons)
    }
}