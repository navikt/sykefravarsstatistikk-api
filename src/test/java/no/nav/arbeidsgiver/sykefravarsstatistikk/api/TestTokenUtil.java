package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import no.nav.security.mock.oauth2.MockOAuth2Server;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class TestTokenUtil {


    public static String SELVBETJENING_ISSUER_ID = "selvbetjening";
    public static String TOKENX_ISSUER_ID = "tokenx";

    public static String createToken(
            MockOAuth2Server oAuth2Server,
            String sub,
            String issuerId
    ) {
        return createToken(oAuth2Server, sub, issuerId, null, null);
    }

    public static String createToken(
            MockOAuth2Server oAuth2Server,
            String sub,
            String issuerId,
            String idp,
            String pid
    ) {
        Map<String, String> claims = new HashMap<>();

        if(StringUtils.isNoneEmpty(idp)) {
            claims.put("idp", idp);
        }

        if(StringUtils.isNoneEmpty(pid)) {
            claims.put("pid", pid);
        }
        String tokenIssuedByOAuth2Server = oAuth2Server.issueToken(
                issuerId,
                sub,
                "someaudience",
                claims
        ).serialize();
        return tokenIssuedByOAuth2Server;
    }
}
