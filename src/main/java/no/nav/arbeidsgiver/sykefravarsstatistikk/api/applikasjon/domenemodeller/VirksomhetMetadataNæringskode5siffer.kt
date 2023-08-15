package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

class VirksomhetMetadataNæringskode5siffer(
    private val orgnr: Orgnr,
    private val årstallOgKvartal: ÅrstallOgKvartal,
    private val næringOgNæringskode5siffer: NæringOgNæringskode5siffer
) {
    fun getOrgnr(): String {
        return orgnr.verdi
    }

    val Årstall: Int
        get() = årstallOgKvartal.årstall
    val kvartal: Int
        get() = årstallOgKvartal.kvartal
    val næring: String?
        get() = næringOgNæringskode5siffer.næring
    val næringskode5siffer: String?
        get() = næringOgNæringskode5siffer.næringskode5Siffer

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as VirksomhetMetadataNæringskode5siffer
        if (orgnr != that.orgnr) return false
        return if (årstallOgKvartal != that.årstallOgKvartal) false else næringOgNæringskode5siffer == that.næringOgNæringskode5siffer
    }

    override fun hashCode(): Int {
        var result = orgnr.hashCode()
        result = 31 * result + årstallOgKvartal.hashCode()
        result = 31 * result + næringOgNæringskode5siffer.hashCode()
        return result
    }
}
