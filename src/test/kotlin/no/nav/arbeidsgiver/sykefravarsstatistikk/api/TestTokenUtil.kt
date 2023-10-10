package no.nav.arbeidsgiver.sykefravarsstatistikk.api

import no.nav.security.mock.oauth2.MockOAuth2Server

object TestTokenUtil {
    @JvmField
    var TOKENX_ISSUER_ID = "tokenx"

    @JvmStatic
    fun createMockIdportenTokenXToken(mockOAuth2Server: MockOAuth2Server): String {
        return createToken(
            mockOAuth2Server,
            "15008462396",
            TOKENX_ISSUER_ID,
            "https://oidc.difi.no/idporten-oidc-provider/"
        )
    }

    @JvmStatic
    fun createToken(
        oAuth2Server: MockOAuth2Server, pid: String?, issuerId: String?, idp: String?
    ): String {
        return createToken(oAuth2Server, pid, "IKKE_I_BRUK_LENGER", issuerId, idp)
    }

    @JvmStatic
    fun createToken(
        oAuth2Server: MockOAuth2Server, pid: String?, sub: String?, issuerId: String?, idp: String?
    ): String {
        val claims = listOf(
            "idp" to idp,
            "pid" to pid,
        ).filter { it.second != null }.toMap() as Map<String, String>
        return oAuth2Server.issueToken(issuerId!!, sub!!, "someaudience", claims).serialize()
    }
}
