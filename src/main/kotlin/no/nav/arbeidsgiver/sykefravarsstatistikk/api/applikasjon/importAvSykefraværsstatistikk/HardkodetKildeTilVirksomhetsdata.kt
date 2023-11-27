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
    override fun hentVirksomheter(årstallOgKvartal: ÅrstallOgKvartal): List<Orgenhet> {
        return hentTestvirksomheter(årstallOgKvartal).map { it.first }
    }


    fun hentTestvirksomheter(gjeldendeKVartal: ÅrstallOgKvartal): List<Pair<Orgenhet, Testvirksomhet.VIRKSOMHETSSTØRRELSE>> {
        val csvSchema = CsvSchema.builder()
            .addColumn("orgnr")
            .addColumn("sektor")
            .addColumn("primærnæring")
            .addColumn("størrelse")
            .setUseHeader(true)
            .build()

        val virksomheter = CsvMapper()
            .readerFor(Testvirksomhet::class.java)
            .with(csvSchema)
            .readValues<Testvirksomhet>(this::class.java.getResource("/mock/testvirksomheter.csv"))
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
                årstallOgKvartal = gjeldendeKVartal
            ) to it.størrelse
        }
    }

    data class Testvirksomhet(
        val orgnr: String = "",
        val sektor: String = "",
        val primærnæring: String = "",
        val størrelse: VIRKSOMHETSSTØRRELSE = VIRKSOMHETSSTØRRELSE.LITEN,
    ) {
        enum class VIRKSOMHETSSTØRRELSE {
            STOR,
            MEDIUM,
            LITEN,
            ENORM,
            KNØTT,
        }
    }
}
