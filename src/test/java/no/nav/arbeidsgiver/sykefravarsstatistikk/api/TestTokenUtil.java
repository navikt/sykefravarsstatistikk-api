package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import no.nav.security.mock.oauth2.MockOAuth2Server;

import java.util.Collections;

public class TestTokenUtil {


    public static String SELVBETJENING_TOKEN_ISSUER_ID = "selvbetjening";

    public static String createToken(
            MockOAuth2Server oAuth2Server,
            String sub,
            String issuerId,
            String idp,
            String pid
            ) {
        return oAuth2Server.issueToken(
                issuerId,
                sub,
                "someaudience",
                Collections.emptyMap()
                //Map.of("idp", idp)
        ).serialize();
    }
}
