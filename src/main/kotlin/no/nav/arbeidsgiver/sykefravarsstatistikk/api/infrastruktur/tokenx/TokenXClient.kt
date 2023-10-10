package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.tokenx

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimNames
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.oauth2.sdk.GeneralException
import com.nimbusds.oauth2.sdk.`as`.AuthorizationServerMetadata
import com.nimbusds.oauth2.sdk.id.Issuer
import no.nav.security.token.support.core.jwt.JwtToken
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.io.IOException
import java.text.ParseException
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Component
open class TokenXClient(
    @param:Value("\${tokenxclient.jwk}") private val tokenxJwk: String,
    @param:Value("\${tokenxclient.clientId}") private val tokenxClientId: String,
    @param:Value("\${tokenxclient.altinn_rettigheter_proxy_audience}") private val altinnRettigheterProxyAudience: String,
    @param:Value("\${no.nav.security.jwt.issuer.tokenx.discoveryurl}") private val tokenxWellKnownUrl: String,
    private val restTemplate: RestTemplate
) {
    @Throws(
        ParseException::class,
        JOSEException::class,
        GeneralException::class,
        IOException::class,
        TokenXException::class
    )
    fun exchangeTokenToAltinnProxy(token: JwtToken): JwtToken {
        val authorizationServerMetadata: AuthorizationServerMetadata = resolveUrlAndGetAuthorizationServerMetadata(
            tokenxWellKnownUrl
        )
        val assertionToken = getAssertionToken(authorizationServerMetadata)
        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_FORM_URLENCODED
        val parametre: MultiValueMap<String, String> = byggParameterMap(assertionToken, token)
        val tokenExchangeRequestHttpEntity: HttpEntity<MultiValueMap<String, String>> =
            HttpEntity<MultiValueMap<String, String>>(parametre, httpHeaders)
        val responseEntity: ResponseEntity<TokenExchangeResponse>
        try {
            responseEntity = restTemplate.postForEntity(
                authorizationServerMetadata.tokenEndpointURI,
                tokenExchangeRequestHttpEntity,
                TokenExchangeResponse::class.java
            )
        } catch (httpClientErrorException: HttpClientErrorException) {
            if (httpClientErrorException.statusCode === HttpStatus.BAD_REQUEST) {
                logger.warn("Mottok en BAD REQUEST response fra TokenX", httpClientErrorException)
                throw TokenXException(httpClientErrorException.message)
            }
            throw httpClientErrorException
        }
        val body: TokenExchangeResponse = responseEntity.body
        return JwtToken(body.access_token)
    }

    @Throws(GeneralException::class, IOException::class)
    protected open fun resolveUrlAndGetAuthorizationServerMetadata(
        wellKnownUrl: String
    ): AuthorizationServerMetadata {
        val trimmetUrl = trimUrl(wellKnownUrl)
        return AuthorizationServerMetadata.resolve(Issuer(trimmetUrl))
    }

    @Throws(ParseException::class, JOSEException::class)
    protected open fun getAssertionToken(authorizationServerMetadata: AuthorizationServerMetadata): String {
        val jwk = RSAKey.parse(tokenxJwk)
        val header: JWSHeader = JWSHeader.Builder(JWSAlgorithm.RS256)
            .keyID(jwk.keyID)
            .type(JOSEObjectType.JWT)
            .build()
        val claims: JWTClaimsSet = JWTClaimsSet.Builder()
            .audience(authorizationServerMetadata.tokenEndpointURI.toString())
            .jwtID(UUID.randomUUID().toString())
            .subject(tokenxClientId)
            .issuer(tokenxClientId)
            .claim(
                JWTClaimNames.NOT_BEFORE,
                Instant.now().minus(1, ChronoUnit.MINUTES).epochSecond
            )
            .claim(JWTClaimNames.ISSUED_AT, Instant.now().epochSecond)
            .claim(
                JWTClaimNames.EXPIRATION_TIME,
                Instant.now().plus(2, ChronoUnit.MINUTES).epochSecond
            )
            .build()
        val signedJWT = SignedJWT(header, claims)
        signedJWT.sign(RSASSASigner(jwk))
        return signedJWT.serialize()
    }

    private fun byggParameterMap(assertionToken: String, token: JwtToken): MultiValueMap<String, String> {
        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        map.add("audience", altinnRettigheterProxyAudience)
        map.add("client_assertion", assertionToken)
        map.add("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
        map.add("subject_token", token.tokenAsString)
        map.add("subject_token_type", "urn:ietf:params:oauth:token-type:jwt")
        map.add("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange")
        return map
    }

    private class TokenExchangeResponse {
        var access_token: String? = null
        var issued_token_type: String? = null
        var token_type: String? = null
        var expires_in: String? = null
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TokenXClient::class.java)
        fun trimUrl(tokenxWellKnownUrl: String): String {
            return tokenxWellKnownUrl.replace("/.well-known/oauth-authorization-server", "")
        }
    }
}
