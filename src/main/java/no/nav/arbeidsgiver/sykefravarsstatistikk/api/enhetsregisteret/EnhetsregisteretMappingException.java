package no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret;

public class EnhetsregisteretMappingException extends RuntimeException {
    EnhetsregisteretMappingException(String msg, Exception e) {
        super(msg, e);
    }
}
