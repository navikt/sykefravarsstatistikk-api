package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

// TODO: Use enum
class Sektor(
    kode: String,
) {
    val sektorkode = Sektorkode(kode)
    val navn = sektorer[sektorkode.kode]
}

private val sektorer = mapOf(
    "0" to "Ukjent sektor",
    "1" to "Statlig forvaltning",
    "2" to "Kommunal forvaltning",
    "3" to "Privat og offentlig næringsvirksomhet",
    "9" to "Fylkeskommunal forvaltning"
)

data class Sektorkode(val kode: String) {
    init {
        require(sektorer.containsKey(kode)) { "Sektorkoden '$kode' finnes ikke." }
    }
}


enum class SektorEnum(val sektorkode: String, val displaystring: String) {
    STATLIG("1", "Statlig forvaltning"),
    KOMMUNAL("2", "Kommunal forvaltning"),
    PRIVAT("3", "Privat og offentlig næringsvirksomhet"),
    FYLKESKOMMUNAL("9", "Fylkeskommunal forvaltning"),
    UKJENT("0", "Ukjent sektor");

    companion object {
        fun fraSektorkode(sektorkode: Sektorkode): SektorEnum? {
            TODO()
        }
    }
}