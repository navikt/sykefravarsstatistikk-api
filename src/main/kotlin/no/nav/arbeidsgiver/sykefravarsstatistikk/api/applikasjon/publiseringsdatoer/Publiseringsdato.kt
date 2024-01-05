package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoer

import java.time.LocalDate

data class Publiseringsdato(
    val rapportPeriode: Int,
    val offentligDato: LocalDate,  // dato for offentliggjøring
    val oppdatertDato: LocalDate,
)
