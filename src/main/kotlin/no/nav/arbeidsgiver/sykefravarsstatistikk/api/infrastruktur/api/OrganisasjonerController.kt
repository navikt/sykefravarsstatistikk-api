package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.api

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.tilgangsstyring.TilgangskontrollService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.altinn.AltinnOrganisasjon
import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Protected
@RestController
class OrganisasjonerController(private val tilgangskontrollService: TilgangskontrollService) {
    @GetMapping("/organisasjoner/statistikk")
    fun hentOrganisasjonerMedStatistikktilgang(): List<AltinnOrganisasjon> {
        val (_, brukerensOrganisasjoner) = tilgangskontrollService.hentBrukerKunIaRettigheter()
        return brukerensOrganisasjoner
    }

    @GetMapping("/organisasjoner")
    fun hentOrganisasjonerMedAlleTilganger(): List<AltinnOrganisasjon> {
        val (_, brukerensOrganisasjoner) = tilgangskontrollService.hentInnloggetBrukerForAlleRettigheter()
        return brukerensOrganisasjoner
    }
}
