package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoApi

import java.time.LocalDate

data class Publiseringsdato(
    val rapportPeriode: Int,
    val offentligDato: LocalDate,  // dato for offentliggjøring
    val oppdatertDato: LocalDate,
    val aktivitet: String // beskrivelse, typ "Sykefravær pr 3. kvartal 2022"
)
