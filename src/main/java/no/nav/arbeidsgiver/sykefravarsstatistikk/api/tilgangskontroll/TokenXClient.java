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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
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

    public TokenXClient(@Value("${tokenxclient.jwk}") String tokenxJwk,
                        @Value("${tokenxclient.clientId}") String tokenxClientId,
                        @Value("${no.nav.security.jwt.issuer.tokenx.discoveryurl}") String tokenxWellKnownUrl, RestTemplate restTemplate) {
        this.tokenxJwk = tokenxJwk;
        this.tokenxClientId = tokenxClientId;
        this.tokenxWellKnownUrl = tokenxWellKnownUrl;
        this.restTemplate = restTemplate;
    }

    // TODO: Burde implementere caching av token fra tokenx
    public JwtToken exchangeTokenToAltinnProxy(JwtToken token) throws ParseException, JOSEException, GeneralException, IOException {
        // Henter metadata
        AuthorizationServerMetadata authorizationServerMetadata = AuthorizationServerMetadata.resolve(new Issuer(tokenxWellKnownUrl));


        // Lag assertion token
        RSAKey jwk = RSAKey.parse(tokenxJwk);
        JWSHeader header = new JWSHeader
                .Builder(JWSAlgorithm.RS256)
                .keyID(jwk.getKeyID())
                .type(JOSEObjectType.JWT)
                .build();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .audience(authorizationServerMetadata.getTokenEndpointURI().toString()) //FIXME
                .jwtID(UUID.randomUUID().toString())
                .subject(tokenxClientId)
                .issuer(tokenxClientId)
                .claim(JWTClaimNames.NOT_BEFORE, Instant.now().minus(1, ChronoUnit.MINUTES).getEpochSecond())
                .claim(JWTClaimNames.ISSUED_AT, Instant.now().getEpochSecond())
                .claim(JWTClaimNames.EXPIRATION_TIME, Instant.now().plus(1, ChronoUnit.HOURS).getEpochSecond())
                .build();
        SignedJWT signedJWT = new SignedJWT(header, claims);
        signedJWT.sign(new RSASSASigner(jwk));
        String assertionToken = signedJWT.serialize();


        // Send request til tokenX
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        TokenExchangeRequest tokenExchangeRequest = new TokenExchangeRequest();
        tokenExchangeRequest.setAudience("dev-gcp:arbeidsgiver:altinn-rettigheter-proxy"); // FIXME
        tokenExchangeRequest.setClient_assertion(assertionToken);
        tokenExchangeRequest.setClient_assertion_type("urn:ietf:params:oauth:client-assertion-type:jwt-bearer");
        tokenExchangeRequest.setSubject_token(token.getTokenAsString());
        tokenExchangeRequest.setSubject_token_type("urn:ietf:params:oauth:token-type:jwt");
        tokenExchangeRequest.setGrant_type("urn:ietf:params:oauth:grant-type:token-exchange");
        HttpEntity<TokenExchangeRequest> tokenExchangeRequestHttpEntity = new HttpEntity<TokenExchangeRequest>(tokenExchangeRequest, httpHeaders);
        TokenExchangeResponse body = restTemplate.postForEntity(authorizationServerMetadata.getTokenEndpointURI(), tokenExchangeRequestHttpEntity, TokenExchangeResponse.class).getBody();
        return new JwtToken(body.access_token);
    }

    private static class TokenExchangeResponse {
        private String access_token;
        private String issued_token_type;
        private String token_type;
        private String expires_in;
    }

    private static class TokenExchangeRequest {
        private String grant_type;
        private String client_assertion_type;
        private String client_assertion;
        private String subject_token_type;
        private String subject_token;
        private String audience;

        public String getGrant_type() {
            return grant_type;
        }

        public void setGrant_type(String grant_type) {
            this.grant_type = grant_type;
        }

        public String getClient_assertion_type() {
            return client_assertion_type;
        }

        public void setClient_assertion_type(String client_assertion_type) {
            this.client_assertion_type = client_assertion_type;
        }

        public String getClient_assertion() {
            return client_assertion;
        }

        public void setClient_assertion(String client_assertion) {
            this.client_assertion = client_assertion;
        }

        public String getSubject_token_type() {
            return subject_token_type;
        }

        public void setSubject_token_type(String subject_token_type) {
            this.subject_token_type = subject_token_type;
        }

        public String getSubject_token() {
            return subject_token;
        }

        public void setSubject_token(String subject_token) {
            this.subject_token = subject_token;
        }

        public String getAudience() {
            return audience;
        }

        public void setAudience(String audience) {
            this.audience = audience;
        }
    }

}
