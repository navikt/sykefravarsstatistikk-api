package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.api

import arrow.core.getOrElse
import jakarta.servlet.http.HttpServletRequest
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret.EnhetsregisteretClient
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret.EnhetsregisteretClient.HentEnhetFeil
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.PubliseringsdatoerService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregering.AggregertStatistikkService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregering.KvartalsvisSykefraværshistorikkService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregering.SummertLegemeldtSykefraværService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.TilgangskontrollService
import no.nav.security.token.support.core.api.Protected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@Protected
@RestController
class SykefraværshistorikkController(
    private val kvartalsvisSykefraværshistorikkService: KvartalsvisSykefraværshistorikkService,
    private val tilgangskontrollService: TilgangskontrollService,
    private val enhetsregisteretClient: EnhetsregisteretClient,
    private val aggregertHistorikkService: AggregertStatistikkService,
    private val publiseringsdatoerService: PubliseringsdatoerService,
    private val summertLegemeldtSykefraværService: SummertLegemeldtSykefraværService
) {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping(value = ["/{orgnr}/sykefravarshistorikk/kvartalsvis"])
    fun hentSykefraværshistorikk(
        @PathVariable("orgnr") orgnrStr: String, request: HttpServletRequest
    ): ResponseEntity<List<KvartalsvisSykefraværshistorikk>> {
        val orgnr = Orgnr(orgnrStr)
        val bruker = tilgangskontrollService.hentBrukerKunIaRettigheter()
        tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
            orgnr, bruker, request.method, "" + request.requestURL
        )
        val underenhet: Underenhet.Næringsdrivende = enhetsregisteretClient.hentUnderenhet(orgnr)
            .fold({
                return when (it) {
                    EnhetsregisteretClient.HentUnderenhetFeil.EnhetsregisteretSvarerIkke,
                    EnhetsregisteretClient.HentUnderenhetFeil.FeilVedKallTilEnhetsregisteret,
                    EnhetsregisteretClient.HentUnderenhetFeil.FeilVedDekodingAvJson,
                    EnhetsregisteretClient.HentUnderenhetFeil.OrgnrMatcherIkke ->
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

                }
            },
                {
                    when (it) {
                        is Underenhet.IkkeNæringsdrivende -> return ResponseEntity.ok(emptyList())
                        is Underenhet.Næringsdrivende -> it
                    }
                })
        val overordnetEnhet = enhetsregisteretClient
            .hentEnhet(underenhet.overordnetEnhetOrgnr)
            .getOrElse {
                return when (it) {
                    HentEnhetFeil.FeilVedKallTilEnhetsregisteret,
                    HentEnhetFeil.FeilVedDekodingAvJson,
                    HentEnhetFeil.OrgnrMatcherIkke ->
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                }
            }

        val harTilgangTilOverordnetEnhet =
            tilgangskontrollService.hentTilgangTilOverordnetEnhetOgLoggSikkerhetshendelse(
                bruker, overordnetEnhet, underenhet, request.method, "" + request.requestURL
            )
        return if (harTilgangTilOverordnetEnhet) {
            ResponseEntity.ok(
                kvartalsvisSykefraværshistorikkService.hentSykefraværshistorikk(
                    underenhet, overordnetEnhet
                )
            )
        } else {
            ResponseEntity.ok(
                kvartalsvisSykefraværshistorikkService.hentSykefraværshistorikk(
                    underenhet, overordnetEnhet.institusjonellSektorkode
                )
            )
        }
    }

    // TODO: Fjern etter at MFA har gått over til kafka
    @GetMapping(value = ["/{orgnr}/sykefravarshistorikk/legemeldtsykefravarsprosent"])
    fun hentLegemeldtSykefraværsprosent(
        @PathVariable("orgnr") orgnrStr: String, request: HttpServletRequest
    ): ResponseEntity<LegemeldtSykefraværsprosent?> {
        val orgnr = Orgnr(orgnrStr)
        val brukerMedIaRettigheter = tilgangskontrollService.hentBrukerKunIaRettigheter()
        val brukerMedMinstEnRettighet = tilgangskontrollService.hentInnloggetBrukerForAlleRettigheter()
        val brukerHarSykefraværRettighetTilVirksomhet = brukerMedIaRettigheter.harTilgang(orgnr)
        val brukerRepresentererVirksomheten = tilgangskontrollService.brukerRepresentererVirksomheten(orgnr)
        tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
            Orgnr(orgnrStr),
            brukerMedMinstEnRettighet,
            request.method,
            "" + request.requestURL
        )
        val underenhet: Underenhet.Næringsdrivende = enhetsregisteretClient.hentUnderenhet(orgnr)
            .fold({
                return when (it) {
                    EnhetsregisteretClient.HentUnderenhetFeil.EnhetsregisteretSvarerIkke,
                    EnhetsregisteretClient.HentUnderenhetFeil.FeilVedKallTilEnhetsregisteret,
                    EnhetsregisteretClient.HentUnderenhetFeil.FeilVedDekodingAvJson,
                    EnhetsregisteretClient.HentUnderenhetFeil.OrgnrMatcherIkke ->
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                }
            },
                {
                    when (it) {
                        is Underenhet.IkkeNæringsdrivende -> return ResponseEntity.noContent().build()
                        is Underenhet.Næringsdrivende -> it
                    }
                })

        if (brukerHarSykefraværRettighetTilVirksomhet) {
            val legemeldtSykefraværsprosent = summertLegemeldtSykefraværService.hentLegemeldtSykefraværsprosent(
                underenhet, publiseringsdatoerService.hentSistePubliserteKvartal()
            )
            return if (legemeldtSykefraværsprosent.prosent == null) {
                ResponseEntity.status(HttpStatus.NO_CONTENT).body(null)
            } else ResponseEntity.status(HttpStatus.OK).body(legemeldtSykefraværsprosent)
        } else if (brukerRepresentererVirksomheten) {
            val legemeldtSykefraværsprosentBransjeEllerNæring = summertLegemeldtSykefraværService
                .hentLegemeldtSykefraværsprosentUtenStatistikkForVirksomhet(
                    underenhet, publiseringsdatoerService.hentSistePubliserteKvartal()
                )
            return if (legemeldtSykefraværsprosentBransjeEllerNæring.prosent == null) {
                ResponseEntity.status(HttpStatus.NO_CONTENT).body(null)
            } else ResponseEntity.status(HttpStatus.OK)
                .body(legemeldtSykefraværsprosentBransjeEllerNæring)
        }
        log.error("Brukeren har ikke tilgang til virksomhet, men ble ikke stoppet av tilgangssjekk.")
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)
    }

    @GetMapping("/{orgnr}/v1/sykefravarshistorikk/aggregert")
    fun hentAggregertStatistikk(
        @PathVariable("orgnr") orgnr: String
    ): ResponseEntity<AggregertStatistikkDto> {
        val statistikk = aggregertHistorikkService.hentAggregertStatistikk(Orgnr(orgnr))
            .getOrElse {
                return when (it) {
                    AggregertStatistikkService.HentAggregertStatistikkFeil.BrukerManglerTilgang ->
                        ResponseEntity.status(HttpStatus.FORBIDDEN).build()

                    AggregertStatistikkService.HentAggregertStatistikkFeil.FeilVedKallTilEnhetsregisteret ->
                        ResponseEntity.internalServerError().build()

                    AggregertStatistikkService.HentAggregertStatistikkFeil.UnderenhetErIkkeNæringsdrivende ->
                        ResponseEntity.badRequest().build()
                }
            }
        return ResponseEntity.status(HttpStatus.OK).body(statistikk)
    }
}