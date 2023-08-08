package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.dto

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.SykefraværFlereKvartalerForEksport
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.SykefraværMedKategori

data class StatistikkategoriKafkamelding(
    private val sisteKvartal: SykefraværMedKategori,
    private val siste4Kvartal: SykefraværFlereKvartalerForEksport,
) : Kafkamelding {

    private val jsonMapper = ObjectMapper()

    override val nøkkel: String
        get() = jsonMapper.writeValueAsString(
            mapOf(
                "kategori" to sisteKvartal.kategori,
                "kode" to sisteKvartal.kode,
                "kvartal" to sisteKvartal.kvartal.toString(),
                "årstall" to sisteKvartal.Årstall.toString()
            )
        )
    override val innhold: String
        get() = jsonMapper.writeValueAsString(
            mapOf(
                "kategori" to sisteKvartal.kategori,
                "kode" to sisteKvartal.kode,
                "sistePubliserteKvartal" to mapOf(
                    "årstall" to sisteKvartal.Årstall,
                    "kvartal" to sisteKvartal.kvartal,
                    "prosent" to sisteKvartal.prosent,
                    "tapteDagsverk" to sisteKvartal.tapteDagsverk,
                    "muligeDagsverk" to sisteKvartal.muligeDagsverk,
                    "antallPersoner" to sisteKvartal.antallPersoner,
                    "erMaskert" to sisteKvartal.erMaskert
                ),
                "siste4Kvartal" to mapOf(
                    "prosent" to siste4Kvartal.prosent,
                    "tapteDagsverk" to siste4Kvartal.tapteDagsverk,
                    "muligeDagsverk" to siste4Kvartal.muligeDagsverk,
                    "erMaskert" to siste4Kvartal.erMaskert,
                    "kvartaler" to siste4Kvartal.kvartaler
                )
            )
        )
}
