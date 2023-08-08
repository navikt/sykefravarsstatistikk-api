package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell

import com.fasterxml.jackson.annotation.JsonValue
import lombok.Value

@Value
class Fnr(val verdi: String) {

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