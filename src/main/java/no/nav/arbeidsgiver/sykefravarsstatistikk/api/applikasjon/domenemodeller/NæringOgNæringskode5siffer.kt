package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

class NæringOgNæringskode5siffer(@JvmField var næring: String, @JvmField var næringskode5Siffer: String) {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as NæringOgNæringskode5siffer
        return if (næring != that.næring) false else næringskode5Siffer == that.næringskode5Siffer
    }

    override fun hashCode(): Int {
        var result = næring.hashCode()
        result = 31 * result + næringskode5Siffer.hashCode()
        return result
    }
}
