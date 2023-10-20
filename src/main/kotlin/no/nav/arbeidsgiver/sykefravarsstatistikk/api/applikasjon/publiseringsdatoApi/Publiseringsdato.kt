package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoApi

import java.sql.Date

data class Publiseringsdato(
    val rapportPeriode: Int,
    val offentligDato: Date,  // dato for offentliggjøring
    val oppdatertDato: Date,
    val aktivitet: String // beskrivelse, typ "Sykefravær pr 3. kvartal 2022"
) {
    fun sammenlignPubliseringsdatoer(annen: Publiseringsdato): Int {
        return this.offentligDato.toLocalDate().compareTo(annen.offentligDato.toLocalDate())
    }
}
