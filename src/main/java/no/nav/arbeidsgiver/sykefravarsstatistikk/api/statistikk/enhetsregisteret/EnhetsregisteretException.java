package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.enhetsregisteret;

public class EnhetsregisteretException extends RuntimeException {
    EnhetsregisteretException(String msg, Exception e) {
        super(msg, e);
    }
}
