package no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class TilgangskontrollException extends RuntimeException {

    public TilgangskontrollException(String msg) {
        super(msg);
    }
}
