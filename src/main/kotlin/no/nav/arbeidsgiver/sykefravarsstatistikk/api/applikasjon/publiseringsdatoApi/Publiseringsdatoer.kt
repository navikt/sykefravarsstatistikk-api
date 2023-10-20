package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoApi

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import java.time.LocalDate

data class Publiseringsdatoer(
    val sistePubliseringsdato: LocalDate,
    val gjeldendePeriode: ÅrstallOgKvartal,
    val nestePubliseringsdato: LocalDate?,
)
