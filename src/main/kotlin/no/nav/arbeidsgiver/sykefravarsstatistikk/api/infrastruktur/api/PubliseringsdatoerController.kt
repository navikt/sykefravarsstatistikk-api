package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.api

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoer.PubliseringsdatoerService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoer.Publiseringsdatoer
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Unprotected
@RestController
class PubliseringsdatoerController(private val publiseringsdatoerService: PubliseringsdatoerService) {
    @GetMapping(value = ["/publiseringsdato"])
    fun hentPubliseringsdatoer(): ResponseEntity<Any> {

        val publiseringsdatoer = publiseringsdatoerService.hentPubliseringsdatoer()
            ?: return ResponseEntity(
                "Klarte ikke hente publiseringsdatoer, prøv igjen senere",
                HttpStatus.NOT_FOUND
            )

        return ResponseEntity.ok(PubliseringdatoerJson.fraDomene(publiseringsdatoer))
    }
}

data class PubliseringdatoerJson(
    val sistePubliseringsdato: String,
    val nestePubliseringsdato: String,
    val gjeldendePeriode: ÅrstallOgKvartal
) {
    companion object {
        fun fraDomene(domene: Publiseringsdatoer) =
            PubliseringdatoerJson(
                sistePubliseringsdato = domene.sistePubliseringsdato.toString(),
                nestePubliseringsdato = domene.nestePubliseringsdato?.toString()
                    ?: "Neste publiseringsdato er utilgjengelig",
                gjeldendePeriode = domene.gjeldendePeriode
            )
    }
}