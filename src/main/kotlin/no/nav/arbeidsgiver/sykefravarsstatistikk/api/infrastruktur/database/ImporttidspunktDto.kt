package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import java.time.LocalDate

data class ImporttidspunktDto(
    val sistImportertTidspunkt: LocalDate,
    val gjeldendePeriode: ÅrstallOgKvartal
)
