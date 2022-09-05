package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class DatauthentingFeil extends RuntimeException {

  public DatauthentingFeil(String message) {
    super(message);
  }
}
