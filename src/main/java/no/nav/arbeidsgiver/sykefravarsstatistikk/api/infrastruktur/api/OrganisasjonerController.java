package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.api;

import java.util.List;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.altinn.AltinnOrganisasjon;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.InnloggetBruker;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.TilgangskontrollService;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Protected
@RestController
public class OrganisasjonerController {

  private final TilgangskontrollService tilgangskontrollService;

  public OrganisasjonerController(TilgangskontrollService tilgangskontrollService) {
    this.tilgangskontrollService = tilgangskontrollService;
  }

  @GetMapping("/organisasjoner/statistikk")
  public List<AltinnOrganisasjon> hentOrganisasjonerMedStatistikktilgang() {
    InnloggetBruker innloggetBruker = tilgangskontrollService.hentBrukerKunIaRettigheter();
    return innloggetBruker.getBrukerensOrganisasjoner();
  }

  @GetMapping("/organisasjoner")
  public List<AltinnOrganisasjon> hentOrganisasjonerMedAlleTilganger() {
    InnloggetBruker innloggetBruker =
        tilgangskontrollService.hentInnloggetBrukerForAlleRettigheter();
    return innloggetBruker.getBrukerensOrganisasjoner();
  }
}
