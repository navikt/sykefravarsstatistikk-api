package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.integrasjoner.enhetsregisteret;

public class EnhetsregisteretIkkeTilgjengeligException extends RuntimeException {
    EnhetsregisteretIkkeTilgjengeligException(String msg, Exception e) {
        super(msg, e);
    }
}
