package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret;

public class EnhetsregisteretMappingException extends RuntimeException {
  EnhetsregisteretMappingException(String msg, Exception e) {
    super(msg, e);
  }

  EnhetsregisteretMappingException(String msg) {
    super(msg);
  }
}
