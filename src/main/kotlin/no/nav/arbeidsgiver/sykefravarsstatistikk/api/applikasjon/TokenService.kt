package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Fnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.InnloggetBruker
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.*

@Component
open class TokenService(
    private val contextHolder: TokenValidationContextHolder,
    private val environment: Environment
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    val ISSUER_TOKENX = "tokenx"

    fun hentInnloggetJwtToken(): JwtToken {
        return contextHolder.tokenValidationContext.getJwtToken(ISSUER_TOKENX)
            ?: throw (TilgangskontrollException(String.format("Finner ikke gyldig jwt token")))
    }

    fun hentInnloggetBruker(): InnloggetBruker {
        val context = contextHolder.tokenValidationContext
        val claims = getClaimsFor(context)
        if (claims.isPresent) {
            log.debug("Claims kommer fra issuer TokenX")
            val fnrString = getTokenXFnr(claims.get())
            return InnloggetBruker(Fnr(fnrString))
        }
        throw TilgangskontrollException(
            "Kan ikke hente innlogget bruker. Finner ikke claims for issuer tokenx"
        )
    }

    private fun getClaimsFor(context: TokenValidationContext): Optional<JwtTokenClaims> {
        return if (context.hasTokenFor(ISSUER_TOKENX)) {
            Optional.of(context.getClaims(ISSUER_TOKENX))
        } else {
            Optional.empty()
        }
    }

    private fun getTokenXFnr(claims: JwtTokenClaims): String {
        /* NOTE: This is not validation of original issuer. We trust TokenX to only issue
     * tokens from trustworthy sources. The purpose is simply to differentiate different
     * original issuers to extract the fnr. */
        val idp = claims.getStringClaim("idp") ?: throw TilgangskontrollException("Mangler claim 'idp'")
        return if (idp.matches("^https://oidc.*difi.*\\.no/idporten-oidc-provider/$".toRegex()) || idp.matches("""^https://(test\.)?idporten\.no$""".toRegex())) {
            claims.getStringClaim("pid")
        } else if (idp.matches("https://fakedings.intern.dev.nav.no/fake/idporten".toRegex())
            && environment.activeProfiles.none { it == "prod" }
        ) {
            claims.getStringClaim("pid")
        } else {
            throw TilgangskontrollException("Ukjent idp fra tokendings")
        }
    }
}

@ResponseStatus(HttpStatus.FORBIDDEN)
class TilgangskontrollException(msg: String) : RuntimeException(msg)
