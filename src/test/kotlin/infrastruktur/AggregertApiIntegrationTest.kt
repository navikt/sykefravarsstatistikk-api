package infrastruktur

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.net.HttpHeaders
import config.SpringIntegrationTestbase
import io.kotest.matchers.shouldBe
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import testUtils.TestTokenUtil.createMockIdportenTokenXToken
import testUtils.TestUtils.PRODUKSJON_NYTELSESMIDLER
import testUtils.TestUtils.SISTE_PUBLISERTE_KVARTAL
import testUtils.TestUtils.opprettStatistikkForLand
import testUtils.TestUtils.slettAllStatistikkFraDatabase
import testUtils.TestUtils.slettAlleImporttidspunkt
import testUtils.insertData
import java.math.BigDecimal
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.time.LocalDate

class AggregertApiIntegrationTest : SpringIntegrationTestbase() {
    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    @Autowired
    lateinit var mockOAuth2Server: MockOAuth2Server

    @Autowired
    lateinit var sykefravarStatistikkVirksomhetRepository: SykefravarStatistikkVirksomhetRepository

    @Autowired
    lateinit var sykefravarStatistikkVirksomhetGraderingRepository: SykefravarStatistikkVirksomhetGraderingRepository

    @Autowired
    lateinit var sykefraværStatistikkLandRepository: SykefraværStatistikkLandRepository

    @Autowired
    lateinit var sykefraværStatistikkNæringRepository: SykefraværStatistikkNæringRepository

    @Autowired
    lateinit var sykefraværStatistikkNæringskodeRepository: SykefraværStatistikkNæringskodeRepository

    @Autowired
    lateinit var importtidspunktRepository: ImporttidspunktRepository

    @BeforeEach
    fun setUp() {
        slettAllStatistikkFraDatabase(
            jdbcTemplate = jdbcTemplate,
            sykefravarStatistikkVirksomhetRepository = sykefravarStatistikkVirksomhetRepository,
            sykefraværStatistikkLandRepository = sykefraværStatistikkLandRepository,
            sykefravarStatistikkVirksomhetGraderingRepository = sykefravarStatistikkVirksomhetGraderingRepository,
            sykefraværStatistikkNæringRepository = sykefraværStatistikkNæringRepository
        )
        importtidspunktRepository.slettAlleImporttidspunkt()
        importtidspunktRepository.settInnImporttidspunkt(SISTE_PUBLISERTE_KVARTAL, LocalDate.parse("2022-06-02"))
    }

    @AfterEach
    fun tearDown() {
        slettAllStatistikkFraDatabase(
            jdbcTemplate = jdbcTemplate,
            sykefravarStatistikkVirksomhetRepository = sykefravarStatistikkVirksomhetRepository,
            sykefraværStatistikkLandRepository = sykefraværStatistikkLandRepository,
            sykefravarStatistikkVirksomhetGraderingRepository = sykefravarStatistikkVirksomhetGraderingRepository
        )
        importtidspunktRepository.slettAlleImporttidspunkt()
    }

    @LocalServerPort
    private val port: String? = null
    private val objectMapper = ObjectMapper()
    private val BARNEHAGER = Næringskode("88911")

    @Test
    @Throws(Exception::class)
    fun hentAgreggertStatistikk_skalReturnere403NaarBrukerIkkeRepresentererBedriften() {
        val response = utførAutorisertKall(ORGNR_UNDERENHET_INGEN_TILGANG)
        Assertions.assertThat(response.statusCode()).isEqualTo(403)
    }

    @Test
    @Throws(Exception::class)
    fun hentAgreggertStatistikk_skalReturnereStatus200SelvOmDetIkkeFinnesData() {
        val response = utførAutorisertKall(ORGNR_UNDERENHET)
        Assertions.assertThat(response.statusCode()).isEqualTo(200)
        val responseBody = objectMapper.readTree(response.body())
        Assertions.assertThat(responseBody["prosentSiste4KvartalerTotalt"]).isEmpty()
        Assertions.assertThat(responseBody["prosentSiste4KvartalerGradert"]).isEmpty()
        Assertions.assertThat(responseBody["trendTotalt"]).isEmpty()
        Assertions.assertThat(responseBody["prosentSiste4KvartalerKorttid"]).isEmpty()
        Assertions.assertThat(responseBody["prosentSiste4KvartalerLangtid"]).isEmpty()
        Assertions.assertThat(responseBody["trendTotalt"]).isEmpty()
    }

    @Test
    @Throws(Exception::class)
    fun hentAgreggertStatistikk_returnererForventedeTyperForBedriftSomHarAlleTyperData() {
        val (årstall, kvartal) = SISTE_PUBLISERTE_KVARTAL.minusEttÅr()
        opprettStatistikkForLand(sykefraværStatistikkLandRepository)
        sykefraværStatistikkNæringRepository.settInn(
            listOf(
                SykefraværsstatistikkForNæring(
                    årstall = SISTE_PUBLISERTE_KVARTAL.årstall,
                    kvartal = SISTE_PUBLISERTE_KVARTAL.kvartal,
                    næringkode = PRODUKSJON_NYTELSESMIDLER.tosifferIdentifikator,
                    antallPersoner = 10,
                    tapteDagsverk = 5.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal()
                ),
                SykefraværsstatistikkForNæring(
                    årstall = årstall,
                    kvartal = kvartal,
                    næringkode = PRODUKSJON_NYTELSESMIDLER.tosifferIdentifikator,
                    antallPersoner = 10,
                    tapteDagsverk = 20.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal()
                )
            )
        )
        sykefravarStatistikkVirksomhetRepository.settInn(
            listOf(
                SykefraværsstatistikkVirksomhet(
                    årstall = SISTE_PUBLISERTE_KVARTAL.årstall,
                    kvartal = SISTE_PUBLISERTE_KVARTAL.kvartal,
                    orgnr = ORGNR_UNDERENHET,
                    tapteDagsverk = 5.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal(),
                    antallPersoner = 10,
                    varighet = "A",
                    rectype = "2",
                )
            )
        )
        sykefravarStatistikkVirksomhetGraderingRepository.insertData(
            ORGNR_UNDERENHET,
            "10",
            "10300",
            DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
            SISTE_PUBLISERTE_KVARTAL,
            7,
            BigDecimal(10),
            BigDecimal(20),
            BigDecimal(100)
        )
        sykefravarStatistikkVirksomhetGraderingRepository.insertData(
            ORGNR_UNDERENHET,
            "10",
            "10300",
            DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
            SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1),
            7,
            BigDecimal(12),
            BigDecimal(20),
            BigDecimal(100)
        )
        sykefravarStatistikkVirksomhetGraderingRepository.insertData(
            ORGNR_UNDERENHET,
            "10",
            "10300",
            DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
            SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2),
            15,
            BigDecimal(25),
            BigDecimal(50),
            BigDecimal(300)
        )
        val response = utførAutorisertKall(ORGNR_UNDERENHET)
        Assertions.assertThat(response.statusCode()).isEqualTo(200)
        val responseBody = objectMapper.readTree(response.body())
        val prosentSiste4Kvartaler = responseBody["prosentSiste4KvartalerTotalt"]
        val gradertProsentSiste4Kvartaler = responseBody["prosentSiste4KvartalerGradert"]
        Assertions.assertThat(responseBody["trendTotalt"].findValuesAsText("statistikkategori"))
            .containsExactly(Statistikkategori.BRANSJE.toString())
        Assertions.assertThat(prosentSiste4Kvartaler.findValuesAsText("statistikkategori"))
            .containsExactlyInAnyOrderElementsOf(
                listOf(
                    Statistikkategori.VIRKSOMHET.toString(),
                    Statistikkategori.BRANSJE.toString(),
                    Statistikkategori.LAND.toString()
                )
            )
        Assertions.assertThat(gradertProsentSiste4Kvartaler.findValuesAsText("statistikkategori"))
            .containsExactlyInAnyOrderElementsOf(
                listOf(
                    Statistikkategori.VIRKSOMHET.toString(),
                    Statistikkategori.BRANSJE.toString()
                )
            )
        Assertions.assertThat(prosentSiste4Kvartaler[0]["label"].textValue())
            .isEqualTo("NAV ARBEID OG YTELSER AVD OSLO")
    }

    @Test
    @Throws(Exception::class)
    fun hentAggregertStatistikk_returnererLangtidOgKorttidForVirksomhetOgBransje() {

        sykefravarStatistikkVirksomhetRepository.settInn(
            listOf(
                SykefraværsstatistikkVirksomhet(
                    årstall = 2022,
                    kvartal = 1,
                    ORGNR_UNDERENHET,
                    varighet = Varighetskategori._1_DAG_TIL_7_DAGER.kode,
                    rectype = "2",
                    tapteDagsverk = BigDecimal(2),
                    muligeDagsverk = BigDecimal(0),
                    antallPersoner = 0,
                ),
                SykefraværsstatistikkVirksomhet(
                    årstall = 2022,
                    kvartal = 1,
                    orgnr = ORGNR_UNDERENHET,
                    varighet = Varighetskategori._8_UKER_TIL_20_UKER.kode,
                    rectype = "2",
                    tapteDagsverk = BigDecimal(4),
                    muligeDagsverk = BigDecimal(0),
                    antallPersoner = 0,
                ),
                SykefraværsstatistikkVirksomhet(
                    årstall = 2022,
                    kvartal = 1,
                    orgnr = ORGNR_UNDERENHET,
                    varighet = Varighetskategori.TOTAL.kode,
                    rectype = "2",
                    tapteDagsverk = BigDecimal(0),
                    muligeDagsverk = BigDecimal(100),
                    antallPersoner = 10
                )
            )
        )
        leggTilStatisitkkNæringMedVarighetForTotalVarighetskategori(
            jdbcTemplate, Næringskode("10300"), ÅrstallOgKvartal(2022, 1), 10, 100
        )
        leggTilStatisitkkNæringMedVarighet(
            jdbcTemplate,
            Næringskode("10300"),
            ÅrstallOgKvartal(2022, 1),
            Varighetskategori._1_DAG_TIL_7_DAGER,
            10
        )
        val response = utførAutorisertKall(ORGNR_UNDERENHET)
        Assertions.assertThat(response.statusCode()).isEqualTo(200)
        val responseBody = objectMapper.readTree(response.body())
        val korttidProsentSiste4Kvartaler = responseBody["prosentSiste4KvartalerKorttid"]
        val LangtidProsentSiste4Kvartaler = responseBody["prosentSiste4KvartalerLangtid"]
        Assertions.assertThat(korttidProsentSiste4Kvartaler.findValuesAsText("statistikkategori"))
            .containsExactlyInAnyOrderElementsOf(
                listOf(
                    Statistikkategori.VIRKSOMHET.toString(),
                    Statistikkategori.BRANSJE.toString()
                )
            )
        Assertions.assertThat(LangtidProsentSiste4Kvartaler.findValuesAsText("statistikkategori"))
            .containsExactlyInAnyOrderElementsOf(
                listOf(
                    Statistikkategori.VIRKSOMHET.toString(),
                    Statistikkategori.BRANSJE.toString()
                )
            )
    }

    @Test
    @Throws(Exception::class)
    fun hentAgreggertStatistikk_returnererIkkeVirksomhetstatistikkTilBrukerSomManglerIaRettigheter() {
        val (årstall, kvartal) = SISTE_PUBLISERTE_KVARTAL.minusEttÅr()
        sykefraværStatistikkNæringskodeRepository.settInn(
            listOf(
                SykefraværsstatistikkForNæringskode(
                    årstall = SISTE_PUBLISERTE_KVARTAL.årstall,
                    kvartal = SISTE_PUBLISERTE_KVARTAL.kvartal,
                    næringkode5siffer = BARNEHAGER.femsifferIdentifikator,
                    antallPersoner = 10,
                    tapteDagsverk = 5.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal()
                ),
                SykefraværsstatistikkForNæringskode(
                    årstall = årstall,
                    kvartal = kvartal,
                    næringkode5siffer = BARNEHAGER.femsifferIdentifikator,
                    antallPersoner = 10,
                    tapteDagsverk = 1.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal(),
                )
            )
        )
        opprettStatistikkForLand(sykefraværStatistikkLandRepository)

        val response = utførAutorisertKall(ORGNR_UNDERENHET_UTEN_IA_RETTIGHETER)
        val responseBody = objectMapper.readTree(response.body())
        Assertions.assertThat(
            responseBody["prosentSiste4KvartalerTotalt"].findValuesAsText("statistikkategori")
        )
            .containsExactlyInAnyOrderElementsOf(
                listOf(
                    Statistikkategori.BRANSJE.toString(),
                    Statistikkategori.LAND.toString()
                )
            )
        Assertions.assertThat(responseBody["trendTotalt"].findValuesAsText("statistikkategori"))
            .containsExactly(Statistikkategori.BRANSJE.toString())
    }

    @Test
    fun `hentAgreggertStatistikk kræsjer ikke dersom mulige dagsverk er 0`() {
        sykefravarStatistikkVirksomhetRepository.settInn(
            listOf(
                SykefraværsstatistikkVirksomhet(
                    årstall = SISTE_PUBLISERTE_KVARTAL.årstall,
                    kvartal = SISTE_PUBLISERTE_KVARTAL.kvartal,
                    orgnr = ORGNR_UNDERENHET,
                    tapteDagsverk = 5.toBigDecimal(),
                    muligeDagsverk = 0.toBigDecimal(),
                    antallPersoner = 10,
                    varighet = "A",
                    rectype = "2",
                )
            )
        )
        val response = utførAutorisertKall(ORGNR_UNDERENHET)
        val responseBody = objectMapper.readTree(response.body())

        responseBody["prosentSiste4KvartalerTotalt"].findValuesAsText("statistikkategori") shouldBe emptyList()
    }

    @Test
    @Throws(Exception::class)
    fun hentAgreggertStatistikk_viserNavnetTilBransjenEllerNæringenSomLabel() {
        sykefraværStatistikkNæringskodeRepository.settInn(
            listOf(
                SykefraværsstatistikkForNæringskode(
                    årstall = 2022,
                    kvartal = 1,
                    næringkode5siffer = BARNEHAGER.femsifferIdentifikator,
                    antallPersoner = 10,
                    tapteDagsverk = 5.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal()
                )
            )
        )
        val response = utførAutorisertKall(ORGNR_UNDERENHET_UTEN_IA_RETTIGHETER)
        val responseBody = objectMapper.readTree(response.body())
        val barnehageJson = responseBody["prosentSiste4KvartalerTotalt"][0]
        Assertions.assertThat(barnehageJson["label"].toString()).isEqualTo("\"Barnehager\"")
    }

    private fun utførAutorisertKall(orgnr: String): HttpResponse<String> {
        val jwtToken = createMockIdportenTokenXToken(mockOAuth2Server)
        return HttpClient.newBuilder()
            .build()
            .send(
                HttpRequest.newBuilder()
                    .GET()
                    .uri(
                        URI.create(
                            "http://127.0.0.1:"
                                    + port
                                    + "/sykefravarsstatistikk-api/"
                                    + orgnr
                                    + "/v1/sykefravarshistorikk/aggregert"
                        )
                    )
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")
                    .build(),
                BodyHandlers.ofString()
            )
    }

    companion object {
        private const val ORGNR_UNDERENHET = "910969439"
        private const val ORGNR_UNDERENHET_UTEN_IA_RETTIGHETER = "910825518"
        private const val ORGNR_UNDERENHET_INGEN_TILGANG = "777777777"
    }
}
