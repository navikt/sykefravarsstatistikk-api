package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkLand
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Component

@Component
class SykefraværStatistikkLandRepository(override val database: Database) :
    UsingExposed, Table("sykefravar_statistikk_land") {

    val årstall = integer("arstall")
    val kvartal = integer("kvartal")
    val antallPersoner = integer("antall_personer")
    val tapteDagsverk = float("tapte_dagsverk")
    val muligeDagsverk = float("mulige_dagsverk")

    fun slettForKvartal(årstallOgKvartal: ÅrstallOgKvartal) = transaction {
        deleteWhere { (årstall eq årstallOgKvartal.årstall) and (kvartal eq årstallOgKvartal.kvartal) }
    }

    fun settInn(data: List<SykefraværsstatistikkLand>): Int {
        return transaction {
            batchInsert(data) {
                this[årstall] = it.årstall
                this[kvartal] = it.kvartal
                this[antallPersoner] = it.antallPersoner
                this[tapteDagsverk] = it.tapteDagsverk.toFloat()
                this[muligeDagsverk] = it.muligeDagsverk.toFloat()
            }.count()
        }
    }
}

