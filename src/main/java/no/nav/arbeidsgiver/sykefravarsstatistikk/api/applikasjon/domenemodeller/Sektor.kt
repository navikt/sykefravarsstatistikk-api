package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

data class Sektor(
    override val kode: String,
    override val navn: String
) : Virksomhetsklassifikasjon
