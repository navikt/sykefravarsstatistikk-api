package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import lombok.EqualsAndHashCode
import lombok.Getter
import java.sql.Timestamp
import java.time.LocalDate

@Getter
@EqualsAndHashCode
class ImporttidspunktDto(
    private val sistImportertTidspunkt: Timestamp,
    private val gjeldendePeriode: ÅrstallOgKvartal
) {
    val importertDato: LocalDate
        get() = sistImportertTidspunkt.toLocalDateTime().toLocalDate()
}
