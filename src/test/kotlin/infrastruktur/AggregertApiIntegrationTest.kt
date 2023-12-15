@file:Suppress("SpringJavaInjectionPointsAutowiringInspection")

package infrastruktur

import arrow.core.right
import com.fasterxml.jackson.databind.ObjectMapper
import config.SpringIntegrationTestbase
import io.kotest.matchers.shouldBe
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.altinn.AltinnOrganisasjon
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.altinn.AltinnService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.Rectype
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret.EnhetsregisteretClient
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.assertj.core.api.Assertions
import org.jetbrains.exposed.sql.deleteAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import testUtils.TestTokenUtil.createMockIdportenTokenXToken
import testUtils.TestUtils.SISTE_PUBLISERTE_KVARTAL
import testUtils.TestUtils.opprettStatistikkForLand
import java.math.BigDecimal
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.time.LocalDate

class AggregertApiIntegrationTest : SpringIntegrationTestbase() {
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
    lateinit var sykefraværStatistikkNæringskodeMedVarighetRepository: SykefraværStatistikkNæringskodeMedVarighetRepository

    @Autowired
    lateinit var sykefraværStatistikkNæringskodeRepository: SykefraværStatistikkNæringskodeRepository

    @Autowired
    lateinit var importtidspunktRepository: ImporttidspunktRepository

    @MockBean
    lateinit var altinnService: AltinnService

    @MockBean
    lateinit var enhetsregisteretClient: EnhetsregisteretClient

    val PRODUKSJON_NYTELSESMIDLER = Næring("10")

    @BeforeEach
    fun setUp() {
        with(sykefravarStatistikkVirksomhetRepository) { transaction { deleteAll() } }
        with(sykefraværStatistikkLandRepository) { transaction { deleteAll() } }
        with(sykefravarStatistikkVirksomhetGraderingRepository) { transaction { deleteAll() } }
        with(sykefraværStatistikkNæringRepository) { transaction { deleteAll() } }
        with(sykefraværStatistikkNæringskodeMedVarighetRepository) { transaction { deleteAll() } }
        with(importtidspunktRepository) { transaction { deleteAll() } }

        importtidspunktRepository.settInnImporttidspunkt(SISTE_PUBLISERTE_KVARTAL, LocalDate.parse("2022-06-02"))

        val altinnOrganisasjon = AltinnOrganisasjon(
            name = "NAV ARBEID OG YTELSER AVD OSLO",
            type = "BEDR",
            parentOrganizationNumber = "910999919",
            organizationNumber = ORGNR_UNDERENHET,
            organizationForm = "BEDR",
            status = "Active"
        )

        whenever(altinnService.hentVirksomheterDerBrukerHarTilknytning(any(), any())).thenReturn(
            listOf(altinnOrganisasjon)
        )

        whenever(altinnService.hentVirksomheterDerBrukerHarSykefraværsstatistikkrettighet(any(), any())).thenReturn(
            listOf(altinnOrganisasjon)
        )

        whenever(enhetsregisteretClient.hentUnderenhet(any())).thenReturn(
            Underenhet.Næringsdrivende(
                navn = "NAV ARBEID OG YTELSER AVD OSLO",
                orgnr = Orgnr(ORGNR_UNDERENHET),
                næringskode = Næringskode("10300"),
                overordnetEnhetOrgnr = Orgnr("910825518"),
                antallAnsatte = 10
            ).right()
        )
    }

    @AfterEach
    fun tearDown() {
        with(sykefravarStatistikkVirksomhetRepository) { transaction { deleteAll() } }
        with(sykefraværStatistikkLandRepository) { transaction { deleteAll() } }
        with(sykefravarStatistikkVirksomhetGraderingRepository) { transaction { deleteAll() } }
        with(importtidspunktRepository) { transaction { deleteAll() } }
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
    fun `hentAgreggertStatistikk returnerer forventede typer for bedrift som har alle typer data`() {
        val (årstall, kvartal) = SISTE_PUBLISERTE_KVARTAL.minusEttÅr()
        sykefraværStatistikkLandRepository.settInn(
            listOf(
                SykefraværsstatistikkLand(
                    årstall = SISTE_PUBLISERTE_KVARTAL.årstall,
                    kvartal = SISTE_PUBLISERTE_KVARTAL.kvartal,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal("4.0"),
                    muligeDagsverk = BigDecimal("100.0")
                ),
            )
        )

        sykefraværStatistikkNæringRepository.settInn(
            listOf(
                SykefraværsstatistikkForNæring(
                    årstall = SISTE_PUBLISERTE_KVARTAL.årstall,
                    kvartal = SISTE_PUBLISERTE_KVARTAL.kvartal,
                    næring = PRODUKSJON_NYTELSESMIDLER.tosifferIdentifikator,
                    antallPersoner = 10,
                    tapteDagsverk = 5.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal()
                ),
                SykefraværsstatistikkForNæring(
                    årstall = årstall,
                    kvartal = kvartal,
                    næring = PRODUKSJON_NYTELSESMIDLER.tosifferIdentifikator,
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
                    varighet = 'A',
                    rectype = "2",
                )
            )
        )

        sykefravarStatistikkVirksomhetGraderingRepository.settInn(
            listOf(
                SykefraværsstatistikkVirksomhetMedGradering(
                    årstall = SISTE_PUBLISERTE_KVARTAL.årstall,
                    kvartal = SISTE_PUBLISERTE_KVARTAL.kvartal,
                    orgnr = ORGNR_UNDERENHET,
                    næring = "10",
                    næringkode = "10300",
                    rectype = Rectype.VIRKSOMHET.kode,
                    antallSykemeldinger = 0,
                    tapteDagsverk = BigDecimal(20),
                    muligeDagsverk = BigDecimal(100),
                    antallGraderteSykemeldinger = 0,
                    antallPersoner = 7,
                    tapteDagsverkGradertSykemelding = BigDecimal(10)
                )
            )
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
                ),
            )
        )
        sykefraværStatistikkNæringskodeMedVarighetRepository.settInn(
            listOf(
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = 2022,
                    kvartal = 1,
                    næringkode = Næringskode("10300").femsifferIdentifikator,
                    varighet = Varighetskategori.TOTAL.kode,
                    antallPersoner = 10,
                    tapteDagsverk = 0.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal()
                ),
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = 2022,
                    kvartal = 1,
                    næringkode = Næringskode("10300").femsifferIdentifikator,
                    varighet = Varighetskategori._1_DAG_TIL_7_DAGER.kode,
                    antallPersoner = 0,
                    tapteDagsverk = 10.toBigDecimal(),
                    muligeDagsverk = 0.toBigDecimal()
                )
            )
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
                    varighet = 'A',
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
    fun `hent aggregert statistikk viser ikke virksomhetstall når bruker ikke har rettigheter`() {
        whenever(enhetsregisteretClient.hentUnderenhet(any())).thenReturn(
            Underenhet.Næringsdrivende(
                navn = "NAV ARBEID OG YTELSER AVD OSLO",
                orgnr = Orgnr(ORGNR_UNDERENHET_UTEN_IA_RETTIGHETER),
                næringskode = BARNEHAGER,
                overordnetEnhetOrgnr = Orgnr("910999919"),
                antallAnsatte = 10
            ).right()
        )
        whenever(altinnService.hentVirksomheterDerBrukerHarTilknytning(any(), any())).thenReturn(
            listOf(
                AltinnOrganisasjon(
                    name = "Barnehager",
                    type = "BEDR",
                    parentOrganizationNumber = "910999919",
                    organizationNumber = ORGNR_UNDERENHET_UTEN_IA_RETTIGHETER,
                    organizationForm = "BEDR",
                    status = "Active"
                )
            )
        )
        whenever(altinnService.hentVirksomheterDerBrukerHarSykefraværsstatistikkrettighet(any(), any())).thenReturn(
            emptyList()
        )
        val (årstall, kvartal) = SISTE_PUBLISERTE_KVARTAL.minusEttÅr()
        sykefraværStatistikkNæringskodeRepository.settInn(
            listOf(
                SykefraværsstatistikkForNæringskode(
                    årstall = SISTE_PUBLISERTE_KVARTAL.årstall,
                    kvartal = SISTE_PUBLISERTE_KVARTAL.kvartal,
                    næringskode = BARNEHAGER.femsifferIdentifikator,
                    antallPersoner = 10,
                    tapteDagsverk = 5.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal()
                ),
                SykefraværsstatistikkForNæringskode(
                    årstall = årstall,
                    kvartal = kvartal,
                    næringskode = BARNEHAGER.femsifferIdentifikator,
                    antallPersoner = 10,
                    tapteDagsverk = 1.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal(),
                )
            )
        )
        opprettStatistikkForLand(sykefraværStatistikkLandRepository)
        val response = utførAutorisertKall(ORGNR_UNDERENHET_UTEN_IA_RETTIGHETER)
        val responseBody = objectMapper.readTree(response.body())
        val barnehageJson = responseBody["prosentSiste4KvartalerTotalt"][0]
        Assertions.assertThat(barnehageJson["label"].toString()).isEqualTo("\"Barnehager\"")
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
                    .header("AUTHORIZATION", "Bearer $jwtToken")
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
