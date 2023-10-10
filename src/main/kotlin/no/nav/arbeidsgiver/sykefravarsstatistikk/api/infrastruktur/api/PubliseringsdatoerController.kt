package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.api

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.PubliseringsdatoerService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Publiseringsdatoer
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.PubliseringsdatoerDatauthentingFeil
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Unprotected
@RestController
class PubliseringsdatoerController(private val publiseringsdatoerService: PubliseringsdatoerService) {
    @GetMapping(value = ["/publiseringsdato"])
    fun hentPubliseringsdatoInfo(): Publiseringsdatoer {
        return publiseringsdatoerService
            .hentPubliseringsdatoer()
            ?: throw PubliseringsdatoerDatauthentingFeil("Klarte ikke hente publiseringsdatoer, prøv igjen senere")
    }
}
