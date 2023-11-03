package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Component

@Component
class SykefraværStatistikkNæringRepository(
    override val database: Database
) : UsingExposed, Table("sykefravar_statistikk_naring") {
    val næring = varchar("naring_kode", 2)
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
                this[tapteDagsverk] = it.tapteDagsverk!!.toDouble()
                this[muligeDagsverk] = it.muligeDagsverk!!.toDouble()
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

    fun hentForAlleNæringer(
        kvartaler: List<ÅrstallOgKvartal>
    ): List<SykefraværsstatistikkForNæring> {
        return transaction {
            select {
                (årstall to kvartal) inList kvartaler.map { it.årstall to it.kvartal }
            }.orderBy(
                årstall to SortOrder.DESC,
                kvartal to SortOrder.DESC,
                næring to SortOrder.ASC,
            ).map {
                    SykefraværsstatistikkForNæring(
                        årstall = it[årstall],
                        kvartal = it[kvartal],
                        næringkode = it[næring],
                        antallPersoner = it[antallPersoner],
                        tapteDagsverk = it[tapteDagsverk].toBigDecimal(),
                        muligeDagsverk = it[muligeDagsverk].toBigDecimal(),
                    )
                }
        }
    }

    fun hentKvartalsvisSykefraværprosent(næringa: Næring): List<SykefraværForEttKvartal> {
        return transaction {
            select {
                næring eq næringa.tosifferIdentifikator
            }.orderBy(årstall to SortOrder.ASC, kvartal to SortOrder.ASC).map {
                SykefraværForEttKvartal(
                    årstallOgKvartal = ÅrstallOgKvartal(it[årstall], it[kvartal]),
                    tapteDagsverk = it[tapteDagsverk].toBigDecimal(),
                    muligeDagsverk = it[muligeDagsverk].toBigDecimal(),
                    antallPersoner = it[antallPersoner]
                )
            }
        }
    }

    fun hentForKvartaler(
        næringa: Næring,
        kvartaler: List<ÅrstallOgKvartal>
    ): List<UmaskertSykefraværForEttKvartal> {
        return transaction {
            select {
                (næring eq næringa.tosifferIdentifikator) and
                        ((årstall to kvartal) inList kvartaler.map { it.årstall to it.kvartal })
            }.orderBy(årstall to SortOrder.ASC, kvartal to SortOrder.ASC).map {
                UmaskertSykefraværForEttKvartal(
                    årstallOgKvartal = ÅrstallOgKvartal(it[årstall], it[kvartal]),
                    dagsverkTeller = it[tapteDagsverk].toBigDecimal(),
                    dagsverkNevner = it[muligeDagsverk].toBigDecimal(),
                    antallPersoner = it[antallPersoner]
                )
            }
        }
    }

}