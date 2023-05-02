package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles

data class Orgnr(val verdi: String) {
    override fun toString(): String {
        return verdi
    }
}