package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

data class Næringskode5Siffer(
    val kode: String,
    val beskrivelse: String?
) {
    init {
        if (!kode.matches("^[0-9]{5}$".toRegex())) {
            throw IllegalArgumentException("Ugyldig næringskode. Må bestå av 5 siffer.")
        }
    }

    fun hentNæringskode2Siffer(): String {
        return kode.substring(0, 2)
    }
}