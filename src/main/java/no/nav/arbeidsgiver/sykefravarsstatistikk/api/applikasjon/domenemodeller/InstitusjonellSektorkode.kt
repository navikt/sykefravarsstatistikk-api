package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import com.fasterxml.jackson.annotation.JsonCreator

data class InstitusjonellSektorkode @JsonCreator constructor(
    val kode: String,
    val beskrivelse: String
)
