package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

data class Næring(
    private val kode: String,
    private val navn: String
) : Virksomhetsklassifikasjon {
    override fun getNavn(): String {
        return navn
    }

    // TODO: Rename til noe annet så vi skjønner at her får vi en næringskode
    override fun getKode(): String {
        return kode
    }

    // TODO: Legg til hent 2-siffer
}