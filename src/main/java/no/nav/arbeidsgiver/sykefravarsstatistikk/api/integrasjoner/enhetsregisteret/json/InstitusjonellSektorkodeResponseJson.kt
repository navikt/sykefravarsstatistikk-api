package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.json

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.InstitusjonellSektorkode

data class InstitusjonellSektorkodeResponseJson(
    val kode: String,
    val beskrivelse: String,
) {
    fun toDomain() = InstitusjonellSektorkode(
        kode, beskrivelse
    )
}