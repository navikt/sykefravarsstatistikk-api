package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal

fun interface DeleteSykefraværsstatistikkFunction {
    fun apply(årstallOgKvartal: ÅrstallOgKvartal): Int
}
