package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkNæringMedVarighet
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
    val varighet = varchar("varighet", 1).nullable()
    val tapteDagsverk = double("taptedv")
    val muligeDagsverk = double("muligedv")
    val antallPersoner = integer("antpers")
    val rectype = varchar("rectype", 1)

    fun hentSykefraværsstatistikkVirksomhet(
        årstallOgKvartal: ÅrstallOgKvartal
    ): List<SykefraværsstatistikkVirksomhet> {
        return transaction {
            select(
                årstall,
                kvartal,
                orgnr,
                varighet,
                rectype,
                antallPersoner.sum(),
                tapteDagsverk.sum(),
                muligeDagsverk.sum(),
            ).where {
                (årstall eq årstallOgKvartal.årstall) and (kvartal eq årstallOgKvartal.kvartal)
            }.groupBy(årstall, kvartal, orgnr, varighet, rectype)
                .map {
                    SykefraværsstatistikkVirksomhet(
                        årstall = it[årstall],
                        kvartal = it[kvartal],
                        orgnr = it[orgnr],
                        varighet = it[varighet]?.first(),
                        tapteDagsverk = it[tapteDagsverk.sum()]!!.toBigDecimal(),
                        muligeDagsverk = it[muligeDagsverk.sum()]!!.toBigDecimal(),
                        antallPersoner = it[antallPersoner.sum()]!!,
                        rectype = it[rectype],
                    )
                }
        }
    }

    fun hentSykefraværsstatistikkNæringMedVarighet(
        årstallOgKvartal: ÅrstallOgKvartal
    ): List<SykefraværsstatistikkNæringMedVarighet> {
        return transaction {
            select(
                årstall, kvartal, næringskode, varighet,
                antallPersoner.sum(), tapteDagsverk.sum(), muligeDagsverk.sum()
            ).where {
                (årstall eq årstallOgKvartal.årstall) and
                        (kvartal eq årstallOgKvartal.kvartal) and
                        (varighet neq null) and
                        (rectype eq Rectype.VIRKSOMHET.kode)
            }.groupBy(årstall, kvartal, næringskode, varighet)
                .map {
                    SykefraværsstatistikkNæringMedVarighet(
                        årstall = it[årstall],
                        kvartal = it[kvartal],
                        næringkode = it[næringskode],
                        varighet = it[varighet]?.first(),
                        antallPersoner = it[antallPersoner.sum()]!!,
                        tapteDagsverk = it[tapteDagsverk.sum()]!!.toBigDecimal(),
                        muligeDagsverk = it[muligeDagsverk.sum()]!!.toBigDecimal(),
                    )
                }
        }
    }

    fun hentSisteKvartal(): ÅrstallOgKvartal {
        return transaction {
            select(årstall, kvartal)
                .orderBy(årstall to SortOrder.DESC, kvartal to SortOrder.DESC)
                .limit(1).map {
                    ÅrstallOgKvartal(
                        årstall = it[årstall],
                        kvartal = it[kvartal]
                    )
                }.first()
        }
    }
}