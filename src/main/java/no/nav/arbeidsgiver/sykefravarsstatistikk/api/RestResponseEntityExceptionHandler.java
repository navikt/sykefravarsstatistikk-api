package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.altinn.AltinnException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollException;
import no.nav.security.spring.oidc.validation.interceptor.OIDCUnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private static Logger logger = LoggerFactory.getLogger(ResponseEntityExceptionHandler.class);


    @ExceptionHandler(value = {TilgangskontrollException.class})
    @ResponseBody
    protected ResponseEntity<Object> handleTilgangskontrollException(RuntimeException e, WebRequest webRequest) {
        return getResponseEntity(e, "You don't have access to this ressource", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = {OIDCUnauthorizedException.class, AccessDeniedException.class})
    @ResponseBody
    protected ResponseEntity<Object> handleUnauthorizedException(RuntimeException e, WebRequest webRequest) {
        return getResponseEntity(e, "You are not authorized to access this ressource", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {AltinnException.class})
    @ResponseBody
    protected ResponseEntity<Object> handleAltinnException(RuntimeException e, WebRequest webRequest) {
        logger.warn("Feil ved Altinn integrasjon", e);
        return getResponseEntity(e, "Internal error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {Exception.class})
    @ResponseBody
    protected ResponseEntity<Object> handleGenerellException(RuntimeException e, WebRequest webRequest) {
        logger.error("Uhåndtert feil", e);
        return getResponseEntity(e, "Internal error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Object> getResponseEntity(RuntimeException e, String melding, HttpStatus status) {
        HashMap<String, String> body = new HashMap<>(1);
        body.put("message", melding);
        logger.info(
                String.format(
                        "Returnerer følgende HttpStatus '%s' med melding '%s' pga exception '%s'",
                        status.toString(),
                        melding,
                        e.getMessage()
                )
        );

        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON_UTF8).body(body);
    }

}
