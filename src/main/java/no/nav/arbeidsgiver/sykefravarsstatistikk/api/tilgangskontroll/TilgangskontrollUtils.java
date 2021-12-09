package no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll;

import com.google.common.collect.ImmutableSet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Fnr;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;

@Component
public class TilgangskontrollUtils {

    // vi kunne egentlig kalle issuer for 'loginservice', det hadde vært mer riktig
    final static String ISSUER_SELVBETJENING = "selvbetjening";
    final static String ISSUER_TOKENX = "tokenx";

    private final static Set<String> VALID_ISSUERS = ImmutableSet.of(ISSUER_TOKENX, ISSUER_SELVBETJENING);
    private final TokenValidationContextHolder contextHolder;

    @Autowired
    public TilgangskontrollUtils(TokenValidationContextHolder contextHolder) {
        this.contextHolder = contextHolder;
    }


    public JwtToken getIDPortenToken() {
        return VALID_ISSUERS.stream()
                .map(issuer -> getJwtTokenFor(contextHolder.getTokenValidationContext(), issuer))
                .flatMap(Optional::stream)
                .findFirst()
                .orElseThrow(() -> new TilgangskontrollException(format("Finner ikke gyldig jwt token")));
    }

    public InnloggetBruker hentInnloggetBruker() {
        TokenValidationContext context = contextHolder.getTokenValidationContext();

        Optional<JwtTokenClaims> claimsForIssuerSelvbetjening = getClaimsFor(context, ISSUER_SELVBETJENING);
        if (claimsForIssuerSelvbetjening.isPresent()) {
            return new InnloggetBruker(
                    new Fnr(claimsForIssuerSelvbetjening.get().getSubject()));
        }

        Optional<JwtTokenClaims> claimsForIssuerTokenX = getClaimsFor(context, ISSUER_TOKENX);
        if (claimsForIssuerTokenX.isPresent()) {
            String fnrString = getTokenXFnr(claimsForIssuerTokenX.get());
            return new InnloggetBruker(
                    new Fnr(fnrString)
            );
        }

        throw new TilgangskontrollException(
                format(
                        "Kan ikke hente innlogget bruker. Finner ikke claims for issuer '%s' eller '%s'",
                        ISSUER_SELVBETJENING,
                        ISSUER_TOKENX
                )
        );
    }

    private Optional<JwtTokenClaims> getClaimsFor(TokenValidationContext context, String issuer) {
        if (context.hasTokenFor(issuer)) {
            return Optional.of(context.getClaims(issuer));
        } else {
            return Optional.empty();
        }
    }

    private Optional<JwtToken> getJwtTokenFor(TokenValidationContext context, String issuer) {
        return Optional.ofNullable(context.getJwtToken(issuer));
    }

    private String getTokenXFnr(JwtTokenClaims claims) {
        /* NOTE: This is not validation of original issuer. We trust TokenX to only issue
         * tokens from trustworthy sources. The purpose is simply to differentiate different
         * original issuers to extract the fnr. */
        String idp = claims.getStringClaim("idp");

        if (idp.matches("^https://oidc.*difi.*\\.no/idporten-oidc-provider/$")) {
            return claims.getStringClaim("pid");
        } else if (idp.matches("^https://nav(no|test)b2c\\.b2clogin\\.com/.*$")) {
            return claims.getSubject();
        } else {
            throw new TilgangskontrollException("Ukjent idp fra tokendings");
        }
    }

}
