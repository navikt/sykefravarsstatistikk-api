package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoer

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import java.time.LocalDate

data class PubliseringskalenderDto(
    val sistePubliseringsdato: LocalDate,
    val gjeldendePeriode: ÅrstallOgKvartal,
    val nestePubliseringsdato: LocalDate?,
)
