package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import java.sql.Timestamp
import java.time.LocalDate

data class ImporttidspunktDto(
    val sistImportertTidspunkt: Timestamp,
    val gjeldendePeriode: ÅrstallOgKvartal
) {
    val importertDato: LocalDate
        get() = sistImportertTidspunkt.toLocalDateTime().toLocalDate()
}
