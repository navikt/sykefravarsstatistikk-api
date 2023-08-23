package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret.json

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Sektor

data class InstitusjonellSektorkodeResponseJson(
    val kode: String,
    val beskrivelse: String,
) {
    fun toDomain() = Sektor(kode)
}