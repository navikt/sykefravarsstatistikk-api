package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.json

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer

data class NæringskodeResponseJson(
    val kode: String,
    val beskrivelse: String,
) {
    fun toDomain() = Næringskode5Siffer(
        kode, beskrivelse
    )
}