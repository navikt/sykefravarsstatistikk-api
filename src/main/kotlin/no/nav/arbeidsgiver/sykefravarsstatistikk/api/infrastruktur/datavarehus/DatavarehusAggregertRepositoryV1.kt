package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkVirksomhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.UsingExposed
import org.jetbrains.exposed.sql.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class DatavarehusAggregertRepositoryV1(
    @param:Qualifier("datavarehusDatabase") override val database: Database
) : UsingExposed, Table("dt_p.agg_ia_sykefravar_v") {
    val årstall = integer("arstall")
    val kvartal = integer("kvartal")
    val orgnr = char("orgnr", 9)
    val næringskode = varchar("naering_kode", 5)
    val alder = varchar("alder", 1)
    val kjønn = varchar("kjonn", 1)
    val fylkbo = varchar("fylkbo", 2)
    val sftype = varchar("sftype", 1)
    val varighet = varchar("varighet", 1)
    val sektor = varchar("sektor", 1)
    val størrelse = varchar("storrelse", 1)
    val fylkarb = varchar("fylkarb", 2)
    val tapteDagsverk = double("taptedv")
    val muligeDagsverk = double("muligedv")
    val antallPersoner = integer("antpers")
    val rectype = varchar("rectype", 1)

    fun hentSykefraværsstatistikkVirksomhet(
        årstallOgKvartal: ÅrstallOgKvartal
    ): List<SykefraværsstatistikkVirksomhet> {
        return transaction {
            slice(
                årstall,
                kvartal,
                orgnr,
                varighet,
                rectype,
                antallPersoner.sum(),
                tapteDagsverk.sum(),
                muligeDagsverk.sum(),
            ).select {
                (årstall eq årstallOgKvartal.årstall) and (kvartal eq årstallOgKvartal.kvartal)
            }.groupBy(årstall, kvartal, orgnr, varighet, rectype)
                .map {
                    SykefraværsstatistikkVirksomhet(
                        årstall = it[årstall],
                        kvartal = it[kvartal],
                        orgnr = it[orgnr],
                        varighet = it[varighet].first(),
                        tapteDagsverk = it[tapteDagsverk.sum()]!!.toBigDecimal(),
                        muligeDagsverk = it[muligeDagsverk.sum()]!!.toBigDecimal(),
                        antallPersoner = it[antallPersoner.sum()]!!,
                        rectype = it[rectype],
                    )
                }
        }
    }
}