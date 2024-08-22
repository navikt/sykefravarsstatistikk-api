package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.KildeTilVirksomhetsdata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Sektor.Companion.fraSektorkode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkVirksomhetMedGradering
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.domene.Orgenhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.UsingExposed
import org.jetbrains.exposed.sql.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class DatavarehusAggregertRepositoryV2(
    @param:Qualifier("datavarehusDatabase") override val database: Database,
) : KildeTilVirksomhetsdata, UsingExposed, Table("dt_p.agg_ia_sykefravar_v_2") {
    val årstall = integer("arstall")
    val kvartal = integer("kvartal")
    val orgnr = varchar("orgnr", 9)
    val sektor = varchar("sektor", 1)
    val næring = varchar("naring", 2)
    val næringskode = varchar("naering_kode", 6)
    val primærnæringskode = varchar("primærnæringskode", 6).nullable()
    val rectype = varchar("rectype", 1)
    val tapteDagsverkGradert = double("taptedv_gs")
    val antallPersoner = integer("antpers")
    val tapteDagsverk = double("taptedv")
    val muligeDagsverk = double("mulige_dv")

    fun hentSykefraværsstatistikkVirksomhetMedGradering(årstallOgKvartal: ÅrstallOgKvartal): List<SykefraværsstatistikkVirksomhetMedGradering> {
        return transaction {
            select(
                årstall,
                kvartal,
                orgnr,
                næring,
                næringskode,
                rectype,
                tapteDagsverkGradert.sum(),
                antallPersoner.sum(),
                tapteDagsverk.sum(),
                muligeDagsverk.sum()
            ).where {
                (årstall eq årstallOgKvartal.årstall) and (kvartal eq årstallOgKvartal.kvartal)
            }.groupBy(årstall, kvartal, orgnr, næring, næringskode, rectype).map {
                SykefraværsstatistikkVirksomhetMedGradering(
                    årstall = it[årstall],
                    kvartal = it[kvartal],
                    orgnr = it[orgnr],
                    næring = it[næring],
                    næringkode = it[næringskode],
                    rectype = it[rectype],
                    tapteDagsverkGradertSykemelding = it[tapteDagsverkGradert.sum()]!!.toBigDecimal(),
                    antallPersoner = it[antallPersoner.sum()]!!,
                    tapteDagsverk = it[tapteDagsverk.sum()]!!.toBigDecimal(),
                    muligeDagsverk = it[muligeDagsverk.sum()]!!.toBigDecimal()
                )
            }
        }
    }



    override fun hentVirksomheter(årstallOgKvartal: ÅrstallOgKvartal): List<Orgenhet> {
        return transaction {
            select(
                orgnr, rectype, sektor, primærnæringskode, årstall, kvartal
            ).where {
                (årstall eq årstallOgKvartal.årstall) and
                        (kvartal eq årstallOgKvartal.kvartal) and
                        (orgnr.trim().charLength() eq 9) and
                        (primærnæringskode.isNotNull()) and
                        (primærnæringskode neq "00.000")
            }.map {
                Orgenhet(
                    orgnr = Orgnr(it[orgnr]),
                    navn = "",  // henter ikke lenger navn for virksomheter, da dette ikke er i bruk
                    rectype = it[rectype],
                    sektor = fraSektorkode(it[sektor]),
                    næring = it[primærnæringskode]?.take(2),
                    næringskode = it[primærnæringskode]?.replace(".", ""),
                    årstallOgKvartal = ÅrstallOgKvartal(it[årstall], it[kvartal])
                )
            }
        }
    }
}
