package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importering.domene.Orgenhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal

interface KildeTilVirksomhetsdata {
    fun hentVirksomheter(årstallOgKvartal: ÅrstallOgKvartal): List<Orgenhet>
}
