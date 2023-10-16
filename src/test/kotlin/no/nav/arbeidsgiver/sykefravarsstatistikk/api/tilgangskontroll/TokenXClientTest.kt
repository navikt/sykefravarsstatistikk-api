package no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll

import com.nimbusds.oauth2.sdk.`as`.AuthorizationServerMetadata
import com.nimbusds.oauth2.sdk.id.Issuer
import io.mockk.every
import io.mockk.mockk
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.TokenService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.tokenx.TokenXClient
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.tokenx.TokenXException
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.core.jwt.JwtToken
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.net.URISyntaxException

class TokenXClientTest {
    private val restTemplate: RestTemplate = mockk()
    val tokenClient: TokenXClient = object : TokenXClient(
        "fakeTokenJwk",
        "fakeTokenXClientId",
        "fakeAlinnRettigheterProxyAudience",
        "fakeTokenXWellKnownUrl",
        restTemplate
    ) {
        override fun getAssertionToken(
            authorizationServerMetadata: AuthorizationServerMetadata
        ): String {
            return "fakeAssertionToken"
        }

        override fun resolveUrlAndGetAuthorizationServerMetadata(
            wellKnownUrl: String
        ): AuthorizationServerMetadata {
            try {
                return AuthorizationServerMetadata(Issuer(URI("fake.uri")))
            } catch (e: URISyntaxException) {
                e.printStackTrace()
                throw e
            }
        }
    }
    private val oAuth2Server = MockOAuth2Server()

    @Test
    fun wellKnownUrl_skal_trimmes_slik_at_resolveUrl_fungerer() {
        assertThat(
            TokenXClient.trimUrl(
                "https://tokendings.dev-gcp.nais.io/.well-known/oauth-authorization-server/"
            )
        ).isEqualTo("https://tokendings.dev-gcp.nais.io/")
        assertThat(
            TokenXClient.trimUrl(
                "https://tokendings.dev-gcp.nais.io/.well-known/oauth-authorization-server"
            )
        ).isEqualTo("https://tokendings.dev-gcp.nais.io")
    }

    @Test
    fun håndterer_400_Bad_Request_Response_fra_TokenX() {
        every {
            restTemplate.postForEntity(any<URI>(), any(), any<Class<Any>>())
        } throws HttpClientErrorException(HttpStatus.BAD_REQUEST, "Some text")
        assertThrows(
            TokenXException::class.java
        ) { tokenClient.exchangeTokenToAltinnProxy(JwtToken(encodedToken)) }
    }

    @Test
    fun la_andre_client_error_response_fra_TokenX_blir_kastet_ut_som_exception() {
        every {
            restTemplate.postForEntity(any<URI>(), any(), any<Class<Any>>())
        } throws HttpClientErrorException(HttpStatus.NOT_FOUND, "Some text")
        assertThrows(
            HttpClientErrorException::class.java
        ) { tokenClient.exchangeTokenToAltinnProxy(JwtToken(encodedToken)) }
    }

    private val encodedToken: String
        get() {
            val fnr = "01010112345"
            return oAuth2Server.issueToken(
                TokenService.ISSUER_TOKENX, fnr, "default", mapOf("pid" to fnr)
            ).serialize()
        }
}
