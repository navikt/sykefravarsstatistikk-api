package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.utils

import com.google.common.collect.ImmutableSet
import lombok.extern.slf4j.Slf4j
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

@Slf4j
@Component
class TilgangskontrollUtils @Autowired constructor(
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
        val claimsForIssuerSelvbetjening = getClaimsFor(context, ISSUER_SELVBETJENING)
        if (claimsForIssuerSelvbetjening.isPresent) {
            log.debug("Claims kommer fra issuer Selvbetjening (loginservice)")
            return InnloggetBruker(Fnr(getFnrFraClaims(claimsForIssuerSelvbetjening.get())))
        }
        val claimsForIssuerTokenX = getClaimsFor(context, ISSUER_TOKENX)
        if (claimsForIssuerTokenX.isPresent) {
            log.debug("Claims kommer fra issuer TokenX")
            val fnrString = getTokenXFnr(claimsForIssuerTokenX.get())
            return InnloggetBruker(Fnr(fnrString))
        }
        throw TilgangskontrollException(
            String.format(
                "Kan ikke hente innlogget bruker. Finner ikke claims for issuer '%s' eller '%s'",
                ISSUER_SELVBETJENING, ISSUER_TOKENX
            )
        )
    }

    private fun getFnrFraClaims(claimsForIssuerSelvbetjening: JwtTokenClaims): String {
        var fnrFromClaim = ""
        if (claimsForIssuerSelvbetjening.getStringClaim("pid") != null) {
            log.debug("Fnr hentet fra claims 'pid'")
            fnrFromClaim = claimsForIssuerSelvbetjening.getStringClaim("pid")
        } else if (claimsForIssuerSelvbetjening.getStringClaim("sub") != null) {
            log.debug("Fnr hentet fra claims 'sub' skal snart fases ut")
            fnrFromClaim = claimsForIssuerSelvbetjening.getStringClaim("sub")
        }
        return fnrFromClaim
    }

    private fun getClaimsFor(context: TokenValidationContext, issuer: String): Optional<JwtTokenClaims> {
        return if (context.hasTokenFor(issuer)) {
            Optional.of(context.getClaims(issuer))
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
        val idp = claims.getStringClaim("idp")
        return if (idp.matches("^https://oidc.*difi.*\\.no/idporten-oidc-provider/$".toRegex())) {
            claims.getStringClaim("pid")
        } else if (idp.matches("^https://nav(no|test)b2c\\.b2clogin\\.com/.*$".toRegex())) {
            getFnrFraClaims(claims)
        } else if (idp.matches("https://fakedings.dev-gcp.nais.io/fake/idporten".toRegex())
            && Arrays.stream(environment.activeProfiles)
                .noneMatch { profile: String -> profile == "prod" }
        ) {
            claims.getStringClaim("pid")
        } else {
            throw TilgangskontrollException("Ukjent idp fra tokendings")
        }
    }

    companion object {
        const val ISSUER_SELVBETJENING = "selvbetjening"
        const val ISSUER_TOKENX = "tokenx"
        private val VALID_ISSUERS: Set<String> = ImmutableSet.of(ISSUER_TOKENX, ISSUER_SELVBETJENING)
    }
}
