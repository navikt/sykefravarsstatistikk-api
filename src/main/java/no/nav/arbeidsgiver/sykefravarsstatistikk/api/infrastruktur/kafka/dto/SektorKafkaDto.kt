package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.dto

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Sektor

enum class SektorKafkaDto(val ssbSektorkode: String) {
    STATLIG("1"),
    KOMMUNAL("2"),
    PRIVAT("3"),
    FYLKESKOMMUNAL_FORVALTNING("9"),
    UKJENT("0");

    companion object {
        fun fraDomene(domeneSektor: Sektor): SektorKafkaDto =
            when (domeneSektor) {
                Sektor.STATLIG -> STATLIG
                Sektor.KOMMUNAL -> KOMMUNAL
                Sektor.PRIVAT -> PRIVAT
                Sektor.FYLKESKOMMUNAL -> FYLKESKOMMUNAL_FORVALTNING
                Sektor.UKJENT -> UKJENT
            }
    }

}