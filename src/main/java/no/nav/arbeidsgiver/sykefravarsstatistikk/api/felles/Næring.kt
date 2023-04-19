package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles

data class Næring(
    private val kode: String,
    private val navn: String
) : Virksomhetsklassifikasjon {
    override fun getNavn(): String {
        return navn
    }

    override fun getKode(): String {
        return kode
    }
}