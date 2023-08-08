package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.api

import jakarta.servlet.http.HttpServletRequest
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Underenhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret.EnhetsregisteretClient
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.KvartalsvisSykefraværshistorikk
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregering.OffentligKvartalsvisSykefraværshistorikkService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.TilgangskontrollService
import no.nav.security.token.support.core.api.Protected
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@Protected
@RestController
class OffentligSykefraværshistorikkController(
    private val offentligKvartalsvisSykefraværshistorikkService: OffentligKvartalsvisSykefraværshistorikkService,
    private val tilgangskontrollService: TilgangskontrollService,
    private val enhetsregisteretClient: EnhetsregisteretClient
) {
    // TODO: Finn ut om dette endepunktet er i bruk, og fjern det dersom ikke.
    @GetMapping(value = ["/{orgnr}/v1/offentlig/sykefravarshistorikk/kvartalsvis"])
    fun hentOffentligeSykefraværsprosenter(
        @PathVariable("orgnr") orgnrStr: String?, request: HttpServletRequest
    ): ResponseEntity<List<KvartalsvisSykefraværshistorikk>> {
        val bruker = tilgangskontrollService.hentInnloggetBrukerForAlleRettigheter()
        tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
            Orgnr(orgnrStr!!), bruker, request.method, "" + request.requestURL
        )
        val underenhet = enhetsregisteretClient.hentUnderenhet(Orgnr(orgnrStr))
            .fold({
                return when (it) {
                    EnhetsregisteretClient.HentUnderenhetFeil.EnhetsregisteretSvarerIkke,
                    EnhetsregisteretClient.HentUnderenhetFeil.FeilVedKallTilEnhetsregisteret,
                    EnhetsregisteretClient.HentUnderenhetFeil.FeilVedDekodingAvJson,
                    EnhetsregisteretClient.HentUnderenhetFeil.OrgnrMatcherIkke ->
                        ResponseEntity.internalServerError().build()

                }
            },
                {
                    when (it) {
                        is Underenhet.IkkeNæringsdrivende -> return ResponseEntity.ok(emptyList())
                        is Underenhet.Næringsdrivende -> it
                    }
                }
            )
        return ResponseEntity.ok(
            offentligKvartalsvisSykefraværshistorikkService.hentSykefraværshistorikkV1Offentlig(
                underenhet
            )
        )
    }
}