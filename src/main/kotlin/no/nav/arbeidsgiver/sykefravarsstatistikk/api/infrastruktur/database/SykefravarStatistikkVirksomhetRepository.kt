package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class SykefravarStatistikkVirksomhetRepository(
    @param:Qualifier("sykefravarsstatistikkDatabase") override val database: Database
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
        virksomhet: Virksomhet, kvartaler: List<ÅrstallOgKvartal>
    ): List<UmaskertSykefraværForEttKvartal> {
        return transaction {
            select(
                tapteDagsverk.sum(),
                muligeDagsverk.sum(),
                antallPersoner.sum(),
                årstall,
                kvartal
            ).where {
                orgnr eq virksomhet.orgnr.verdi and (årstall to kvartal inList kvartaler.map { it.årstall to it.kvartal })
            }
                .groupBy(årstall, kvartal)
                .orderBy(årstall to SortOrder.ASC, kvartal to SortOrder.ASC)
                .map {
                    UmaskertSykefraværForEttKvartal(
                        årstallOgKvartal = ÅrstallOgKvartal(it[årstall], it[kvartal]),
                        dagsverkTeller = it[tapteDagsverk.sum()]!!.toBigDecimal(),
                        dagsverkNevner = it[muligeDagsverk.sum()]!!.toBigDecimal(),
                        antallPersoner = it[antallPersoner.sum()]!!
                    )
                }.sorted()
        }
    }

    fun hentLangtidsfravær(
        organisasjonsnummer: Orgnr
    ) = hentSykefraværMedVarighet(organisasjonsnummer, Varighetskategori.langtidsvarigheter)

    fun hentKorttidsfravær(
        organisasjonsnummer: Orgnr
    ) = hentSykefraværMedVarighet(organisasjonsnummer, Varighetskategori.kortidsvarigheter)

    private fun hentSykefraværMedVarighet(
        organisasjonsnummer: Orgnr,
        varigheter: List<Varighetskategori>
    ): List<UmaskertSykefraværForEttKvartal> {
        return transaction {
            selectAll().where {
                (orgnr eq organisasjonsnummer.verdi) and
                        (varighet inList varigheter.map { it.kode })
            }.orderBy(
                årstall to SortOrder.ASC,
                kvartal to SortOrder.ASC,
                varighet to SortOrder.ASC
            ).map {
                UmaskertSykefraværForEttKvartal(
                    SykefraværsstatistikkVirksomhetUtenVarighet(
                        årstall = it[årstall],
                        kvartal = it[kvartal],
                        tapteDagsverk = it[tapteDagsverk].toBigDecimal(),
                        muligeDagsverk = it[muligeDagsverk].toBigDecimal(),
                        antallPersoner = it[antallPersoner],
                        orgnr = it[orgnr]
                    )
                )
            }
        }
    }

    fun hentSykefraværAlleVirksomheter(
        kvartaler: List<ÅrstallOgKvartal>
    ): List<SykefraværsstatistikkVirksomhetUtenVarighet> {
        return transaction {
            select (
                årstall,
                kvartal,
                orgnr,
                tapteDagsverk.sum(),
                muligeDagsverk.sum(),
                antallPersoner.sum(),
            ).where {
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
            batchInsert(data, shouldReturnGeneratedValues = false) {
                this[orgnr] = it.orgnr!!
                this[årstall] = it.årstall
                this[kvartal] = it.kvartal
                this[antallPersoner] = it.antallPersoner
                this[tapteDagsverk] = it.tapteDagsverk.toFloat()
                this[muligeDagsverk] = it.muligeDagsverk.toFloat()
                this[varighet] = it.varighet
                this[virksomhetstype] = it.rectype.first()
            }.count()
        }
    }

    fun hentAlt(organisasjonsnummer: Orgnr): List<SykefraværForEttKvartal> {
        return transaction {
            select (
                tapteDagsverk.sum(),
                muligeDagsverk.sum(),
                antallPersoner.sum(),
                årstall,
                kvartal,
            )
                .where { orgnr eq organisasjonsnummer.verdi }
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