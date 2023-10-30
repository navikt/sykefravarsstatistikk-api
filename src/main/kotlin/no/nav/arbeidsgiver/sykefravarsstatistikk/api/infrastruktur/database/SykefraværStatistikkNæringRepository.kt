package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkForNæring
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Component

@Component
class SykefraværStatistikkNæringRepository(
    override val database: Database
) : UsingExposed, Table("sykefravar_statistikk_naring") {
    val næring = text("naring_kode")
    val årstall = integer("arstall")
    val kvartal = integer("kvartal")
    val antallPersoner = integer("antall_personer")
    val tapteDagsverk = double("tapte_dagsverk")
    val muligeDagsverk = double("mulige_dagsverk")

    fun settInn(statistikk: List<SykefraværsstatistikkForNæring>): Int {
        return transaction {
            batchInsert(statistikk, shouldReturnGeneratedValues = false) {
                this[næring] = it.næringkode
                this[årstall] = it.årstall
                this[kvartal] = it.kvartal
                this[antallPersoner] = it.antallPersoner
                this[tapteDagsverk] = it.tapteDagsverk?.toDouble()!!
                this[muligeDagsverk] = it.muligeDagsverk?.toDouble()!!
            }.count()
        }
    }

    fun slettFor(årstallOgKvartal: ÅrstallOgKvartal): Int {
        return transaction {
            deleteWhere {
                (årstall eq årstallOgKvartal.årstall) and (kvartal eq årstallOgKvartal.kvartal)
            }
        }
    }

    fun hentNyesteKvartal(): ÅrstallOgKvartal {
        return transaction {
            selectAll()
                .orderBy(årstall to SortOrder.DESC)
                .orderBy(kvartal to SortOrder.DESC)
                .limit(1)
                .map { ÅrstallOgKvartal(it[årstall], it[kvartal]) }
                .first()
        }
    }
}