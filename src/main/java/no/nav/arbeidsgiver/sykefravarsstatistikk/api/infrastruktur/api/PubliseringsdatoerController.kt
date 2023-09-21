package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.api;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.PubliseringsdatoerService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Publiseringsdatoer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.PubliseringsdatoerDatauthentingFeil;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Unprotected
@RestController
public class PubliseringsdatoerController {

  private final PubliseringsdatoerService publiseringsdatoerService;

  public PubliseringsdatoerController(PubliseringsdatoerService publiseringsdatoerService) {
    this.publiseringsdatoerService = publiseringsdatoerService;
  }

  @GetMapping(value = "/publiseringsdato")
  public Publiseringsdatoer hentPubliseringsdatoInfo() {

    return publiseringsdatoerService
        .hentPubliseringsdatoer()
        .getOrElseThrow(
            () -> new PubliseringsdatoerDatauthentingFeil("Klarte ikke hente publiseringsdatoer, prøv igjen senere"));
  }
}
