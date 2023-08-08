package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret.json

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.OverordnetEnhet

data class OverordnetEnhetResponseJson(
    @JsonProperty("organisasjonsnummer")
    val orgnr: String,
    val navn: String,
    @JsonProperty("naeringskode1")
    val næringskode: NæringskodeResponseJson,
    val institusjonellSektorkode: InstitusjonellSektorkodeResponseJson,
    val antallAnsatte: Int,
) {

    fun toDomain() = OverordnetEnhet(
        orgnr = Orgnr(orgnr),
        navn = navn,
        næringskode = næringskode.toDomain(),
        institusjonellSektorkode = institusjonellSektorkode.toDomain(),
        antallAnsatte = antallAnsatte,
    )
}

