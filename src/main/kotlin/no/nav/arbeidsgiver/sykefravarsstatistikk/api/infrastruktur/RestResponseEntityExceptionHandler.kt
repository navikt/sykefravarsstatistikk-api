package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur

import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.error.exceptions.AltinnException
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.tilgangsstyring.TilgangskontrollException
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret.EnhetsregisteretIkkeTilgjengeligException
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret.EnhetsregisteretMappingException
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret.IngenNæringException
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret.OrgnrEksistererIkkeException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.nio.file.AccessDeniedException
import java.util.*

@ControllerAdvice
class RestResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {
    @ExceptionHandler(value = [EnhetsregisteretMappingException::class])
    @ResponseBody
    fun handleEnhetsregisteretMappingException(
        e: RuntimeException,
        @Suppress("UNUSED_PARAMETER")
        webRequest: WebRequest?
    ): ResponseEntity<Any> {
        return getResponseEntity(
            e, "Kunne ikke lese informasjon om enheten fra Enhetsregisteret", HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(value = [OrgnrEksistererIkkeException::class])
    @ResponseBody
    fun handleOrgnrEksistererIkkeException(
        e: RuntimeException,
        @Suppress("UNUSED_PARAMETER")
        webRequest: WebRequest?
    ): ResponseEntity<Any> {
        Companion.logger.warn("Orgnr finnes ikke i Enhetsregisteret", e)
        return getResponseEntity(e, "Orgnr finnes ikke i Enhetsregisteret", HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(value = [EnhetsregisteretIkkeTilgjengeligException::class])
    @ResponseBody
    fun handleEnhetsregisteretIkkeTilgjengeligException(
        e: RuntimeException,
        @Suppress("UNUSED_PARAMETER")
        webRequest: WebRequest?
    ): ResponseEntity<Any> {
        Companion.logger.warn(
            "Kunne ikke hente informasjon om enheten fra Enhetsregisteret fordi Enhetsregisteret ikke er tilgjengelig",
            e
        )
        return getResponseEntity(e, "Internal error", HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(value = [IngenNæringException::class])
    @ResponseBody
    fun handleIngenNæringException(
        e: RuntimeException,
        @Suppress("UNUSED_PARAMETER")
        webRequest: WebRequest?
    ): ResponseEntity<Any> {
        Companion.logger.info(
            "Kunne ikke lese informasjon om enheten fra Enhetsregisteret fordi enheten ikke har næring",
            e
        )
        return getResponseEntity(
            e,
            "Internal error",
            HttpStatus.INTERNAL_SERVER_ERROR,
            Collections.singletonMap("causedBy", INGEN_NÆRING)
        )
    }

    @ExceptionHandler(value = [TilgangskontrollException::class])
    @ResponseBody
    fun handleTilgangskontrollException(
        e: RuntimeException,
        @Suppress("UNUSED_PARAMETER")
        webRequest: WebRequest?
    ): ResponseEntity<Any> {
        return getResponseEntity(e, "You don't have access to this resource", HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(value = [JwtTokenUnauthorizedException::class, AccessDeniedException::class])
    @ResponseBody
    fun handleUnauthorizedException(
        e: RuntimeException,
        @Suppress("UNUSED_PARAMETER")
        webRequest: WebRequest?
    ): ResponseEntity<Any> {
        return getResponseEntity(
            e, "You are not authorized to access this resource", HttpStatus.UNAUTHORIZED
        )
    }

    @ExceptionHandler(value = [AltinnException::class])
    @ResponseBody
    fun handleAltinnException(
        e: RuntimeException,
        @Suppress("UNUSED_PARAMETER")
        webRequest: WebRequest?
    ): ResponseEntity<Any> {
        Companion.logger.warn("Feil ved Altinn integrasjon", e)
        return getResponseEntity(e, "Internal error", HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(value = [Exception::class])
    @ResponseBody
    fun handleGenerellException(
        e: RuntimeException,
        @Suppress("UNUSED_PARAMETER")
        webRequest: WebRequest?
    ): ResponseEntity<Any> {
        Companion.logger.error("Uhåndtert feil", e)
        return getResponseEntity(e, "Internal error", HttpStatus.INTERNAL_SERVER_ERROR)
    }

    private fun getResponseEntity(
        e: RuntimeException, melding: String, status: HttpStatus
    ): ResponseEntity<Any> {
        return getResponseEntity(e, melding, status, HashMap())
    }

    private fun getResponseEntity(
        e: RuntimeException, melding: String, status: HttpStatus, bodyTillegg: Map<String, String>
    ): ResponseEntity<Any> {
        val body = HashMap(bodyTillegg)
        body["message"] = melding
        Companion.logger.debug(
            String.format(
                "Returnerer følgende HttpStatus '%s' med melding '%s' pga exception '%s'",
                status.toString(), melding, e.message
            )
        )
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ResponseEntityExceptionHandler::class.java)
        private const val INGEN_NÆRING = "INGEN_NÆRING"
    }
}
