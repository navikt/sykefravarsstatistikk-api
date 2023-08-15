package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

data class Publiseringsdatoer(
    val sistePubliseringsdato: String? = null,
    val nestePubliseringsdato: String? = null,
    val gjeldendePeriode: ÅrstallOgKvartal? = null
)
