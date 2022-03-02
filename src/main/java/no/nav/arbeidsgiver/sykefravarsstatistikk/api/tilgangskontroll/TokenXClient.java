package no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.GeneralException;
import com.nimbusds.oauth2.sdk.as.AuthorizationServerMetadata;
import com.nimbusds.oauth2.sdk.id.Issuer;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
public class TokenXClient {
    private final String tokenxJwk;
    private final String tokenxClientId;
    private final String tokenxWellKnownUrl;
    private final RestTemplate restTemplate;
    private final String altinnRettigheterProxyAudience;
    private static Logger logger = LoggerFactory.getLogger(TokenXClient.class);

    public TokenXClient(@Value("${tokenxclient.jwk}") String tokenxJwk,
                        @Value("${tokenxclient.clientId}") String tokenxClientId,
                        @Value("${tokenxclient.altinn_rettigheter_proxy_audience}") String altinnRettigheterProxyAudience,
                        @Value("${no.nav.security.jwt.issuer.tokenx.discoveryurl}") String tokenxWellKnownUrl,
                        RestTemplate restTemplate
    ) {
        this.tokenxJwk = tokenxJwk;
        this.tokenxClientId = tokenxClientId;
        this.altinnRettigheterProxyAudience = altinnRettigheterProxyAudience;
        this.tokenxWellKnownUrl = tokenxWellKnownUrl;
        this.restTemplate = restTemplate;
    }

    // TODO: Burde implementere caching av token fra tokenx
    public JwtToken exchangeTokenToAltinnProxy(JwtToken token) throws ParseException, JOSEException, GeneralException, IOException {
        // Henter metadata
        String trimmetUrl = trimUrl(tokenxWellKnownUrl);
        AuthorizationServerMetadata authorizationServerMetadata = AuthorizationServerMetadata.resolve(new Issuer(trimmetUrl));

        // Lag assertion token
        RSAKey jwk = RSAKey.parse(tokenxJwk);
        JWSHeader header = new JWSHeader
                .Builder(JWSAlgorithm.RS256)
                .keyID(jwk.getKeyID())
                .type(JOSEObjectType.JWT)
                .build();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .audience(authorizationServerMetadata.getTokenEndpointURI().toString())
                .jwtID(UUID.randomUUID().toString())
                .subject(tokenxClientId)
                .issuer(tokenxClientId)
                .claim(JWTClaimNames.NOT_BEFORE, Instant.now().minus(1, ChronoUnit.MINUTES).getEpochSecond())
                .claim(JWTClaimNames.ISSUED_AT, Instant.now().getEpochSecond())
                .claim(JWTClaimNames.EXPIRATION_TIME, Instant.now().plus(2, ChronoUnit.MINUTES).getEpochSecond())
                .build();
        SignedJWT signedJWT = new SignedJWT(header, claims);
        signedJWT.sign(new RSASSASigner(jwk));
        String assertionToken = signedJWT.serialize();

        // Send request til tokenX
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> parametre = byggParameterMap(assertionToken, token);
        HttpEntity<MultiValueMap<String, String>> tokenExchangeRequestHttpEntity = new HttpEntity<>(parametre, httpHeaders);

        ResponseEntity<TokenExchangeResponse> responseEntity = null;
        try {
            responseEntity = restTemplate.postForEntity(
                    authorizationServerMetadata.getTokenEndpointURI(),
                    tokenExchangeRequestHttpEntity,
                    TokenExchangeResponse.class);
        } catch (HttpClientErrorException httpClientErrorException) {
            if (httpClientErrorException.getStatusCode() == HttpStatus.BAD_REQUEST) {
                logger.warn("Mottok en BAD REQUEST response fra TokenX", httpClientErrorException);
            }
            throw httpClientErrorException;
        }

        TokenExchangeResponse body = responseEntity
                .getBody();
        return new JwtToken(body.access_token);
    }

    public MultiValueMap<String, String> byggParameterMap(String assertionToken, JwtToken token) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("audience", altinnRettigheterProxyAudience);
        map.add("client_assertion", assertionToken);
        map.add("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer");
        map.add("subject_token", token.getTokenAsString());
        map.add("subject_token_type", "urn:ietf:params:oauth:token-type:jwt");
        map.add("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange");

        return map;
    }

    @NotNull
    protected static String trimUrl(String tokenxWellKnownUrl) {
        return tokenxWellKnownUrl.replace("/.well-known/oauth-authorization-server", "");
    }

    private static class TokenExchangeResponse {
        public String access_token;
        public String issued_token_type;
        public String token_type;
        public String expires_in;
    }
}
