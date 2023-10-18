package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoApi

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal

data class PubliseringsdatoerJson(
    val sistePubliseringsdato: String? = null,
    val nestePubliseringsdato: String? = null,
    val gjeldendePeriode: ÅrstallOgKvartal? = null
)
