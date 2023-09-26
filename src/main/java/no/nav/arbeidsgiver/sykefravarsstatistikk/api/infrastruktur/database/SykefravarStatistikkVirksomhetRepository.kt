package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.springframework.stereotype.Component

@Component
class SykefravarStatistikkVirksomhetRepository(override val database: Database) : UsingExposed,
    Table("sykefravar_statistikk_virksomhet") {

    val orgnr = varchar("orgnr", 20)
    val årstall = integer("arstall")
    val kvartal = integer("kvartal")
    val antallPersoner = integer("antall_personer")
    val tapteDagsverk = float("tapte_dagsverk")
    val muligeDagsverk = float("mulige_dagsverk")

    fun slettDataEldreEnn(årstallOgKvartal: ÅrstallOgKvartal) {
        transaction {
            deleteWhere {
                årstall less årstallOgKvartal.årstall or
                        ((årstall eq årstallOgKvartal.årstall) and (kvartal less årstallOgKvartal.kvartal))
            }
        }
    }
}