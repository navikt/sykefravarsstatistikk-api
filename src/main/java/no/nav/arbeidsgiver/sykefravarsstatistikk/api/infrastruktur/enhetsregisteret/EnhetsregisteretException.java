package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret;

public class EnhetsregisteretException extends RuntimeException {

  EnhetsregisteretException(String msg) {
    super(msg);
  }
}
