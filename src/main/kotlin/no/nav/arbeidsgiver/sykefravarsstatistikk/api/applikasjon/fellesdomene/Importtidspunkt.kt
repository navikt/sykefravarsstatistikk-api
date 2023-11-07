package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene

import java.time.LocalDate

data class Importtidspunkt(
    val sistImportertTidspunkt: LocalDate,
    val gjeldendePeriode: ÅrstallOgKvartal
)
