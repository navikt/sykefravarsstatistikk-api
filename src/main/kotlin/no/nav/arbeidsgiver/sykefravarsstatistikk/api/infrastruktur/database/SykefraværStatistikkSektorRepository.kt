package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Sektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkSektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class SykefraværStatistikkSektorRepository(
    @param:Qualifier("sykefravarsstatistikkDatabase") override val database: Database
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
                this[sektorKode] = it.sektor.sektorkode
                this[antallPersoner] = it.antallPersoner
                this[tapteDagsverk] = it.tapteDagsverk?.toDouble()!!
                this[muligeDagsverk] = it.muligeDagsverk?.toDouble()!!
            }.count()
        }
    }

    fun hentForKvartaler(kvartaler: List<ÅrstallOgKvartal>): List<SykefraværsstatistikkSektor> {
        return transaction {
            selectAll()
                .where {
                    (årstall to kvartal) inList kvartaler.map { it.årstall to it.kvartal }
                }
                .orderBy(årstall to SortOrder.ASC)
                .orderBy(kvartal to SortOrder.ASC)
                .map {
                    SykefraværsstatistikkSektor(
                        årstall = it[årstall],
                        kvartal = it[kvartal],
                        sektor = Sektor.fraSektorkode(it[sektorKode])!!,
                        antallPersoner = it[antallPersoner],
                        tapteDagsverk = it[tapteDagsverk].toBigDecimal(),
                        muligeDagsverk = it[muligeDagsverk].toBigDecimal()
                    )
                }
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
            selectAll()
                .where {
                    (sektorKode eq sektor.sektorkode)
                }
                .orderBy(årstall to SortOrder.ASC)
                .orderBy(kvartal to SortOrder.ASC)
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