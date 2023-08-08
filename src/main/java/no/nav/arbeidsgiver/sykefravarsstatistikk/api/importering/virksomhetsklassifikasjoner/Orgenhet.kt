package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.virksomhetsklassifikasjoner

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.ÅrstallOgKvartal

data class Orgenhet(
    val orgnr: Orgnr,
    val navn: String?,
    val rectype: String?,
    val sektor: String?,
    val næring: String?,
    val næringskode: String?,
    val årstallOgKvartal: ÅrstallOgKvartal
)