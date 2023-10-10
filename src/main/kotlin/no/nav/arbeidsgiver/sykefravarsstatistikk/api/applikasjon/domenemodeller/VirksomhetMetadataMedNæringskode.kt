package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

class VirksomhetMetadataMedNæringskode(
    private val orgnr: Orgnr,
    private val årstallOgKvartal: ÅrstallOgKvartal,
    private val næringskode: Næringskode
) {
    fun getOrgnr(): String {
        return orgnr.verdi
    }

    val Årstall: Int
        get() = årstallOgKvartal.årstall
    val kvartal: Int
        get() = årstallOgKvartal.kvartal
    val næring: String
        get() = næringskode.næring.tosifferIdentifikator
    val næringskode5siffer: String
        get() = næringskode.femsifferIdentifikator

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as VirksomhetMetadataMedNæringskode
        if (orgnr != that.orgnr) return false
        return if (årstallOgKvartal != that.årstallOgKvartal) false else næringskode == that.næringskode
    }

    override fun hashCode(): Int {
        var result = orgnr.hashCode()
        result = 31 * result + årstallOgKvartal.hashCode()
        result = 31 * result + næringskode.hashCode()
        return result
    }
}
