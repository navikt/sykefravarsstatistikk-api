package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles

import com.fasterxml.jackson.annotation.JsonValue
import lombok.Value

@Value
class Fnr(verdi: String) {
    val verdi: String

    init {
        if (!erGyldigFnr(verdi)) {
            throw RuntimeException("Ugyldig fødselsnummer. Må bestå av 11 tegn.")
        }
        this.verdi = verdi
    }

    @JsonValue
    fun asString(): String {
        return verdi
    }

    companion object {
        fun erGyldigFnr(fnr: String): Boolean {
            return fnr.matches("^[0-9]{11}$".toRegex())
        }
    }
}