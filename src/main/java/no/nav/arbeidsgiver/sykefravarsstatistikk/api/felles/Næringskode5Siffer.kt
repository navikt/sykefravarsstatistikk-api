package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles

import com.fasterxml.jackson.annotation.JsonProperty

data class Næringskode5Siffer (
    @JsonProperty("kode")
    private val uvalidertNæringskode: String,
    val beskrivelse: String?
) {
    @get:JsonProperty("kode")
    val kode: String

    init {
        val næringskodeUtenPunktum = uvalidertNæringskode.replace(".", "")
        if (næringskodeUtenPunktum.matches("^[0-9]{5}$".toRegex())) {
            this.kode = næringskodeUtenPunktum
        } else {
            throw RuntimeException("Ugyldig næringskode. Må bestå av 5 siffer.")
        }
    }

    fun hentNæringskode2Siffer(): String {
        return kode.substring(0, 2)
    }
}

/*
{
    "kode": "xxxxx",
    beskrivelse
}

 */