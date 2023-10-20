package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.UmaskertSykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Virksomhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.springframework.stereotype.Component

@Component
class SykefravarStatistikkVirksomhetRepository(
    override val database: Database
) : UsingExposed, Table("sykefravar_statistikk_virksomhet") {

    val orgnr = varchar("orgnr", 20)
    val årstall = integer("arstall")
    val kvartal = integer("kvartal")
    val antallPersoner = integer("antall_personer")
    val tapteDagsverk = float("tapte_dagsverk")
    val muligeDagsverk = float("mulige_dagsverk")

    fun hentUmaskertSykefravær(
        virksomhet: Virksomhet, fraÅrstallOgKvartal: ÅrstallOgKvartal
    ): List<UmaskertSykefraværForEttKvartal> {
        return transaction {
            slice(
                tapteDagsverk.sum(),
                muligeDagsverk.sum(),
                antallPersoner.sum(),
                årstall,
                kvartal
            ).select {
                orgnr eq virksomhet.orgnr.verdi and ((årstall eq fraÅrstallOgKvartal.årstall) and (kvartal greaterEq fraÅrstallOgKvartal.kvartal)) or (årstall greater fraÅrstallOgKvartal.årstall)
            }
                .groupBy(årstall, kvartal)
                .orderBy(årstall to SortOrder.ASC, kvartal to SortOrder.ASC).map {
                    UmaskertSykefraværForEttKvartal(
                        ÅrstallOgKvartal(it[årstall], it[kvartal]),
                        it[tapteDagsverk.sum()]!!.toBigDecimal(),
                        it[muligeDagsverk.sum()]!!.toBigDecimal(),
                        it[antallPersoner.sum()]!!
                    )
                }.sorted()
        }
    }

    fun slettDataEldreEnn(årstallOgKvartal: ÅrstallOgKvartal): Int {
        return transaction {
            deleteWhere {
                årstall less årstallOgKvartal.årstall or ((årstall eq årstallOgKvartal.årstall) and (kvartal less årstallOgKvartal.kvartal))
            }
        }
    }
}