package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.utils;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Fnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.InnloggetBruker;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.exceptions.TilgangskontrollException;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;

@Slf4j
@Component
public class TilgangskontrollUtils {

  static final String ISSUER_SELVBETJENING = "selvbetjening";
  public static final String ISSUER_TOKENX = "tokenx";

  private static final Set<String> VALID_ISSUERS =
      ImmutableSet.of(ISSUER_TOKENX, ISSUER_SELVBETJENING);
  private final TokenValidationContextHolder contextHolder;
  private final Environment environment;

  @Autowired
  public TilgangskontrollUtils(
      TokenValidationContextHolder contextHolder, Environment environment) {
    this.contextHolder = contextHolder;
    this.environment = environment;
  }

  public JwtToken hentInnloggetJwtToken() {
    return VALID_ISSUERS.stream()
        .map(issuer -> getJwtTokenFor(contextHolder.getTokenValidationContext(), issuer))
        .flatMap(Optional::stream)
        .findFirst()
        .orElseThrow(() -> new TilgangskontrollException(format("Finner ikke gyldig jwt token")));
  }

  public InnloggetBruker hentInnloggetBruker() {
    TokenValidationContext context = contextHolder.getTokenValidationContext();

    Optional<JwtTokenClaims> claimsForIssuerSelvbetjening =
        getClaimsFor(context, ISSUER_SELVBETJENING);

    if (claimsForIssuerSelvbetjening.isPresent()) {
      log.debug("Claims kommer fra issuer Selvbetjening (loginservice)");

      return new InnloggetBruker(new Fnr(getFnrFraClaims(claimsForIssuerSelvbetjening.get())));
    }

    Optional<JwtTokenClaims> claimsForIssuerTokenX = getClaimsFor(context, ISSUER_TOKENX);
    if (claimsForIssuerTokenX.isPresent()) {
      log.debug("Claims kommer fra issuer TokenX");
      String fnrString = getTokenXFnr(claimsForIssuerTokenX.get());
      return new InnloggetBruker(new Fnr(fnrString));
    }

    throw new TilgangskontrollException(
        format(
            "Kan ikke hente innlogget bruker. Finner ikke claims for issuer '%s' eller '%s'",
            ISSUER_SELVBETJENING, ISSUER_TOKENX));
  }

  private String getFnrFraClaims(JwtTokenClaims claimsForIssuerSelvbetjening) {
    String fnrFromClaim = "";
    if (claimsForIssuerSelvbetjening.getStringClaim("pid") != null) {
      log.debug("Fnr hentet fra claims 'pid'");
      fnrFromClaim = claimsForIssuerSelvbetjening.getStringClaim("pid");
    } else if (claimsForIssuerSelvbetjening.getStringClaim("sub") != null) {
      log.debug("Fnr hentet fra claims 'sub' skal snart fases ut");
      fnrFromClaim = claimsForIssuerSelvbetjening.getStringClaim("sub");
    }
    return fnrFromClaim;
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
      return getFnrFraClaims(claims);
    } else if (idp.matches("https://fakedings.dev-gcp.nais.io/fake/idporten")
        && Arrays.stream(environment.getActiveProfiles())
            .noneMatch(profile -> profile.equals("prod"))) {
      return claims.getStringClaim("pid");
    } else {
      throw new TilgangskontrollException("Ukjent idp fra tokendings");
    }
  }
}
