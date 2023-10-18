package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene

enum class Sektor(val sektorkode: String, val displaystring: String) {
    STATLIG("1", "Statlig forvaltning"),
    KOMMUNAL("2", "Kommunal forvaltning"),
    PRIVAT("3", "Privat og offentlig næringsvirksomhet"),
    FYLKESKOMMUNAL("9", "Fylkeskommunal forvaltning"),
    UKJENT("0", "Ukjent sektor");

    companion object {
        fun fraSektorkode(kode: String): Sektor? {
            return Sektor.entries.firstOrNull { it.sektorkode == kode }
        }
    }
}