package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret.json

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.Underenhet

data class UnderenhetResponseJson(
    @JsonProperty("organisasjonsnummer")
    val orgnr: String,
    val navn: String,
    @JsonProperty("naeringskode1")
    val næringskode: NæringskodeResponseJson?,
    val antallAnsatte: Int,
    val overordnetEnhet: String,
) {
    fun toDomain(): Underenhet = if (næringskode != null) {
        Underenhet.Næringsdrivende(
            orgnr = Orgnr(orgnr),
            overordnetEnhetOrgnr = Orgnr(overordnetEnhet),
            navn = navn,
            næringskode = næringskode.toDomain(),
            antallAnsatte = antallAnsatte,
        )
    } else {
        Underenhet.IkkeNæringsdrivende(
            orgnr = Orgnr(orgnr),
            overordnetEnhetOrgnr = Orgnr(overordnetEnhet),
            navn = navn,
            antallAnsatte = antallAnsatte,
        )
    }
}
