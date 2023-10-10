package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import java.sql.Date

data class PubliseringsdatoDbDto(
    val rapportPeriode: Int,
    val offentligDato: Date,  // dato for offentliggjøring
    val oppdatertDato: Date,
    val aktivitet: String // beskrivelse, typ "Sykefravær pr 3. kvartal 2022"
) {
    fun sammenlignPubliseringsdatoer(annen: PubliseringsdatoDbDto): Int {
        return this.offentligDato.toLocalDate().compareTo(annen.offentligDato.toLocalDate())
    }
}
