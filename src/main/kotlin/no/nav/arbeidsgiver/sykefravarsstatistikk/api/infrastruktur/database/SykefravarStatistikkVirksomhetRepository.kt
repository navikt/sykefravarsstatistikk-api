package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.UmaskertSykefraværForEttKvartalMedVarighet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
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
    val varighet = char("varighet").nullable()
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

    fun hentSykefraværMedVarighet(
        organisasjonsnummer: Orgnr
    ): List<UmaskertSykefraværForEttKvartalMedVarighet> {
        return transaction {
            select {
                (orgnr eq organisasjonsnummer.verdi) and
                        (varighet inList listOf('A', 'B', 'C', 'D', 'E', 'F', 'X'))
            }.orderBy(
                årstall to SortOrder.ASC,
                kvartal to SortOrder.ASC,
                varighet to SortOrder.ASC
            ).map {
                UmaskertSykefraværForEttKvartalMedVarighet(
                    årstallOgKvartal = ÅrstallOgKvartal(
                        årstall = it[årstall],
                        kvartal = it[kvartal]
                    ),
                    tapteDagsverk = it[tapteDagsverk].toBigDecimal(),
                    muligeDagsverk = it[muligeDagsverk].toBigDecimal(),
                    antallPersoner = it[antallPersoner],
                    varighet = Varighetskategori.fraKode(it[varighet]?.toString())
                )
            }
        }
    }

    fun hentSykefraværAlleVirksomheter(
        kvartaler: List<ÅrstallOgKvartal>
    ): List<SykefraværsstatistikkVirksomhetUtenVarighet> {
        return transaction {
            slice(
                årstall,
                kvartal,
                orgnr,
                tapteDagsverk.sum(),
                muligeDagsverk.sum(),
                antallPersoner.sum(),
            ).select {
                (årstall to kvartal) inList kvartaler.map { it.årstall to it.kvartal }
            }.groupBy(årstall, kvartal, orgnr)
                .map {
                    SykefraværsstatistikkVirksomhetUtenVarighet(
                        årstall = it[årstall],
                        kvartal = it[kvartal],
                        orgnr = it[orgnr],
                        tapteDagsverk = it[tapteDagsverk.sum()]!!.toBigDecimal(),
                        muligeDagsverk = it[muligeDagsverk.sum()]!!.toBigDecimal(),
                        antallPersoner = it[antallPersoner.sum()]!!
                    )
                }
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
                this[virksomhetstype] = it.rectype.first()
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