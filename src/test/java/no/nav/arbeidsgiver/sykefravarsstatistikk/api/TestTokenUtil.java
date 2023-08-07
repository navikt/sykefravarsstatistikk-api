package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import java.util.HashMap;
import java.util.Map;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import org.apache.commons.lang3.StringUtils;

public class TestTokenUtil {

  public static String TOKENX_ISSUER_ID = "tokenx";

  public static String createMockIdportenTokenXToken(MockOAuth2Server mockOAuth2Server) {
    return createToken(
        mockOAuth2Server,
        "15008462396",
        TOKENX_ISSUER_ID,
        "https://oidc.difi.no/idporten-oidc-provider/");
  }

  public static String createToken(
      MockOAuth2Server oAuth2Server, String pid, String issuerId, String idp) {

    return createToken(oAuth2Server, pid, "IKKE_I_BRUK_LENGER", issuerId, idp);
  }

  public static String createToken(
      MockOAuth2Server oAuth2Server, String pid, String sub, String issuerId, String idp) {
    Map<String, String> claims = new HashMap<>();

    if (StringUtils.isNoneEmpty(idp)) {
      claims.put("idp", idp);
    }

    if (StringUtils.isNoneEmpty(pid)) {
      claims.put("pid", pid);
    }
    String tokenIssuedByOAuth2Server =
        oAuth2Server.issueToken(issuerId, sub, "someaudience", claims).serialize();
    return tokenIssuedByOAuth2Server;
  }
}
