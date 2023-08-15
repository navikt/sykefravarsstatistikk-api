package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

data class OpprettEllerOppdaterResultat(
    val antallRadOpprettet: Int = 0,
    val antallRadOppdatert: Int = 0
) {
    fun add(other: OpprettEllerOppdaterResultat) = OpprettEllerOppdaterResultat(
        antallRadOpprettet + other.antallRadOpprettet,
        antallRadOppdatert + other.antallRadOppdatert
    )
}
