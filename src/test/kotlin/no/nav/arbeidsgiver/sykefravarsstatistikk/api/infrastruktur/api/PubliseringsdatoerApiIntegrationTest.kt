package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.api

import config.SpringIntegrationTestbase
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoer.Publiseringsdato
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.ImporttidspunktRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.PubliseringsdatoerRepository
import org.assertj.core.api.AssertionsForClassTypes
import org.jetbrains.exposed.sql.deleteAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
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
    private lateinit var publiseringsdatoerRepository: PubliseringsdatoerRepository

    @BeforeEach
    fun setUp() {
        with(importtidspunktRepository) { transaction { deleteAll() } }
    }

    @AfterEach
    fun tearDown() {
        with(importtidspunktRepository) { transaction { deleteAll() } }
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun hentPubliseringsdatoer_skalReturnereResponsMedKorrektFormat() {
        importtidspunktRepository.settInnImporttidspunkt(ÅrstallOgKvartal(2022, 1), LocalDate.parse("2022-06-02"))
        publiseringsdatoerRepository.overskrivPubliseringsdatoer(
            listOf(
                Publiseringsdato(
                    0,
                    LocalDate.parse("2022-09-08"),
                    LocalDate.parse("2022-01-01"),
                )
            )
        )

        val response = HttpClient.newBuilder()
            .build()
            .send(
                HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:$port/sykefravarsstatistikk-api/publiseringsdato"))
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
