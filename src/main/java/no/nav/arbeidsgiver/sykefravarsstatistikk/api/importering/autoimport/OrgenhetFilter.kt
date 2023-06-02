package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.virksomhetsklassifikasjoner.Orgenhet

fun fjernDupliserteOrgnr(orgenheter: List<Orgenhet>): List<Orgenhet> {
    // Noen bedrifter er registrert som flere sektorer, typisk dersom de har endret sektor underveis i et kvartal.
    // I de tilfellene har vi bestemt oss for å velge ut i henhold til prioriteringen 1=statlig, 2=kommunal, 3=privat, 0=ukjent
    val sektorprioritering = listOf("1", "2", "3", "0")

    return orgenheter.sortedBy { sektorprioritering.indexOf(it.sektor) }.distinctBy {it.orgnr}
}
