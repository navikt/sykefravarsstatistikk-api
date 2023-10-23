package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.springframework.stereotype.Component
import java.sql.ResultSet

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
    val varighet = char("varighet")
    val virksomhetstype = char("rectype")

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
                .orderBy(årstall to SortOrder.ASC, kvartal to SortOrder.ASC)
                .map {
                    UmaskertSykefraværForEttKvartal(
                        ÅrstallOgKvartal(it[årstall], it[kvartal]),
                        it[tapteDagsverk.sum()]!!.toBigDecimal(),
                        it[muligeDagsverk.sum()]!!.toBigDecimal(),
                        it[antallPersoner.sum()]!!
                    )
                }.sorted()
        }
    }

    fun settInn(data: List<SykefraværsstatistikkVirksomhet>): Int {
        return transaction {
            batchInsert(data) {
                this[orgnr] = it.orgnr!!
                this[årstall] = it.årstall
                this[kvartal] = it.kvartal
                this[antallPersoner] = it.antallPersoner
                this[tapteDagsverk] = it.tapteDagsverk.toFloat()
                this[muligeDagsverk] = it.muligeDagsverk.toFloat()
                this[varighet] = it.varighet!!.first()
                this[virksomhetstype] = it.rectype!!.first()
            }.count()
        }
    }

    fun hentAlt(organisasjonsnummer: Orgnr): List<SykefraværForEttKvartal> {
        return transaction {
            slice(
                tapteDagsverk.sum(),
                muligeDagsverk.sum(),
                antallPersoner.sum(),
                årstall,
                kvartal,
            )
                .select { orgnr eq organisasjonsnummer.verdi }
                .groupBy(årstall, kvartal)
                .orderBy(årstall to SortOrder.ASC, kvartal to SortOrder.ASC)
                .map {
                    SykefraværForEttKvartal(
                        årstallOgKvartal = ÅrstallOgKvartal(it[årstall], it[kvartal]),
                        tapteDagsverk = it[tapteDagsverk.sum()]!!.toBigDecimal(),
                        muligeDagsverk = it[muligeDagsverk.sum()]!!.toBigDecimal(),
                        antallPersoner = it[antallPersoner.sum()]!!
                    )
                }
        }
    }

    fun slettForKvartal(årstallOgKvartal: ÅrstallOgKvartal): Int {
        return transaction {
            deleteWhere { (årstall eq årstallOgKvartal.årstall) and (kvartal eq årstallOgKvartal.kvartal) }
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