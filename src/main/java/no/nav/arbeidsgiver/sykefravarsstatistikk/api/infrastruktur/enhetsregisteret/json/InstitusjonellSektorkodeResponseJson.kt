package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret.json

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.InstitusjonellSektorkode

data class InstitusjonellSektorkodeResponseJson(
    val kode: String,
    val beskrivelse: String,
) {
    fun toDomain() = InstitusjonellSektorkode(
        kode, beskrivelse
    )
}