package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.api

import config.SpringIntegrationTestbase
import testUtils.TestUtils.SISTE_PUBLISERTE_KVARTAL
import testUtils.TestUtils.slettAlleImporttidspunkt
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.ImporttidspunktRepository
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
import java.time.LocalDate

class PubliseringsdatoerApiIntegrationTest : SpringIntegrationTestbase() {
    @LocalServerPort
    private val port: String? = null

    @Autowired
    private lateinit var importtidspunktRepository: ImporttidspunktRepository

    @Autowired
    var jdbcTemplate: NamedParameterJdbcTemplate? = null

    @BeforeEach
    fun setUp() {
        importtidspunktRepository.slettAlleImporttidspunkt()
    }

    @AfterEach
    fun tearDown() {
        importtidspunktRepository.slettAlleImporttidspunkt()
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun hentPubliseringsdatoer_skalReturnereResponsMedKorrektFormat() {
        importtidspunktRepository.settInnImporttidspunkt(SISTE_PUBLISERTE_KVARTAL, LocalDate.parse("2022-06-02"))
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
