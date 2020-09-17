package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.integrasjoner.enhetsregisteret;

public class EnhetsregisteretException extends RuntimeException {
    EnhetsregisteretException(String msg, Exception e) {
        super(msg, e);
    }
}
