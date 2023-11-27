package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Sektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.domene.Orgenhet

fun List<Orgenhet>.fjernDupliserteOrgnr(): List<Orgenhet> {
    // Noen bedrifter er registrert som flere sektorer, typisk dersom de har endret sektor underveis i et kvartal.
    // I de tilfellene har vi bestemt oss for å velge ut i henhold til prioriteringen statlig, fylkeskommunal, kommunal, privat, ukjent)
    val sektorprioritering = listOf(Sektor.STATLIG, Sektor.FYLKESKOMMUNAL, Sektor.KOMMUNAL, Sektor.PRIVAT, Sektor.UKJENT)

    return this.sortedBy { sektorprioritering.indexOf(it.sektor) }.distinctBy {it.orgnr}
}
