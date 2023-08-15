package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import lombok.*
import java.sql.Date

@Getter
@EqualsAndHashCode
class PubliseringsdatoDbDto(
    // (sic)
    private val rapportPeriode: Int,
    private val offentligDato: Date,  // dato for offentliggjøring
    private val oppdatertDato: Date,
    private val aktivitet: String // beskrivelse, typ "Sykefravær pr 3. kvartal 2022"
) {
    fun sammenlignPubliseringsdatoer(annen: PubliseringsdatoDbDto): Int {
        return this.getOffentligDato().toLocalDate().compareTo(annen.getOffentligDato().toLocalDate())
    }
}
