package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Orgenhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal

interface KildeTilVirksomhetsdata {
    fun hentVirksomheter(årstallOgKvartal: ÅrstallOgKvartal): List<Orgenhet>
}
