package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles

data class Næringskode5Siffer (
    private val uvalidertNæringskode: String,
    val beskrivelse: String?
) {
    val kode: String

    init {
        val næringskodeUtenPunktum = uvalidertNæringskode.replace(".", "")
        if (næringskodeUtenPunktum.matches("^[0-9]{5}$".toRegex())) {
            this.kode = næringskodeUtenPunktum
        } else {
            throw IllegalArgumentException("Ugyldig næringskode. Må bestå av 5 siffer.")
        }
    }

    fun hentNæringskode2Siffer(): String {
        return kode.substring(0, 2)
    }
}