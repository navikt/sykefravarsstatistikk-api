package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.tilgangsstyring

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.tilgangsstyring.Fnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.altinn.AltinnOrganisasjon
import java.util.*
import java.util.stream.Collectors

data class InnloggetBruker(
    val fnr: Fnr,
    var brukerensOrganisasjoner: List<AltinnOrganisasjon> = emptyList()
) {

    fun harTilgang(orgnr: Orgnr): Boolean {
        val orgnumreBrukerHarTilgangTil = brukerensOrganisasjoner.stream()
            .filter { obj: AltinnOrganisasjon? -> Objects.nonNull(obj) }
            .map(AltinnOrganisasjon::organizationNumber)
            .collect(Collectors.toList())
        return orgnumreBrukerHarTilgangTil.contains(orgnr.verdi)
    }
}
