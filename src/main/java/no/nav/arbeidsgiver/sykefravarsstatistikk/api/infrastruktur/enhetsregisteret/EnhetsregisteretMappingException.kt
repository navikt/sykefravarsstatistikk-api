package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret;

public class EnhetsregisteretMappingException extends RuntimeException {
  EnhetsregisteretMappingException(String msg, Exception e) {
    super(msg, e);
  }

  EnhetsregisteretMappingException(String msg) {
    super(msg);
  }
}
