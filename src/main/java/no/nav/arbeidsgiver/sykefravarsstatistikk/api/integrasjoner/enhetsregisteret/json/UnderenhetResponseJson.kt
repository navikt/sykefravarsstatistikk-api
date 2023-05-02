package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.json

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet

data class UnderenhetResponseJson(
    @JsonProperty("organisasjonsnummer")
    val orgnr: String,
    val navn: String,
    @JsonProperty("naeringskode1")
    val næringskode: NæringskodeResponseJson,
    val antallAnsatte: Int,
    val overordnetEnhet: String,
) {
    fun toDomain() = Underenhet(
        orgnr = Orgnr(orgnr),
        navn = navn,
        næringskode = næringskode.toDomain(),
        antallAnsatte = antallAnsatte,
        overordnetEnhetOrgnr = Orgnr(overordnetEnhet),
    )
}
