package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkLand
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.UmaskertSykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class SykefraværStatistikkLandRepository(@param:Qualifier("sykefravarsstatistikkDatabase") override val database: Database) :
    UsingExposed, Table("sykefravar_statistikk_land") {

    val årstall = integer("arstall")
    val kvartal = integer("kvartal")
    val antallPersoner = integer("antall_personer")
    val tapteDagsverk = float("tapte_dagsverk")
    val muligeDagsverk = float("mulige_dagsverk")

    fun hentForKvartaler(kvartaler: List<ÅrstallOgKvartal>): List<UmaskertSykefraværForEttKvartal> {
        return transaction {
            selectAll()
                .where {
                    årstall to kvartal inList kvartaler.map { it.årstall to it.kvartal }
                }.orderBy(årstall to SortOrder.ASC, kvartal to SortOrder.ASC)
                .map {
                    UmaskertSykefraværForEttKvartal(
                        årstallOgKvartal = ÅrstallOgKvartal(it[årstall], it[kvartal]),
                        dagsverkTeller = it[tapteDagsverk].toBigDecimal(),
                        dagsverkNevner = it[muligeDagsverk].toBigDecimal(),
                        antallPersoner = it[antallPersoner]
                    )
                }
        }
    }

    fun hentSykefraværstatistikkLand(kvartaler: List<ÅrstallOgKvartal>): List<SykefraværsstatistikkLand> {
        return hentForKvartaler(kvartaler).map {
            SykefraværsstatistikkLand(
                it.årstallOgKvartal.årstall,
                it.årstallOgKvartal.kvartal,
                it.antallPersoner,
                it.dagsverkTeller,
                it.dagsverkNevner
            )
        }
    }

    fun slettForKvartal(årstallOgKvartal: ÅrstallOgKvartal) = transaction {
        deleteWhere { (årstall eq årstallOgKvartal.årstall) and (kvartal eq årstallOgKvartal.kvartal) }
    }

    fun settInn(data: List<SykefraværsstatistikkLand>): Int {
        return transaction {
            batchInsert(data, shouldReturnGeneratedValues = false) {
                this[årstall] = it.årstall
                this[kvartal] = it.kvartal
                this[antallPersoner] = it.antallPersoner
                this[tapteDagsverk] = it.tapteDagsverk.toFloat()
                this[muligeDagsverk] = it.muligeDagsverk.toFloat()
            }.count()
        }
    }

    fun hentAlt(): List<SykefraværForEttKvartal> {
        return transaction {
            selectAll()
                .orderBy(årstall to SortOrder.ASC, kvartal to SortOrder.ASC)
                .map {
                    SykefraværForEttKvartal(
                        ÅrstallOgKvartal(it[årstall], it[kvartal]),
                        it[tapteDagsverk].toBigDecimal(),
                        it[muligeDagsverk].toBigDecimal(),
                        it[antallPersoner],
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

