package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.json

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.Næringskode5Siffer

/**
 * @property kode kommer på følgende format fra Enhetsregisteret: "52.292"
 */
data class NæringskodeResponseJson(
    val kode: String,
    val beskrivelse: String,
) {
    fun toDomain() = Næringskode5Siffer(
        kode = kode.replace(".", ""),
        beskrivelse = beskrivelse
    )
}