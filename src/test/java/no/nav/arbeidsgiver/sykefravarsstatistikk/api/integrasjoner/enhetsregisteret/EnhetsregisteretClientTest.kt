package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import lombok.SneakyThrows
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.InstitusjonellSektorkode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.OverordnetEnhet
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate

@WireMockTest
class EnhetsregisteretClientTest {
    private val url get() = "http://localhost:${port}/enhetsregisteret/"
    private val restTemplate: RestTemplate = RestTemplate()
    private val enhetsregisteretClient: EnhetsregisteretClient by lazy {
        EnhetsregisteretClient(
            restTemplate, url
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
        val result = enhetsregisteretClient.hentEnhet(Orgnr("999263550"))

        assertThat(result.getOrNull()).isEqualTo(
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
                fail("Skulle feilet")
            }
        )
    }

    @Test
    fun `hentEnhet skal feile hvis et felt mangler i responsen`() {
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

        enhetsregisteretClient.hentEnhet(Orgnr("999263550")).fold(
            {
                assertThat(it).isEqualTo(EnhetsregisteretClient.HentEnhetFeil.FeilVedKallTilEnhetsregisteret)
            },
            {
                fail("Skulle feilet")
            }
        )
    }

    @Test
    @Disabled
    fun hhentEnhet__skal_feile_hvis_returnert_orgnr_ikke_matcher_med_medsendt_orgnr() {
        val responsMedFeilOrgnr = gyldigEnhetRespons("999263550")
        mockRespons(responsMedFeilOrgnr)
        org.junit.jupiter.api.Assertions.assertThrows(
            OrgnrEksistererIkkeException::class.java
        ) { enhetsregisteretClient.hentEnhet(Orgnr("777777777")) }
    }

    @Test
    @Disabled
    fun hentUnderenhet__skal_hente_riktige_felter() {
        mockRespons(gyldigUnderenhetRespons("971800534"))
        val (orgnr, overordnetEnhetOrgnr, navn, næringskode, antallAnsatte) = enhetsregisteretClient.hentUnderenhet(
            Orgnr("971800534")
        )
        assertThat(orgnr.verdi).isEqualTo("971800534")
        assertThat(overordnetEnhetOrgnr!!.verdi).isEqualTo("999263550")
        assertThat(navn).isEqualTo("NAV ARBEID OG YTELSER AVD OSLO")
        assertThat(næringskode!!.kode).isEqualTo("84300")
        assertThat(næringskode.beskrivelse).isEqualTo("Trygdeordninger underlagt offentlig forvaltning")
        assertThat(antallAnsatte).isEqualTo(40)
    }

    @Test
    @Disabled
    fun hentUnderenhet__skal_feile_hvis_server_returnerer_5xx() {
        Mockito.`when`(restTemplate.getForObject(ArgumentMatchers.anyString(), ArgumentMatchers.any<Class<Any>>()))
            .thenThrow(HttpServerErrorException(HttpStatus.BAD_GATEWAY))
        org.junit.jupiter.api.Assertions.assertThrows(
            EnhetsregisteretIkkeTilgjengeligException::class.java
        ) { enhetsregisteretClient.hentUnderenhet(TestData.etOrgnr()) }
    }

    @Test
    @Disabled
    fun hentUnderenhet__skal_feile_hvis_et_felt_mangler() {
        val responsMedManglendeFelt = gyldigUnderenhetRespons("822565212")
        responsMedManglendeFelt.remove("navn")
        mockRespons(responsMedManglendeFelt)
        org.junit.jupiter.api.Assertions.assertThrows(
            EnhetsregisteretMappingException::class.java
        ) { enhetsregisteretClient.hentUnderenhet(TestData.etOrgnr()) }
    }

    @Test
    @Disabled
    fun informasjonOmUnderenhet__skal_feile_hvis_returnert_orgnr_ikke_matcher_med_medsendt_orgnr() {
        val responsMedFeilOrgnr = gyldigUnderenhetRespons("822565212")
        mockRespons(responsMedFeilOrgnr)
        org.junit.jupiter.api.Assertions.assertThrows(
            OrgnrEksistererIkkeException::class.java
        ) { enhetsregisteretClient.hentUnderenhet(Orgnr("777777777")) }
    }

    @SneakyThrows
    private fun mockRespons(node: JsonNode) {
//        Mockito.`when`(
//            restTemplate.getForObject(
//                ArgumentMatchers.anyString(), ArgumentMatchers.any<Class<Any>>(), ArgumentMatchers.anyString()
//            )
//        ).thenReturn(objectMapper.writeValueAsString(node))
    }

    @SneakyThrows
    private fun gyldigUnderenhetRespons(orgnr: String): ObjectNode {
        val str = """{
  "organisasjonsnummer": "$orgnr",
  "navn": "NAV ARBEID OG YTELSER AVD OSLO",
  "naeringskode1": {
    "beskrivelse": "Trygdeordninger underlagt offentlig forvaltning",
    "kode": "84.300"
  },
  "antallAnsatte": 40,
  "overordnetEnhet": "999263550"
}"""
        return objectMapper.readTree(str) as ObjectNode
    }

    @SneakyThrows
    private fun gyldigEnhetRespons(orgnr: String): ObjectNode {
        val str = """{
  "organisasjonsnummer": "$orgnr",
  "navn": "NAV ARBEID OG YTELSER",
  "naeringskode1": {
    "beskrivelse": "Trygdeordninger underlagt offentlig forvaltning",
    "kode": "84.300"
  },
  "institusjonellSektorkode": {
    "kode": "6100",
    "beskrivelse": "Statsforvaltningen"
  },
  "antallAnsatte": 40
}"""
        return objectMapper.readTree(str) as ObjectNode
    }

    companion object {
        private val objectMapper = ObjectMapper()
        private var port = 0

        @BeforeAll
        @JvmStatic
        fun beforeAll(wmRuntimeInfo: WireMockRuntimeInfo) {
            port = wmRuntimeInfo.httpPort
        }
    }
}