package no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret;

public class EnhetsregisteretException extends RuntimeException {
    EnhetsregisteretException(String msg, Exception e) {
        super(msg, e);
    }
}
