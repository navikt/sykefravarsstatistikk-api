package no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret;

public class EnhetsregisteretException extends RuntimeException {
    EnhetsregisteretException(String msg, Exception e) {
        super(msg, e);
    }

    public EnhetsregisteretException(String msg) {
        super(msg);
    }
}
