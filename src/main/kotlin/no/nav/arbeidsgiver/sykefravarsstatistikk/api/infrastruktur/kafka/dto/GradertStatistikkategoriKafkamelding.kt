package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.dto

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.SykefraværFlereKvartalerForEksport
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.SykefraværMedKategori

class GradertStatistikkategoriKafkamelding(
    val sisteKvartal: SykefraværMedKategori,
    val siste4Kvartal: SykefraværFlereKvartalerForEksport
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
                    "tapteDagsverkGradert" to sisteKvartal.tapteDagsverk,
                    "tapteDagsverk" to sisteKvartal.muligeDagsverk,
                    "antallPersoner" to sisteKvartal.antallPersoner,
                    "erMaskert" to sisteKvartal.erMaskert
                ),
                "siste4Kvartal" to mapOf(
                    "prosent" to siste4Kvartal.prosent,
                    "tapteDagsverkGradert" to siste4Kvartal.tapteDagsverk,
                    "tapteDagsverk" to siste4Kvartal.muligeDagsverk,
                    "erMaskert" to siste4Kvartal.erMaskert,
                    "kvartaler" to siste4Kvartal.kvartaler
                )
            )
        )
}
