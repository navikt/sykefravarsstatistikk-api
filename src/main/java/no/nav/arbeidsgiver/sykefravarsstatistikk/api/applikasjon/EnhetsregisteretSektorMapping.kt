package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Sektor


private val enhetsregisterSektorkoder = mapOf(
    "1110" to "3",
    "1120" to "3",
    "1510" to "3",
    "1520" to "3",
    "2100" to "3",
    "2300" to "3",
    "2500" to "3",
    "3100" to "1",
    "3200" to "3",
    "3500" to "3",
    "3600" to "3",
    "3900" to "1",
    "4100" to "3",
    "4300" to "3",
    "4500" to "3",
    "4900" to "3",
    "5500" to "3",
    "5700" to "3",
    "6100" to "1",
    "6500" to "2",
    "7000" to "3",
    "8200" to "3",
    "8300" to "3",
    "8500" to "3",
    "9000" to "3"
)

fun fraEnhetsregisteretSektor(fireSifferKode: String) = enhetsregisterSektorkoder[fireSifferKode]?.let { Sektor(it) }
