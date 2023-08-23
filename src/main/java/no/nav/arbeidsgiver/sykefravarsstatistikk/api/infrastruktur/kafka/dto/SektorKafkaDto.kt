package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.dto

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Sektor

enum class SektorKafkaDto(val ssbSektorkode: String) {
    STATLIG("1"),
    KOMMUNAL("2"),
    PRIVAT("3"),
    FYLKESKOMMUNAL_FORVALTNING("9"),
    UKJENT("0");

    companion object {
        fun fraDomene(sektor: Sektor): SektorKafkaDto =
            entries.find { it.ssbSektorkode == sektor.sektorkode.kode } ?: UKJENT
    }

}