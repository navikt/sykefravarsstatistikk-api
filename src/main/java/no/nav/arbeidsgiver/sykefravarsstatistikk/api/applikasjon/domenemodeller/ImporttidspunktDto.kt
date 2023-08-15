package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import java.sql.Timestamp
import java.time.LocalDate

data class ImporttidspunktDto(
    val sistImportertTidspunkt: Timestamp,
    val gjeldendePeriode: ÅrstallOgKvartal
) {
    val importertDato: LocalDate
        get() = sistImportertTidspunkt.toLocalDateTime().toLocalDate()
}
