package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.integrasjoner.enhetsregisteret;

public class EnhetsregisteretIkkeTilgjengeligException extends RuntimeException {
    EnhetsregisteretIkkeTilgjengeligException(String msg, Exception e) {
        super(msg, e);
    }
}
