package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importering

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Orgenhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Sektor

fun fjernDupliserteOrgnr(orgenheter: List<Orgenhet>): List<Orgenhet> {
    // Noen bedrifter er registrert som flere sektorer, typisk dersom de har endret sektor underveis i et kvartal.
    // I de tilfellene har vi bestemt oss for å velge ut i henhold til prioriteringen statlig, kommunal, privat, ukjent)
    val sektorprioritering = listOf(Sektor.STATLIG, Sektor.KOMMUNAL, Sektor.PRIVAT, Sektor.UKJENT)

    return orgenheter.sortedBy { sektorprioritering.indexOf(it.sektor) }.distinctBy {it.orgnr}
}
