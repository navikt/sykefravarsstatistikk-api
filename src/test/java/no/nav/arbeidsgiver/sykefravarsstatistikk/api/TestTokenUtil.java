package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import no.nav.security.mock.oauth2.MockOAuth2Server;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class TestTokenUtil {


    public static String SELVBETJENING_ISSUER_ID = "selvbetjening";
    public static String TOKENX_ISSUER_ID = "tokenx";

    /*
       3 use-cases / mulige JWT tokens
        - loginservice (alene): som det fungerer i dag
            sub ---> fnr
            iss ---> "https://navtestb2c.b2clogin.com/
            "aud": "0090b6e1-ffcc-4c37-bc21-049f7d1f0fe5",
            "acr": "Level4",

        - TokenX med exchange av en token fra loginservice (det Altinn proxy bruker)

        - TokenX med exchange av en token fra id-porten (hvor vi skal til)
     */


    /*
       sub: “subject identifier” - an unique identifier for the authenticated user. The value is pairwise, meaning
         a given client will always get the same value, whilst different clients do not get equal values for the same
         user.
       aud: The indended audience for token. Normally the Oauth2 ‘issuer’ URL of the Resource Server / API.
       acr: Level4
       pid: “Personidentifikator” - the Norwegian national ID number (fødselsnummer/d-nummer) of the autenticated
         end user.
       idp: Identity Provider, f.eks "https://oidc.difi.no/idporten-oidc-provider/" | https://nav(no|test)b2c\\.b2clogin\\.com/
       iss: The identifier of ID-porten as can be verified on the .well-known endpoint
     */
    public static String createTokenFraLoginservice(
            MockOAuth2Server oAuth2Server,
            String sub
    ) {
        return createToken(oAuth2Server, sub, SELVBETJENING_ISSUER_ID, null, null);
    }

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
        //System.out.println("---------------> Token:"+tokenIssuedByOAuth2Server);
        return tokenIssuedByOAuth2Server;
    }
}
