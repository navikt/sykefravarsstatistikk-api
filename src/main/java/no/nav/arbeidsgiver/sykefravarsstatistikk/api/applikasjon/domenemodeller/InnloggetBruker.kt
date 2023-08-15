package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import lombok.Data
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.exceptions.TilgangskontrollException
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.altinn.AltinnOrganisasjon
import java.util.*
import java.util.stream.Collectors

@Data
class InnloggetBruker(private val fnr: Fnr) {
    private val brukerensOrganisasjoner: List<AltinnOrganisasjon>

    init {
        brukerensOrganisasjoner = ArrayList()
    }

    fun sjekkTilgang(orgnr: Orgnr) {
        if (!harTilgang(orgnr)) {
            throw TilgangskontrollException("Har ikke tilgang til statistikk for denne bedriften.")
        }
    }

    fun harTilgang(orgnr: Orgnr): Boolean {
        val orgnumreBrukerHarTilgangTil = brukerensOrganisasjoner.stream()
            .filter { obj: AltinnOrganisasjon? -> Objects.nonNull(obj) }
            .map(AltinnOrganisasjon::organizationNumber)
            .collect(Collectors.toList())
        return orgnumreBrukerHarTilgangTil.contains(orgnr.verdi)
    }
}
