package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.KildeTilVirksomhetsdata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Sektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.domene.Orgenhet
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("dev")
@Primary
object HardkodetKildeTilVirksomhetsdata : KildeTilVirksomhetsdata {
    override fun hentTestvirksomheter(årstallOgKvartal: ÅrstallOgKvartal): List<Orgenhet> {
        val csvSchema = CsvSchema.builder()
            .addColumn("orgnr")
            .addColumn("sektor")
            .addColumn("primærnæring")
            .setUseHeader(true)
            .build()

        val virksomheter = CsvMapper()
            .readerFor(DevVirksomhet::class.java)
            .with(csvSchema)
            .readValues<DevVirksomhet>(this::class.java.getResource("/mock/testvirksomheter.csv"))
            .readAll()
            .toList()

        return virksomheter.map {
            val næringskode = it.primærnæring.replace(".", "")
            Orgenhet(
                orgnr = Orgnr(it.orgnr),
                navn = "Virksomhet ${it.orgnr}",
                rectype = "2",
                sektor = Sektor.fraSektorkode(it.sektor),
                næring = næringskode.take(2),
                næringskode = næringskode,
                årstallOgKvartal = årstallOgKvartal
            )
        }
    }

    data class DevVirksomhet(
        val orgnr: String = "",
        val sektor: String = "",
        val primærnæring: String = "",
    )
}
