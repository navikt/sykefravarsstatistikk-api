package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

data class VirksomhetMetadata(
    private val orgnrObj: Orgnr,
    val navn: String,
    val rectype: String,
    val sektor: Sektor,
    val primærnæring: String,
    val primærnæringskode: String,
    val årstallOgKvartal: ÅrstallOgKvartal
) {
    val orgnr: String
        get() = orgnrObj.verdi

    val næringOgNæringskode5siffer: MutableList<BedreNæringskode> = mutableListOf()

    @get:JvmName("getÅrstall")
    val årstall: Int
        get() = årstallOgKvartal.årstall

    val kvartal: Int
        get() = årstallOgKvartal.kvartal

    fun leggTilNæringOgNæringskode5siffer(næringOgNæringskode5siffer: List<BedreNæringskode>?) {
        if (!næringOgNæringskode5siffer.isNullOrEmpty()) {
            this.næringOgNæringskode5siffer.addAll(næringOgNæringskode5siffer)
        }
    }
}

