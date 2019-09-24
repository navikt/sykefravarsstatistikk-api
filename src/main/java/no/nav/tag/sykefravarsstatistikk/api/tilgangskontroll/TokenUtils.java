package no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll;

import com.nimbusds.jwt.JWTClaimsSet;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.tag.sykefravarsstatistikk.api.domene.Fnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.InnloggetSelvbetjeningBruker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TokenUtils {
    
    final static String ISSUER_ISSO = "isso";
    final static String ISSUER_SELVBETJENING = "selvbetjening";

    private final OIDCRequestContextHolder contextHolder;

    @Autowired
    public TokenUtils(OIDCRequestContextHolder contextHolder) {
        this.contextHolder = contextHolder;
    }


    public InnloggetSelvbetjeningBruker hentInnloggetBruker() {
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


    public InnloggetSelvbetjeningBruker hentInnloggetSelvbetjeningBruker() {
        String fnr = hentClaim(ISSUER_SELVBETJENING, "sub")
                .orElseThrow(() -> new TilgangskontrollException("Finner ikke fodselsnummer til bruker."));
        return new InnloggetSelvbetjeningBruker(new Fnr(fnr));
    }

    private Optional<String> hentClaim(String issuer, String claim) {
        Optional<JWTClaimsSet> claimSet = hentClaimSet(issuer);
        return claimSet.map(jwtClaimsSet -> String.valueOf(jwtClaimsSet.getClaim(claim)));
    }

    private Optional<JWTClaimsSet> hentClaimSet(String issuer) {
        return Optional.ofNullable(contextHolder.getOIDCValidationContext().getClaims(issuer))
                .map(claims -> claims.getClaimSet());
    }

}
