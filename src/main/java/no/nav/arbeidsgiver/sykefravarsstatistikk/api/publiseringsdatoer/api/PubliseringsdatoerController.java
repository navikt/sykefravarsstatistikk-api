package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.api;

import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.DatauthentingFeil;
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
  public Publiseringsdatoer hentPubliseringsdatoInfo(HttpServletRequest request) {

    return publiseringsdatoerService
        .hentPubliseringsdatoer()
        .getOrElseThrow(
            () -> new DatauthentingFeil("Klarte ikke hente publiseringsdatoer, prøv igjen senere"));
  }
}
