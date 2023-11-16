package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkLand
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkSektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.UsingExposed
import org.jetbrains.exposed.sql.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class DatavarehusLandRespository(
    @param:Qualifier("datavarehusDatabase")
    override val database: Database
) : UsingExposed, Table("dt_p.agg_ia_sykefravar_land_v") {
    val årstall = integer("arstall")
    val kvartal = integer("kvartal")
    val antallPersoner = integer("antpers")
    val tapteDagsverk = double("taptedv")
    val muligeDagsverk = double("muligedv")
    val sektor = varchar("sektor", 1)
    val kjønn = varchar("kjonn", 1)
    val næring = varchar("naring", 2)

    val alder = varchar("alder", 1)
    val fylke = varchar("fylkbo", 2)
    val varighet = varchar("varighet", 1)

    fun hentSisteKvartal(): ÅrstallOgKvartal {
        return transaction {
            slice(årstall, kvartal)
                .selectAll()
                .orderBy(årstall to SortOrder.DESC, kvartal to SortOrder.DESC)
                .limit(1).map {
                    ÅrstallOgKvartal(
                        årstall = it[årstall],
                        kvartal = it[kvartal]
                    )
                }.first()
        }
    }

    fun hentFor(årstallOgKvartal: ÅrstallOgKvartal): List<SykefraværsstatistikkLand> {
        return transaction {
            slice(
                årstall, kvartal, antallPersoner.sum(), tapteDagsverk.sum(), muligeDagsverk.sum()
            ).select {
                (kjønn neq "X") and (næring neq "X") and
                        (årstall eq årstallOgKvartal.årstall) and (kvartal eq årstallOgKvartal.kvartal)
            }.groupBy(
                årstall, kvartal
            ).map {
                SykefraværsstatistikkLand(
                    årstall = it[årstall],
                    kvartal = it[kvartal],
                    antallPersoner = it[antallPersoner.sum()]!!,
                    tapteDagsverk = it[tapteDagsverk.sum()]!!.toBigDecimal(),
                    muligeDagsverk = it[muligeDagsverk.sum()]!!.toBigDecimal()
                )
            }
        }
    }

    fun hentSykefraværsstatistikkSektor(
        årstallOgKvartal: ÅrstallOgKvartal,
    ): List<SykefraværsstatistikkSektor> {
        return transaction {
            slice(
                årstall, kvartal, sektor, antallPersoner.sum(), tapteDagsverk.sum(), muligeDagsverk.sum()
            ).select {
                (kjønn neq "X") and (næring neq "X") and
                        (årstall eq årstallOgKvartal.årstall) and (kvartal eq årstallOgKvartal.kvartal)
            }.groupBy(
                årstall, kvartal, sektor
            ).map {
                SykefraværsstatistikkSektor(
                    årstall = it[årstall],
                    kvartal = it[kvartal],
                    antallPersoner = it[antallPersoner.sum()]!!,
                    tapteDagsverk = it[tapteDagsverk.sum()]!!.toBigDecimal(),
                    muligeDagsverk = it[muligeDagsverk.sum()]!!.toBigDecimal(),
                    sektorkode = it[sektor],
                )
            }
        }
    }
}