package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.altinn.AltinnException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.enhetsregisteret.EnhetsregisteretIkkeTilgjengeligException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.enhetsregisteret.EnhetsregisteretMappingException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.enhetsregisteret.IngenNæringException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollException;
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private static Logger logger = LoggerFactory.getLogger(ResponseEntityExceptionHandler.class);

    private static final String INGEN_NÆRING = "INGEN_NÆRING";

    @ExceptionHandler(value = {EnhetsregisteretMappingException.class})
    @ResponseBody
    protected ResponseEntity<Object> handleEnhetsregisteretException(RuntimeException e, WebRequest webRequest) {
        return getResponseEntity(e,
                "Kunne ikke lese informasjon om enheten fra Enhetsregisteret",
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {EnhetsregisteretIkkeTilgjengeligException.class})
    @ResponseBody
    protected ResponseEntity<Object> handleEnhetsregisteretIkkeTilgjengeligException(RuntimeException e, WebRequest webRequest) {
        logger.warn("Kunne ikke hente informasjon om enheten fra Enhetsregisteret fordi Enhetsregisteret ikke er tilgjengelig", e);
        return getResponseEntity(e, "Internal error", HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @ExceptionHandler(value = {IngenNæringException.class})
    @ResponseBody
    protected ResponseEntity<Object> handleIngenNæringException(RuntimeException e, WebRequest webRequest) {
        logger.warn("Kunne ikke lese informasjon om enheten fra Enhetsregisteret fordi enheten ikke har næring", e);
        return getResponseEntity(
                e,
                "Internal error",
                HttpStatus.INTERNAL_SERVER_ERROR,
                Collections.singletonMap("causedBy", INGEN_NÆRING)
        );

    }

    @ExceptionHandler(value = {TilgangskontrollException.class})
    @ResponseBody
    protected ResponseEntity<Object> handleTilgangskontrollException(RuntimeException e, WebRequest webRequest) {
        return getResponseEntity(e, "You don't have access to this ressource", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = {JwtTokenUnauthorizedException.class, AccessDeniedException.class})
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
        return getResponseEntity(e, melding, status, new HashMap<>());
    }

    private ResponseEntity<Object> getResponseEntity(RuntimeException e, String melding, HttpStatus status, Map<String, String> bodyTillegg) {
        HashMap<String, String> body = new HashMap<>(bodyTillegg);
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
