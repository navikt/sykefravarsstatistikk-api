package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import ia.felles.definisjoner.bransjer.Bransje
import ia.felles.definisjoner.bransjer.BransjeId
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component


@Component
class SykefraværStatistikkNæringskodeRepository(
    @param:Qualifier("sykefravarsstatistikkDatabase") override val database: Database
) : UsingExposed, Table("sykefravar_statistikk_naring5siffer") {

    val næringskode = varchar("naring_kode", 5)
    val årstall = integer("arstall")
    val kvartal = integer("kvartal")
    val antallPersoner = integer("antall_personer")
    val tapteDagsverk = double("tapte_dagsverk")
    val muligeDagsverk = double("mulige_dagsverk")

    fun settInn(data: List<SykefraværsstatistikkForNæringskode>): Int {
        return transaction {
            batchInsert(data) {
                this[årstall] = it.årstall
                this[kvartal] = it.kvartal
                this[næringskode] = it.næringskode
                this[antallPersoner] = it.antallPersoner
                this[tapteDagsverk] = it.tapteDagsverk.toDouble()
                this[muligeDagsverk] = it.muligeDagsverk.toDouble()
            }
        }.count()
    }

    fun hentKvartalsvisSykefraværprosent(næringskoder: List<Næringskode>): List<SykefraværForEttKvartal> {
        return transaction {
            slice(
                årstall,
                kvartal,
                antallPersoner.sum(),
                tapteDagsverk.sum(),
                muligeDagsverk.sum()
            )
                .select { næringskode inList næringskoder.map { it.femsifferIdentifikator } }
                .groupBy(årstall, kvartal)
                .orderBy(årstall to SortOrder.ASC)
                .orderBy(kvartal to SortOrder.ASC)
                .map {
                    SykefraværForEttKvartal(
                        årstallOgKvartal = ÅrstallOgKvartal(it[årstall], it[kvartal]),
                        antallPersoner = it[antallPersoner.sum()]!!,
                        tapteDagsverk = it[tapteDagsverk.sum()]!!.toBigDecimal(),
                        muligeDagsverk = it[muligeDagsverk.sum()]!!.toBigDecimal()

                    )
                }
        }
    }

    fun hentAltForKvartaler(kvartaler: List<ÅrstallOgKvartal>): List<SykefraværsstatistikkForNæringskode> {
        return transaction {
            select {
                (årstall to kvartal) inList kvartaler.map { it.årstall to it.kvartal }
            }
                .orderBy(årstall to SortOrder.DESC)
                .orderBy(kvartal to SortOrder.DESC)
                .map {
                    SykefraværsstatistikkForNæringskode(
                        årstall = it[årstall],
                        kvartal = it[kvartal],
                        næringskode = it[næringskode],
                        antallPersoner = it[antallPersoner],
                        tapteDagsverk = it[tapteDagsverk].toBigDecimal(),
                        muligeDagsverk = it[muligeDagsverk].toBigDecimal(),
                    )
                }
        }
    }

    fun hentForBransje(
        bransje: Bransje,
        kvartaler: List<ÅrstallOgKvartal>
    ): List<UmaskertSykefraværForEttKvartal> {
        return transaction {
            slice(
                årstall,
                kvartal,
                antallPersoner.sum(),
                tapteDagsverk.sum(),
                muligeDagsverk.sum(),
            )
                .select {
                    (næringskode inList (bransje.bransjeId as BransjeId.Næringskoder).næringskoder) and
                            ((årstall to kvartal) inList kvartaler.map { it.årstall to it.kvartal })
                }
                .groupBy(årstall, kvartal)
                .orderBy(årstall to SortOrder.ASC)
                .orderBy(kvartal to SortOrder.ASC)
                .map {
                    UmaskertSykefraværForEttKvartal(
                        årstallOgKvartal = ÅrstallOgKvartal(it[årstall], it[kvartal]),
                        antallPersoner = it[antallPersoner.sum()]!!,
                        dagsverkTeller = it[tapteDagsverk.sum()]!!.toBigDecimal(),
                        dagsverkNevner = it[muligeDagsverk.sum()]!!.toBigDecimal(
                        )
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

    fun slettKvartal(årstallOgKvartal: ÅrstallOgKvartal): Int {
        return transaction {
            deleteWhere { (årstall eq årstallOgKvartal.årstall) and (kvartal eq årstallOgKvartal.kvartal) }
        }
    }
}