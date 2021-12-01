package no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Fnr;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static java.lang.String.format;

@Component
public class TilgangskontrollUtils {

    final static String ISSUER_ISSO = "isso";
    final static String ISSUER_SELVBETJENING = "selvbetjening";
    final static String ISSUER_LOGINSERVICE = "loginservice";
    final static String ISSUER_TOKENX = "tokenx";

    private final TokenValidationContextHolder contextHolder;

    @Autowired
    public TilgangskontrollUtils(TokenValidationContextHolder contextHolder) {
        this.contextHolder = contextHolder;
    }

    // TODO: MÅ vi kalle det for selvbetjening idtoken?
    public JwtToken getSelvbetjeningToken() {
        return getJwtTokenFor(contextHolder.getTokenValidationContext(), ISSUER_SELVBETJENING);
    }

    public boolean erInnloggetSelvbetjeningBruker() {
        InnloggetBruker innloggetBruker = hentInnloggetBruker();
        return Fnr.erGyldigFnr(innloggetBruker.getFnr().getVerdi());
    }


    public InnloggetBruker hentInnloggetBruker() {
        TokenValidationContext context = contextHolder.getTokenValidationContext();

        JwtTokenClaims claimsForIssuerSelvbetjening = getClaimsFor(context, ISSUER_SELVBETJENING);
        if (claimsForIssuerSelvbetjening != null) {
            return new InnloggetBruker(
                    new Fnr(claimsForIssuerSelvbetjening.getSubject()));
        }

        JwtTokenClaims claimsForIssuerTokenX = getClaimsFor(context, ISSUER_TOKENX);
        if (claimsForIssuerTokenX != null) {
            String fnrString = getTokenXFnr(claimsForIssuerTokenX);
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

    private JwtTokenClaims getClaimsFor(TokenValidationContext context, String issuer) {
        if (context.hasTokenFor(issuer)) {
            return context.getClaims(issuer);
        } else {
            throw new TilgangskontrollException(format("Finner ikke claims for issuer '%s'", issuer));
        }
    }

    private JwtToken getJwtTokenFor(TokenValidationContext context, String issuer) {
        if (context.hasTokenFor(issuer)) {
            return context.getJwtToken(issuer);
        } else {
            throw new TilgangskontrollException(format("Finner ikke jwt token for issuer '%s'", issuer));
        }
    }

    private String getTokenXFnr(JwtTokenClaims claims) {
        /* NOTE: This is not validation of original issuer. We trust TokenX to only issue
         * tokens from trustworthy sources. The purpose is simply to differentiate different
         * original issuers to extract the fnr. */
        String idp = claims.getStringClaim("idp");

        if (idp.matches("^https://oidc.*difi.*\\.no/idporten-oidc-provider/$")) {
            return claims.getStringClaim("pid");
        } else if (idp.matches("^ https://nav(no|test)b2c\\.b2clogin\\.com/.*$")) {
            return claims.getSubject();
        } else {
            throw new TilgangskontrollException("Ukjent idp fra tokendings");
        }
    }


    // OLD --> støtter bare selvbetjening tokens

    public boolean erInnloggetSelvbetjeningBruker_OLD() {
        return hentClaim(ISSUER_SELVBETJENING, "sub")
                .map(fnrString -> Fnr.erGyldigFnr(fnrString))
                .orElse(false);
    }


    public InnloggetBruker hentInnloggetSelvbetjeningBruker() {
        String fnr = hentClaim(ISSUER_SELVBETJENING, "sub")
                .orElseThrow(() -> new TilgangskontrollException("Finner ikke fodselsnummer til bruker."));
        return new InnloggetBruker(new Fnr(fnr));
    }

    private Optional<String> hentClaim(String issuer, String claim) {
        Optional<JwtTokenClaims> claims = hentClaimSet(issuer);
        return claims.map(jwtClaims -> String.valueOf(jwtClaims.get(claim)));
    }

    private Optional<JwtTokenClaims> hentClaimSet(String issuer) {
        return Optional.ofNullable(contextHolder.getTokenValidationContext().getClaims(issuer));
    }

}
