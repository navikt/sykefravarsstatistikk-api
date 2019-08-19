package no.nav.tag.sykefravarsstatistikk.api.altinn;

public class AltinnException extends RuntimeException {
    AltinnException(String msg, Exception e) {
        super(msg, e);
    }

    public AltinnException(String msg) {
        super(msg);
    }
}
