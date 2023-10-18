package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.importering.Orgenhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.ÅrstallOgKvartal

interface KildeTilVirksomhetsdata {
    fun hentVirksomheter(årstallOgKvartal: ÅrstallOgKvartal): List<Orgenhet>
}
