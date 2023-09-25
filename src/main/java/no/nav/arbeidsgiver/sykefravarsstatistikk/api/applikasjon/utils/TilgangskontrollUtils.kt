package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.utils

import com.google.common.collect.ImmutableSet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Fnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.InnloggetBruker
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.exceptions.TilgangskontrollException
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import java.util.*

@Component
open class TilgangskontrollUtils @Autowired constructor(
    private val contextHolder: TokenValidationContextHolder, private val environment: Environment
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun hentInnloggetJwtToken(): JwtToken? {
        return VALID_ISSUERS.stream()
            .map { issuer: String ->
                getJwtTokenFor(
                    contextHolder.tokenValidationContext, issuer
                )
            }
            .flatMap { obj: Optional<JwtToken> -> obj.stream() }
            .findFirst()
            .orElseThrow { TilgangskontrollException(String.format("Finner ikke gyldig jwt token")) }
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

    private fun getJwtTokenFor(context: TokenValidationContext, issuer: String): Optional<JwtToken> {
        return Optional.ofNullable(context.getJwtToken(issuer))
    }

    private fun getTokenXFnr(claims: JwtTokenClaims): String {
        /* NOTE: This is not validation of original issuer. We trust TokenX to only issue
     * tokens from trustworthy sources. The purpose is simply to differentiate different
     * original issuers to extract the fnr. */
        val idp = claims.getStringClaim("idp") ?: throw TilgangskontrollException("Mangler claim 'idp'")
        return if (idp.matches("^https://oidc.*difi.*\\.no/idporten-oidc-provider/$".toRegex()) || idp.matches("""^https://(test\.)?idporten\.no$""".toRegex())) {
            claims.getStringClaim("pid")
        } else if (idp.matches("https://fakedings.dev-gcp.nais.io/fake/idporten".toRegex())
            && environment.activeProfiles.none { it == "prod" }
        ) {
            claims.getStringClaim("pid")
        } else {
            throw TilgangskontrollException("Ukjent idp fra tokendings")
        }
    }

    companion object {
        const val ISSUER_TOKENX = "tokenx"
        private val VALID_ISSUERS: Set<String> = ImmutableSet.of(ISSUER_TOKENX)
    }
}
