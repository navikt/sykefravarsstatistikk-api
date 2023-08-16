package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import com.fasterxml.jackson.annotation.JsonValue

data class Fnr(val verdi: String) {

    init {
        if (!verdi.matches("^[0-9]{11}$".toRegex())) {
            throw UgyldigFnrException()
        }
    }

    @JsonValue
    fun asString(): String {
        return verdi
    }

    class UgyldigFnrException : RuntimeException("Ugyldig fødselsnummer. Må bestå av 11 tegn.")
}