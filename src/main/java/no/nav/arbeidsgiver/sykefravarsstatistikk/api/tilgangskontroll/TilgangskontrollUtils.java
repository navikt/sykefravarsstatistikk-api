package no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Fnr;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TilgangskontrollUtils {

    final static String ISSUER_ISSO = "isso";
    final static String ISSUER_SELVBETJENING = "selvbetjening";

    private final TokenValidationContextHolder contextHolder;

    @Autowired
    public TilgangskontrollUtils(TokenValidationContextHolder contextHolder) {
        this.contextHolder = contextHolder;
    }


    public InnloggetBruker hentInnloggetBruker() {
        if (erInnloggetSelvbetjeningBruker()) {
            return hentInnloggetSelvbetjeningBruker();
        } else {
            throw new TilgangskontrollException("Bruker er ikke innlogget.");
        }
    }

    public boolean erInnloggetSelvbetjeningBruker() {
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

    public JwtToken getSelvbetjeningToken() {
        return contextHolder.getTokenValidationContext().getJwtToken(ISSUER_SELVBETJENING);
    }
}
