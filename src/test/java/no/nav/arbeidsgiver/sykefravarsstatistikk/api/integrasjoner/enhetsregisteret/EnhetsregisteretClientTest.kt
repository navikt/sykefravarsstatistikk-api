package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret

import arrow.core.left
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import common.StaticAppenderExtension
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.reactive.function.client.WebClient

@WireMockTest
@ExtendWith(StaticAppenderExtension::class)
class EnhetsregisteretClientTest {
    private val url get() = "http://localhost:${port}/enhetsregisteret"
    private val webClient = WebClient.create()
    private val enhetsregisteretClient: EnhetsregisteretClient by lazy {
        EnhetsregisteretClient(
            webClient, url
        )
    }

    @Test
    fun `hent informasjon om enhet skal hente riktig felter`() {
        stubFor(
            get("/enhetsregisteret/enheter/999263550").willReturn(
                okJson(
                    """
                    {
                      "organisasjonsnummer": "999263550",
                      "navn": "NAV ARBEID OG YTELSER",
                      "naeringskode1": {
                        "kode": "84300",
                        "beskrivelse": "Trygdeordninger underlagt offentlig forvaltning"
                      },
                      "institusjonellSektorkode": {
                        "kode": "6100",
                        "beskrivelse": "Statsforvaltningen"
                      },
                      "antallAnsatte": 40
                    }
                    """.trimIndent()
                )
            )
        )
        val overordnetEnhet = enhetsregisteretClient.hentEnhet(Orgnr("999263550"))

        assertThat(overordnetEnhet.getOrNull()).isEqualTo(
            OverordnetEnhet(
                orgnr = Orgnr("999263550"),
                navn = "NAV ARBEID OG YTELSER",
                næringskode = Næringskode5Siffer("84300", "Trygdeordninger underlagt offentlig forvaltning"),
                institusjonellSektorkode = InstitusjonellSektorkode("6100", "Statsforvaltningen"),
                antallAnsatte = 40
            )
        )
    }

    @Test
    fun `hent enhet skal feile hvis server returnerer 5xx`() {
        stubFor(
            get("/enhetsregisteret/enheter/999263550").willReturn(
                serverError()
            )
        )
        enhetsregisteretClient.hentEnhet(Orgnr("999263550")).fold(
            {
                assertThat(it).isEqualTo(EnhetsregisteretClient.HentEnhetFeil.FeilVedKallTilEnhetsregisteret)
            },
            {
                fail("Skulle returnert left")
            }
        )
    }

    @Test
    fun `hent enhet skal feile hvis et felt mangler i responsen`() {
        stubFor(
            get("/enhetsregisteret/enheter/999263550").willReturn(
                okJson(
                    """
                    {
                      "organisasjonsnummer": "999263550",
                      "navn": "NAV ARBEID OG YTELSER",
                      "naeringskode1": {
                        "kode": "84300",
                        "beskrivelse": "Trygdeordninger underlagt offentlig forvaltning"
                      },
                      "antallAnsatte": 40
                    }
                    """.trimIndent()
                )
            )
        )

        val result = enhetsregisteretClient.hentEnhet(Orgnr("999263550"))
        assertThat(result).isEqualTo(EnhetsregisteretClient.HentEnhetFeil.FeilVedDekodingAvJson.left())
    }

    @Test
    fun `hent enhet skal feile hvis returnert orgnr ikke matcher med medsendt orgnr`() {
        stubFor(
            get("/enhetsregisteret/enheter/777777777").willReturn(
                okJson(
                    """
                    {
                    "organisasjonsnummer": "999263550",
                    "navn": "NAV ARBEID OG YTELSER",
                    "naeringskode1": {
                        "kode": "84300",
                        "beskrivelse": "Trygdeordninger underlagt offentlig forvaltning"
                    },
                    "institusjonellSektorkode": {
                        "kode": "6100",
                        "beskrivelse": "Statsforvaltningen"
                    },
                    "antallAnsatte": 40
                    }
                    """.trimIndent()
                )
            )
        )
        enhetsregisteretClient.hentEnhet(Orgnr("777777777")).fold(
            {
                assertThat(it).isEqualTo(EnhetsregisteretClient.HentEnhetFeil.OrgnrMatcherIkke)
            },
            {
                fail("Skulle feilet")
            }
        )
    }

    @Test
    fun `hent underenheter skal hente riktige felter`() {
        stubFor(
            get("/enhetsregisteret/underenheter/971800534").willReturn(
                okJson(
                    """
                    {
                      "organisasjonsnummer": "971800534",
                      "navn": "NAV ARBEID OG YTELSER AVD OSLO",
                      "overordnetEnhet": "999263550",
                      "naeringskode1": {
                        "kode": "84300",
                        "beskrivelse": "Trygdeordninger underlagt offentlig forvaltning"
                      },
                      "antallAnsatte": 40
                    }
                    """.trimIndent()
                )
            )
        )

        val underenhet = enhetsregisteretClient.hentUnderenhet(Orgnr("971800534"))
        assertThat(underenhet.getOrNull()).isEqualTo(
            Underenhet.Næringsdrivende(
                orgnr = Orgnr("971800534"),
                navn = "NAV ARBEID OG YTELSER AVD OSLO",
                overordnetEnhetOrgnr = Orgnr("999263550"),
                næringskode = Næringskode5Siffer("84300", "Trygdeordninger underlagt offentlig forvaltning"),
                antallAnsatte = 40
            )
        )
    }

    @Test
    fun `hent underenhet skal feile hvis enhetsregisteret returnerer 5xx`() {
        stubFor(
            get("/enhetsregisteret/underenheter/971800534").willReturn(
                serverError()
            )
        )

        val result = enhetsregisteretClient.hentUnderenhet(Orgnr("971800534"))
        assertThat(result).isEqualTo(EnhetsregisteretClient.HentUnderenhetFeil.EnhetsregisteretSvarerIkke.left())
    }

    @Test
    fun `hent underenhet skal feile hvis ett felt mangler`() {
        stubFor(
            get("/enhetsregisteret/underenheter/971800534").willReturn(
                okJson(
                    """
                    {
                      "navn": "NAV ARBEID OG YTELSER AVD OSLO",
                      "overordnetEnhet": "999263550",
                      "naeringskode1": {
                        "kode": "84300",
                        "beskrivelse": "Trygdeordninger underlagt offentlig forvaltning"
                      }
                    }
                    """.trimIndent()
                )
            )
        )

        val result = enhetsregisteretClient.hentUnderenhet(Orgnr("971800534"))
        assertThat(result).isEqualTo(EnhetsregisteretClient.HentUnderenhetFeil.FeilVedDekodingAvJson.left())
    }

    @Test
    fun informasjonOmUnderenhet__skal_feile_hvis_returnert_orgnr_ikke_matcher_med_medsendt_orgnr() {
        stubFor(
            get("/enhetsregisteret/underenheter/777777777").willReturn(
                okJson(
                    """
                    {
                      "organisasjonsnummer": "971800534",
                      "navn": "NAV ARBEID OG YTELSER AVD OSLO",
                      "overordnetEnhet": "999263550",
                      "naeringskode1": {
                        "kode": "84300",
                        "beskrivelse": "Trygdeordninger underlagt offentlig forvaltning"
                      },
                      "antallAnsatte": 40
                    }
                    """.trimIndent()
                )
            )
        )
        enhetsregisteretClient.hentUnderenhet(Orgnr("777777777")).fold(
            {
                assertThat(it).isEqualTo(EnhetsregisteretClient.HentUnderenhetFeil.OrgnrMatcherIkke)
            },
            {
                fail("Skulle feilet")
            }
        )
    }

    companion object {
        private var port = 0

        @BeforeAll
        @JvmStatic
        fun beforeAll(wmRuntimeInfo: WireMockRuntimeInfo) {
            port = wmRuntimeInfo.httpPort
        }
    }
}