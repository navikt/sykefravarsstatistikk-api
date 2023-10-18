package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.api

import arrow.core.getOrElse
import jakarta.servlet.http.HttpServletRequest
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.TilgangskontrollService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregering.AggregertStatistikkService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregering.KvartalsvisSykefraværshistorikkService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.json.AggregertStatistikkJson
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.json.KvartalsvisSykefraværshistorikkJson
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.Underenhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret.EnhetsregisteretClient
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret.EnhetsregisteretClient.HentEnhetFeil
import no.nav.security.token.support.core.api.Protected
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
) {

    @GetMapping(value = ["/{orgnr}/sykefravarshistorikk/kvartalsvis"])
    fun hentSykefraværshistorikk(
        @PathVariable("orgnr") orgnrStr: String, request: HttpServletRequest
    ): ResponseEntity<List<KvartalsvisSykefraværshistorikkJson>> {
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
                    underenhet, overordnetEnhet.sektor
                )
            )
        }
    }


    @GetMapping("/{orgnr}/v1/sykefravarshistorikk/aggregert")
    fun hentAggregertStatistikk(
        @PathVariable("orgnr") orgnr: String
    ): ResponseEntity<AggregertStatistikkJson> {
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