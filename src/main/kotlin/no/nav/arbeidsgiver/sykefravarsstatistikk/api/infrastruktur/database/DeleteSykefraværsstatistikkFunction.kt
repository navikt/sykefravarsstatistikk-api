package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.ÅrstallOgKvartal

fun interface DeleteSykefraværsstatistikkFunction {
    fun apply(årstallOgKvartal: ÅrstallOgKvartal): Int
}
