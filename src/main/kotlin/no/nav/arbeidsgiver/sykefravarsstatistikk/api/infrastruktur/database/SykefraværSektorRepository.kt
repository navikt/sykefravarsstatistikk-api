package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Sektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkSektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Component

@Component
class SykefraværSektorRepository(
    override val database: Database
) : UsingExposed, Table("sykefravar_statistikk_sektor") {
    val årstall = integer("arstall")
    val kvartal = integer("kvartal")
    val sektorKode = text("sektor_kode")
    val antallPersoner = integer("antall_personer")
    val tapteDagsverk = double("tapte_dagsverk")
    val muligeDagsverk = double("mulige_dagsverk")

    fun settInn(statistikk: List<SykefraværsstatistikkSektor>): Int {
        return transaction {
            batchInsert(statistikk, shouldReturnGeneratedValues = false) {
                this[årstall] = it.årstall
                this[kvartal] = it.kvartal
                this[sektorKode] = it.sektorkode
                this[antallPersoner] = it.antallPersoner
                this[tapteDagsverk] = it.tapteDagsverk?.toDouble()!!
                this[muligeDagsverk] = it.muligeDagsverk?.toDouble()!!
            }.count()
        }
    }

    fun slettDataFor(årstallOgKvartal: ÅrstallOgKvartal): Int {
        return transaction {
            deleteWhere {
                (årstall eq årstallOgKvartal.årstall) and (kvartal eq årstallOgKvartal.kvartal)
            }
        }
    }

    fun hentKvartalsvisSykefraværprosent(sektor: Sektor): List<SykefraværForEttKvartal> {
        return transaction {
            select {
                sektorKode eq sektor.sektorkode
            }
                .groupBy(årstall, kvartal)
                .orderBy(årstall to SortOrder.ASC, kvartal to SortOrder.ASC)
                .map {
                    SykefraværForEttKvartal(
                        årstallOgKvartal = ÅrstallOgKvartal(
                            årstall = it[årstall],
                            kvartal = it[kvartal]
                        ),
                        tapteDagsverk = it[tapteDagsverk].toBigDecimal(),
                        muligeDagsverk = it[muligeDagsverk].toBigDecimal(),
                        antallPersoner = it[antallPersoner]
                    )
                }
        }
    }

}