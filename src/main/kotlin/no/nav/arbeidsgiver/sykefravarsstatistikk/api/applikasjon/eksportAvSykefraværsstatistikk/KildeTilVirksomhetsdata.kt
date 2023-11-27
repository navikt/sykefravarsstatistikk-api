package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.domene.Orgenhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal

interface KildeTilVirksomhetsdata {
    fun hentTestvirksomheter(årstallOgKvartal: ÅrstallOgKvartal): List<Orgenhet>
}
