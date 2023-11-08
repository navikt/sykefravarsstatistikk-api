package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.dto

import com.fasterxml.jackson.databind.ObjectMapper
import ia.felles.definisjoner.bransjer.Bransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal

data class MetadataVirksomhetKafkamelding(
    val orgnr: String,
    val årstallOgKvartal: ÅrstallOgKvartal,
    val næring: String,
    val bransje: Bransje?,
    val sektor: SektorKafkaDto,
) : Kafkamelding {
    override val nøkkel: String
        get() = ObjectMapper().writeValueAsString(
            mapOf(
                "orgnr" to orgnr,
                "arstall" to årstallOgKvartal.årstall.toString(),
                "kvartal" to årstallOgKvartal.kvartal.toString(),
            )
        )
    override val innhold: String
        get() = ObjectMapper().writeValueAsString(
            mapOf(
                "orgnr" to orgnr,
                "arstall" to årstallOgKvartal.årstall.toString(),
                "kvartal" to årstallOgKvartal.kvartal.toString(),
                "naring" to næring,
                "bransje" to bransje?.name,
                "sektor" to sektor.name
            )
        )
}