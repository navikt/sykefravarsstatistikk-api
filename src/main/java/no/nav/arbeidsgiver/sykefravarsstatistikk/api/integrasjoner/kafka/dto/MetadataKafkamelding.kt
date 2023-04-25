package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.ArbeidsmiljøportalenBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal

data class MetadataKafkamelding(
    val orgnr: String,
    val årstallOgKvartal: ÅrstallOgKvartal,
    val næringskode: String,
    val bransje: ArbeidsmiljøportalenBransje,
    val sektor: Sektor,
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
                "naringskode" to næringskode,
                "bransje" to bransje.name,
                "sektor" to sektor.name
            )
        )
}