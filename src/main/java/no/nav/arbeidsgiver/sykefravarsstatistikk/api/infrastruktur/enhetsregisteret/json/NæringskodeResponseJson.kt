package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret.json

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.Næringskode5Siffer

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