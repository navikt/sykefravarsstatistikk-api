package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk

import arrow.core.getOrElse
import jakarta.servlet.http.HttpServletRequest
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient.HentEnhetFeil
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.IngenNæringException
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.api.PubliseringsdatoerService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.AggregertStatistikkDto
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.AggregertStatistikkService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis.KvartalsvisSykefraværshistorikk
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis.KvartalsvisSykefraværshistorikkService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SummertLegemeldtSykefraværService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SummertSykefraværService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SummertSykefraværshistorikk
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollException
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService
import no.nav.security.token.support.core.api.Protected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@Protected
@RestController
class SykefraværshistorikkController(
    private val kvartalsvisSykefraværshistorikkService: KvartalsvisSykefraværshistorikkService,
    private val tilgangskontrollService: TilgangskontrollService,
    private val enhetsregisteretClient: EnhetsregisteretClient,
    private val summertSykefraværService: SummertSykefraværService,
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
        val underenhet = enhetsregisteretClient.hentUnderenhet(orgnr)
        val overordnetEnhet = enhetsregisteretClient
            .hentEnhet(underenhet.overordnetEnhetOrgnr!!)
            .getOrElse { _: HentEnhetFeil ->
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
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

    // TODO: Fjern har vi har gått over til "aggregert"-endepunktet
    @GetMapping(value = ["/{orgnr}/sykefravarshistorikk/summert"])
    fun hentSummertKorttidsOgLangtidsfraværV2(
        @PathVariable("orgnr") orgnrStr: String,
        @RequestParam("antallKvartaler") antallKvartaler: Int,
        request: HttpServletRequest
    ): List<SummertSykefraværshistorikk> {
        val bruker = tilgangskontrollService.hentBrukerKunIaRettigheter()
        tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
            Orgnr(orgnrStr), bruker, request.method, "" + request.requestURL
        )
        val underenhet = enhetsregisteretClient.hentUnderenhet(Orgnr(orgnrStr))
        require(antallKvartaler == 4) { "For øyeblikket støtter vi kun summering av 4 kvartaler." }
        val summertSykefraværshistorikkVirksomhet = summertSykefraværService.hentSummertSykefraværshistorikk(
            underenhet, publiseringsdatoerService.hentSistePubliserteKvartal(), antallKvartaler
        )
        val summertSykefraværshistorikkBransjeEllerNæring =
            summertSykefraværService.hentSummertSykefraværshistorikkForBransjeEllerNæring(
                underenhet, antallKvartaler
            )
        return listOf(
            summertSykefraværshistorikkVirksomhet, summertSykefraværshistorikkBransjeEllerNæring
        )
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
        val underenhet: Underenhet = try {
            enhetsregisteretClient.hentUnderenhet(orgnr)
        } catch (e: IngenNæringException) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null)
        }
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
            .getOrElseThrow { e: TilgangskontrollException? -> e }
        return ResponseEntity.status(HttpStatus.OK).body(statistikk)
    }
}