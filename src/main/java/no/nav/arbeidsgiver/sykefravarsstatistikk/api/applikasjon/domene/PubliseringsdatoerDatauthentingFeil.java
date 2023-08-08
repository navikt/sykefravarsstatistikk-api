package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class PubliseringsdatoerDatauthentingFeil extends RuntimeException {

  public PubliseringsdatoerDatauthentingFeil(String message) {
    super(message);
  }
}
