package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class TilgangskontrollException extends RuntimeException {

  public TilgangskontrollException(String msg) {
    super(msg);
  }
}
