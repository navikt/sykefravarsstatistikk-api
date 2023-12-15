@file:Suppress("SpringJavaInjectionPointsAutowiringInspection")

package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.api

import arrow.core.right
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import config.SpringIntegrationTestbase
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.altinn.AltinnOrganisasjon
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.altinn.AltinnService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret.EnhetsregisteretClient
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import testUtils.TestTokenUtil.TOKENX_ISSUER_ID
import testUtils.TestTokenUtil.createToken
import testUtils.TestUtils.SISTE_PUBLISERTE_KVARTAL
import testUtils.TestUtils.opprettStatistikkForLand
import testUtils.TestUtils.slettAllStatistikkFraDatabase
import testUtils.TestUtils.slettAlleImporttidspunkt
import java.io.IOException
import java.math.BigDecimal
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.time.LocalDate
import java.util.*
import java.util.stream.Collectors

class ApiEndpointsIntegrationTest : SpringIntegrationTestbase() {
    private val SISTE_ÅRSTALL: Int = SISTE_PUBLISERTE_KVARTAL.årstall
    private val SISTE_KVARTAL: Int = SISTE_PUBLISERTE_KVARTAL.kvartal

    @Autowired
    lateinit var mockOAuth2Server: MockOAuth2Server

    @Autowired
    lateinit var sykefravarStatistikkVirksomhetRepository: SykefravarStatistikkVirksomhetRepository

    @Autowired
    lateinit var sykefraværStatistikkLandRepository: SykefraværStatistikkLandRepository

    @Autowired
    lateinit var sykefraværStatistikkSektorRepository: SykefraværStatistikkSektorRepository

    @Autowired
    lateinit var sykefraværStatistikkNæringRepository: SykefraværStatistikkNæringRepository

    @Autowired
    lateinit var importtidspunktRepository: ImporttidspunktRepository

    @MockBean
    lateinit var altinnService: AltinnService

    @MockBean
    lateinit var enhetsregisteretClient: EnhetsregisteretClient

    @LocalServerPort
    private val port: String? = null
    private val objectMapper = ObjectMapper()

    val PRODUKSJON_NYTELSESMIDLER = Næring("10")

    @BeforeEach
    fun setUp() {
        slettAllStatistikkFraDatabase(
            sykefravarStatistikkVirksomhetRepository = sykefravarStatistikkVirksomhetRepository,
            sykefraværStatistikkSektorRepository = sykefraværStatistikkSektorRepository,
            sykefraværStatistikkNæringRepository = sykefraværStatistikkNæringRepository,
        )
        importtidspunktRepository.slettAlleImporttidspunkt()
        importtidspunktRepository.settInnImporttidspunkt(SISTE_PUBLISERTE_KVARTAL, LocalDate.parse("2022-06-02"))
    }

    @Test
    @Throws(Exception::class)
    fun sykefraværshistorikk__skal_ikke_tillate_selvbetjening_token() {
        val jwtTokenIssuedByLoginservice = createToken(mockOAuth2Server, "15008462396", "selvbetjening", "")
        val respons = gjørKallMotKvartalsvis(ORGNR_UNDERENHET, jwtTokenIssuedByLoginservice)
        Assertions.assertThat(respons.statusCode()).isEqualTo(401)
    }

    @Test
    fun `kvartalsvis skal returnere riktig JSON`() {
        whenever(altinnService.hentVirksomheterDerBrukerHarSykefraværsstatistikkrettighet(any(), any()))
            .thenReturn(
                listOf(
                    AltinnOrganisasjon(
                        name = "NAV ARBEID OG YTELSER AVD OSLO",
                        type = "BEDR",
                        parentOrganizationNumber = ORGNR_OVERORDNET_ENHET,
                        organizationNumber = ORGNR_UNDERENHET,
                        organizationForm = "BEDR",
                        status = "Active"
                    ),
                    AltinnOrganisasjon(
                        name = "NAV ARBEID OG YTELSER",
                        type = "BEDR",
                        parentOrganizationNumber = "",
                        organizationNumber = ORGNR_OVERORDNET_ENHET,
                        organizationForm = "BEDR",
                        status = "Active"
                    )
                )
            )

        whenever(enhetsregisteretClient.hentUnderenhet(any()))
            .thenReturn(
                Underenhet.Næringsdrivende(
                    orgnr = Orgnr(ORGNR_UNDERENHET),
                    overordnetEnhetOrgnr = Orgnr(ORGNR_OVERORDNET_ENHET),
                    navn = "NAV ARBEID OG YTELSER AVD OSLO",
                    næringskode = Næringskode("10000"),
                    antallAnsatte = 10
                ).right()
            )

        whenever(enhetsregisteretClient.hentEnhet(any()))
            .thenReturn(
                OverordnetEnhet(
                    orgnr = Orgnr(ORGNR_OVERORDNET_ENHET),
                    navn = "NAV ARBEID OG YTELSER",
                    næringskode = Næringskode("10000"),
                    sektor = Sektor.STATLIG,
                    antallAnsatte = 10
                ).right()
            )
        val jwtToken = createToken(
            oAuth2Server = mockOAuth2Server,
            pid = "15008462396",
            issuerId = TOKENX_ISSUER_ID,
            idp = "https://oidc.difi.no/idporten-oidc-provider/"
        )
        opprettStatistikkForLand(sykefraværStatistikkLandRepository)

        sykefraværStatistikkSektorRepository.settInn(
            listOf(
                SykefraværsstatistikkSektor(
                    årstall = SISTE_PUBLISERTE_KVARTAL.årstall,
                    kvartal = SISTE_PUBLISERTE_KVARTAL.kvartal,
                    sektorkode = Sektor.STATLIG.sektorkode,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal("657853.346702"),
                    muligeDagsverk = BigDecimal("13558710.866603")
                )
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
                    muligeDagsverk = 100.toBigDecimal(),
                )
            )
        )
        sykefravarStatistikkVirksomhetRepository.settInn(
            listOf(
                SykefraværsstatistikkVirksomhet(
                    årstall = SISTE_ÅRSTALL,
                    kvartal = SISTE_KVARTAL,
                    orgnr = ORGNR_UNDERENHET,
                    tapteDagsverk = 9.toBigDecimal(),
                    muligeDagsverk = 200.toBigDecimal(),
                    antallPersoner = 10,
                    varighet = 'A',
                    rectype = "2",
                )
            )
        )
        sykefravarStatistikkVirksomhetRepository.settInn(
            listOf(
                SykefraværsstatistikkVirksomhet(
                    årstall = SISTE_ÅRSTALL,
                    kvartal = SISTE_KVARTAL,
                    orgnr = ORGNR_OVERORDNET_ENHET,
                    tapteDagsverk = 7.toBigDecimal(),
                    muligeDagsverk = 200.toBigDecimal(),
                    antallPersoner = 10,
                    varighet = 'A',
                    rectype = "1",
                )
            )
        )

        val response = gjørKallMotKvartalsvis(ORGNR_UNDERENHET, "Bearer $jwtToken")

        Assertions.assertThat(response.statusCode()).isEqualTo(200)

        val alleSykefraværshistorikk = objectMapper.readTree(response.body())

        Assertions.assertThat(
            alleSykefraværshistorikk.findValues("type").stream()
                .map { obj: JsonNode -> obj.textValue() }
                .collect(Collectors.toList()))
            .containsExactlyInAnyOrderElementsOf(
                listOf(
                    Statistikkategori.LAND.toString(),
                    Statistikkategori.SEKTOR.toString(),
                    Statistikkategori.NÆRING.toString(),
                    Statistikkategori.VIRKSOMHET.toString(),
                    Statistikkategori.OVERORDNET_ENHET.toString()
                )
            )
        Assertions.assertThat(alleSykefraværshistorikk[0]["label"])
            .isEqualTo(objectMapper.readTree("\"Norge\""))
        Assertions.assertThat(alleSykefraværshistorikk[0]["kvartalsvisSykefraværsprosent"])
            .contains(
                objectMapper.readTree(
                    "{\"prosent\":4.0,\"tapteDagsverk\":4.0,\"muligeDagsverk\":100.0,"
                            + "\"erMaskert\":false,\"årstall\":"
                            + SISTE_ÅRSTALL
                            + ",\"kvartal\":"
                            + SISTE_KVARTAL
                            + "}"
                )
            )
        Assertions.assertThat(alleSykefraværshistorikk[1]["label"])
            .isEqualTo(objectMapper.readTree("\"Statlig forvaltning\""))
        Assertions.assertThat(alleSykefraværshistorikk[1]["kvartalsvisSykefraværsprosent"][0])
            .isEqualTo(
                objectMapper.readTree(
                    "{\"prosent\":4.9,\"tapteDagsverk\":657853.3,\"muligeDagsverk\":1"
                            + ".35587109E7,\"erMaskert\":false,\"årstall\":"
                            + SISTE_ÅRSTALL
                            + ",\"kvartal\":"
                            + SISTE_KVARTAL
                            + "}"
                )
            )
        Assertions.assertThat(alleSykefraværshistorikk[2]["label"])
            .isEqualTo(objectMapper.readTree("\"Produksjon av nærings- og nytelsesmidler\""))
        Assertions.assertThat(alleSykefraværshistorikk[2]["kvartalsvisSykefraværsprosent"][0])
            .isEqualTo(
                objectMapper.readTree(
                    "{\"prosent\":5.0,\"tapteDagsverk\":5.0,\"muligeDagsverk\":100.0,"
                            + "\"erMaskert\":false,\"årstall\":"
                            + SISTE_ÅRSTALL
                            + ",\"kvartal\":"
                            + SISTE_KVARTAL
                            + "}"
                )
            )
        Assertions.assertThat(alleSykefraværshistorikk[3]["label"])
            .isEqualTo(objectMapper.readTree("\"NAV ARBEID OG YTELSER AVD OSLO\""))
        Assertions.assertThat(alleSykefraværshistorikk[3]["kvartalsvisSykefraværsprosent"][0])
            .isEqualTo(
                objectMapper.readTree(
                    "{\"prosent\":4.5,\"tapteDagsverk\":9.0,\"muligeDagsverk\":200.0,"
                            + "\"erMaskert\":false,\"årstall\":"
                            + SISTE_ÅRSTALL
                            + ",\"kvartal\":"
                            + SISTE_KVARTAL
                            + "}"
                )
            )
        Assertions.assertThat(alleSykefraværshistorikk[4]["label"])
            .isEqualTo(objectMapper.readTree("\"NAV ARBEID OG YTELSER\""))
        Assertions.assertThat(alleSykefraværshistorikk[4]["kvartalsvisSykefraværsprosent"][0])
            .isEqualTo(
                objectMapper.readTree(
                    "{\"prosent\":3.5,\"tapteDagsverk\":7.0,\"muligeDagsverk\":200.0,"
                            + "\"erMaskert\":false,\"årstall\":"
                            + SISTE_ÅRSTALL
                            + ",\"kvartal\":"
                            + SISTE_KVARTAL
                            + "}"
                )
            )

    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun sykefraværshistorikk_sektor__skal_utføre_tilgangskontroll() {
        val response = gjørKallMotKvartalsvis(ORGNR_UNDERENHET_INGEN_TILGANG, bearerMedJwt)
        Assertions.assertThat(response.statusCode()).isEqualTo(403)
        Assertions.assertThat(response.body())
            .isEqualTo("{\"message\":\"You don't have access to this resource\"}")
    }

    @Test
    @Throws(Exception::class)
    fun sykefraværshistorikk__skal_IKKE_godkjenne_en_token_uten_sub_eller_pid() {
        val jwtToken = createToken(mockOAuth2Server, "", "", TOKENX_ISSUER_ID, "")
        val response = gjørKallMotKvartalsvis(ORGNR_UNDERENHET_INGEN_TILGANG, jwtToken)
        Assertions.assertThat(response.statusCode()).isEqualTo(401)
        Assertions.assertThat(response.body())
            .isEqualTo("{\"message\":\"You are not authorized to access this resource\"}")
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun gjørKallMotKvartalsvis(orgnr: String, jwtToken: String): HttpResponse<String> {
        val httpRequest = HttpRequest.newBuilder()
            .uri(
                URI.create(
                    "http://127.0.0.1:"
                            + port
                            + "/sykefravarsstatistikk-api/"
                            + orgnr
                            + "/sykefravarshistorikk/kvartalsvis"
                )
            )
            .header("AUTHORIZATION", jwtToken)
            .GET()
            .build()
        return HttpClient.newBuilder()
            .build()
            .send(httpRequest, BodyHandlers.ofString())
    }

    private val bearerMedJwt: String
        get() = ("Bearer "
                + createToken(mockOAuth2Server, "15008462396", TOKENX_ISSUER_ID, ""))

    companion object {
        private const val ORGNR_UNDERENHET = "910969439"
        private const val ORGNR_OVERORDNET_ENHET = "999263550"
        private const val ORGNR_UNDERENHET_INGEN_TILGANG = "777777777"
    }
}
