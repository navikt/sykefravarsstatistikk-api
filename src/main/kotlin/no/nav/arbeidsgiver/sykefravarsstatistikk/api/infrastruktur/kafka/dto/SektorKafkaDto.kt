package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.dto

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Sektor

enum class SektorKafkaDto {
    STATLIG,
    KOMMUNAL,
    PRIVAT,
    FYLKESKOMMUNAL_FORVALTNING,
    UKJENT;

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