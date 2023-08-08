package no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.oauth2.sdk.GeneralException;
import java.io.IOException;
import java.text.ParseException;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.OverordnetEnhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.Virksomhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.altinn.AltinnKlientWrapper;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.sporbarhet.Loggevent;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.sporbarhet.Sporbarhetslogg;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TilgangskontrollService {

  private final AltinnKlientWrapper altinnKlientWrapper;
  private final TilgangskontrollUtils tokenUtils;
  private final Sporbarhetslogg sporbarhetslogg;
  private final String iawebServiceCode;
  private final String iawebServiceEdition;
  private final TokenXClient tokenXClient;

  public TilgangskontrollService(
      AltinnKlientWrapper altinnKlientWrapper,
      TilgangskontrollUtils tokenUtils,
      Sporbarhetslogg sporbarhetslogg,
      @Value("${altinn.iaweb.service.code}") String iawebServiceCode,
      @Value("${altinn.iaweb.service.edition}") String iawebServiceEdition,
      TokenXClient tokenXClient) {
    this.altinnKlientWrapper = altinnKlientWrapper;
    this.tokenUtils = tokenUtils;
    this.sporbarhetslogg = sporbarhetslogg;
    this.iawebServiceCode = iawebServiceCode;
    this.iawebServiceEdition = iawebServiceEdition;
    this.tokenXClient = tokenXClient;
  }

  public InnloggetBruker hentBrukerKunIaRettigheter() {
    InnloggetBruker innloggetBruker = tokenUtils.hentInnloggetBruker();
    try {
      JwtToken exchangedTokenToAltinnProxy =
          tokenXClient.exchangeTokenToAltinnProxy(tokenUtils.hentInnloggetJwtToken());
      innloggetBruker.setBrukerensOrganisasjoner(
          altinnKlientWrapper.hentVirksomheterDerBrukerHarSykefraværsstatistikkrettighet(
              exchangedTokenToAltinnProxy, innloggetBruker.getFnr()));
    } catch (ParseException | JOSEException | GeneralException | IOException | TokenXException e) {
      throw new TilgangskontrollException(e.getMessage());
    }
    return innloggetBruker;
  }

  public boolean brukerHarIaRettigheterIVirksomheten(Orgnr tilOrgnr) {
    return hentBrukerKunIaRettigheter().harTilgang(tilOrgnr);
  }

  public boolean brukerRepresentererVirksomheten(Orgnr orgnr) {
    return hentInnloggetBrukerForAlleRettigheter().harTilgang(orgnr);
  }

  public InnloggetBruker hentInnloggetBrukerForAlleRettigheter() {
    InnloggetBruker innloggetBruker = tokenUtils.hentInnloggetBruker();
    try {
      JwtToken exchangedTokenToAltinnProxy =
          tokenXClient.exchangeTokenToAltinnProxy(tokenUtils.hentInnloggetJwtToken());

      innloggetBruker.setBrukerensOrganisasjoner(
          altinnKlientWrapper.hentVirksomheterDerBrukerHarTilknytning(
              exchangedTokenToAltinnProxy, innloggetBruker.getFnr()));
    } catch (ParseException | JOSEException | GeneralException | IOException | TokenXException e) {
      throw new TilgangskontrollException(e.getMessage());
    }
    return innloggetBruker;
  }

  public boolean hentTilgangTilOverordnetEnhetOgLoggSikkerhetshendelse(
          InnloggetBruker bruker,
          OverordnetEnhet overordnetEnhet,
          Virksomhet underenhet,
          String httpMetode,
          String requestUrl) {
    boolean harTilgang = bruker.harTilgang(overordnetEnhet.getOrgnr());
    String kommentar =
        String.format(
            "Bruker ba om tilgang orgnr %s indirekte ved å kalle endepunktet til underenheten"
                + " %s",
            overordnetEnhet.getOrgnr().getVerdi(), underenhet.getOrgnr().getVerdi());
    sporbarhetslogg.loggHendelse(
        new Loggevent(
            bruker,
            overordnetEnhet.getOrgnr(),
            harTilgang,
            httpMetode,
            requestUrl,
            iawebServiceCode,
            iawebServiceEdition),
        kommentar);

    return harTilgang;
  }

  public void sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
      Orgnr orgnr, String httpMetode, String requestUrl) {
    InnloggetBruker bruker = hentBrukerKunIaRettigheter();
    sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(orgnr, bruker, httpMetode, requestUrl);
  }

  public void sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
      Orgnr orgnr, InnloggetBruker bruker, String httpMetode, String requestUrl) {
    boolean harTilgang = bruker.harTilgang(orgnr);

    sporbarhetslogg.loggHendelse(
        new Loggevent(
            bruker,
            orgnr,
            harTilgang,
            httpMetode,
            requestUrl,
            iawebServiceCode,
            iawebServiceEdition));

    if (!harTilgang) {
      throw new TilgangskontrollException("Har ikke tilgang til statistikk for denne bedriften.");
    }
  }
}
