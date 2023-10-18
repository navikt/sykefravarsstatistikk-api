package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.json

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.ÅrstallOgKvartal

data class PubliseringsdatoerJson(
    val sistePubliseringsdato: String? = null,
    val nestePubliseringsdato: String? = null,
    val gjeldendePeriode: ÅrstallOgKvartal? = null
)
