package no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll;

import com.nimbusds.oauth2.sdk.as.AuthorizationServerMetadata;
import com.nimbusds.oauth2.sdk.id.Issuer;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollUtils.ISSUER_TOKENX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TokenXClientTest {

    @Mock
    private RestTemplate restTemplate;
    private TokenXClient tokenClient;

    private final MockOAuth2Server oAuth2Server = new MockOAuth2Server();


    @BeforeEach
    public void setUp() {
        tokenClient = new TokenXClient(
                "fakeTokenJwk",
                "fakeTokenXClientId",
                "fakeAlinnRettigheterProxyAudience",
                "fakeTokenXWellKnownUrl",
                restTemplate
        ){
            @Override
            protected String getAssertionToken(AuthorizationServerMetadata authorizationServerMetadata) {
                return "fakeAssertionToken";
            }
            @NotNull
            @Override
            protected AuthorizationServerMetadata resolveUrlAndGetAuthorizationServerMetadata(String wellKnownUrl) {
                try {
                    return new AuthorizationServerMetadata(new Issuer(new URI("fake.uri")));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    @Test
    public void wellKnownUrl_skal_trimmes_slik_at_resolveUrl_fungerer() {
        assertThat(
                TokenXClient.trimUrl("https://tokendings.dev-gcp.nais.io/.well-known/oauth-authorization-server/")
        ).isEqualTo("https://tokendings.dev-gcp.nais.io/");

        assertThat(
                TokenXClient.trimUrl("https://tokendings.dev-gcp.nais.io/.well-known/oauth-authorization-server")
        ).isEqualTo("https://tokendings.dev-gcp.nais.io");
    }

    @Test
    public void håndterer_400_Bad_Request_Response_fra_TokenX() {
        when(
                restTemplate.postForEntity(
                        any(),
                        any(),
                        any()
                )
        ).thenThrow(
                new HttpClientErrorException(
                        HttpStatus.BAD_REQUEST,
                        "Some text"
                )
        );

        assertThrows(
                TokenXException.class,
                () -> tokenClient.exchangeTokenToAltinnProxy(new JwtToken(getEncodedToken()))
        );
    }

    @Test
    public void la_andre_client_error_response_fra_TokenX_blir_kastet_ut_som_exception() {
        when(
                restTemplate.postForEntity(
                        any(),
                        any(),
                        any()
                )
        ).thenThrow(
                new HttpClientErrorException(
                        HttpStatus.NOT_FOUND,
                        "Some text"
                )
        );

        assertThrows(
                HttpClientErrorException.class,
                () -> tokenClient.exchangeTokenToAltinnProxy(new JwtToken(getEncodedToken()))
        );
    }


    private String getEncodedToken() {
        String fnr = "01010112345";
        String encodedToken = oAuth2Server.issueToken(
                ISSUER_TOKENX,
                fnr,
                "default",
                Map.of("pid", fnr)
        ).serialize();
        return encodedToken;
    }
}
