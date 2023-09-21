package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.statistikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal

fun interface DeleteSykefraværsstatistikkFunction {
    fun apply(årstallOgKvartal: ÅrstallOgKvartal): Int
}
