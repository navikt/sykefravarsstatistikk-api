package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import ia.felles.definisjoner.bransjer.Bransje
import ia.felles.definisjoner.bransjer.BransjeId
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.Rectype
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class SykefravarStatistikkVirksomhetGraderingRepository(
    @param:Qualifier("sykefravarsstatistikkDatabase") override val database: Database
) : UsingExposed, Table("sykefravar_statistikk_virksomhet_med_gradering") {

    val orgnr = varchar("orgnr", 20)
    val næring = text("naring")
    val næringskode = text("naring_kode")
    val årstall = integer("arstall")
    val kvartal = integer("kvartal")
    val tapteDagsverkGradertSykemelding = float("tapte_dagsverk_gradert_sykemelding")
    val antallPersoner = integer("antall_personer")
    val tapteDagsverk = float("tapte_dagsverk")
    val muligeDagsverk = float("mulige_dagsverk")
    val rectype = varchar("rectype", 1)

    fun slettDataEldreEnn(årstallOgKvartal: ÅrstallOgKvartal): Int {
        return transaction {
            deleteWhere {
                årstall less årstallOgKvartal.årstall or ((årstall eq årstallOgKvartal.årstall) and (kvartal less årstallOgKvartal.kvartal))
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

    fun settInn(sykefraværsstatistikk: List<SykefraværsstatistikkVirksomhetMedGradering>): Int {
        return transaction {
            batchInsert(sykefraværsstatistikk, shouldReturnGeneratedValues = false) {
                this[orgnr] = it.orgnr
                this[næring] = it.næring
                this[næringskode] = it.næringkode
                this[årstall] = it.årstall
                this[kvartal] = it.kvartal
                this[antallPersoner] = it.antallPersoner
                this[tapteDagsverk] = it.tapteDagsverk.toFloat()
                this[muligeDagsverk] = it.muligeDagsverk.toFloat()
                this[tapteDagsverkGradertSykemelding] = it.tapteDagsverkGradertSykemelding.toFloat()
                this[rectype] = it.rectype
            }.count()
        }
    }

    fun hentSykefraværAlleVirksomheterGradert(
        kvartaler: List<ÅrstallOgKvartal>
    ): List<SykefraværsstatistikkVirksomhetMedGradering> {
        return transaction {
            select(
                årstall,
                kvartal,
                orgnr,
                næring,
                næringskode,
                rectype,
                tapteDagsverkGradertSykemelding.sum(),
                antallPersoner.sum(),
                tapteDagsverk.sum(),
                muligeDagsverk.sum(),
            )
                .where {
                    (årstall to kvartal) inList kvartaler.map { it.årstall to it.kvartal }
                }
                .groupBy(årstall, kvartal, orgnr, næring, næringskode, rectype)
                .map {
                    SykefraværsstatistikkVirksomhetMedGradering(
                        årstall = it[årstall],
                        kvartal = it[kvartal],
                        orgnr = it[orgnr],
                        næring = it[næring],
                        næringkode = it[næringskode],
                        rectype = it[rectype],
                        tapteDagsverkGradertSykemelding = it[tapteDagsverkGradertSykemelding.sum()]!!.toBigDecimal(),
                        antallPersoner = it[antallPersoner.sum()]!!,
                        tapteDagsverk = it[tapteDagsverk.sum()]!!.toBigDecimal(),
                        muligeDagsverk = it[muligeDagsverk.sum()]!!.toBigDecimal(),
                    )
                }
        }
    }

    fun hentForNæring(inputNæring: Næring): List<UmaskertSykefraværForEttKvartal> = hent {
        (næring eq inputNæring.tosifferIdentifikator) and
                (rectype eq Rectype.VIRKSOMHET.kode)
    }

    fun hentForOrgnr(inputOrgnr: Orgnr): List<UmaskertSykefraværForEttKvartal> = hent {
        (orgnr eq inputOrgnr.verdi) and
                (rectype eq Rectype.VIRKSOMHET.kode)
    }

    fun hentForBransje(bransje: Bransje): List<UmaskertSykefraværForEttKvartal> = hent {
        val identifikatorer = bransje.bransjeId.let {
            when (it) {
                is BransjeId.Næring -> listOf(it.næring)
                is BransjeId.Næringskoder -> it.næringskoder
            }
        }
        val bransjeidentifikator = if (bransje.bransjeId is BransjeId.Næringskoder) {
            næringskode
        } else {
            næring
        }
        (bransjeidentifikator inList identifikatorer) and
                (rectype eq Rectype.VIRKSOMHET.kode)
    }


    private fun hent(where: SqlExpressionBuilder.() -> Op<Boolean>): List<UmaskertSykefraværForEttKvartal> {
        return transaction {
            select(
                årstall,
                kvartal,
                tapteDagsverkGradertSykemelding.sum(),
                tapteDagsverk.sum(),
                antallPersoner.sum(),
            ).where { where() }
                .groupBy(årstall, kvartal)
                .orderBy(årstall to SortOrder.ASC, kvartal to SortOrder.ASC)
                .map {
                    UmaskertSykefraværForEttKvartal(
                        årstallOgKvartal = ÅrstallOgKvartal(it[årstall], it[kvartal]),
                        dagsverkTeller = it[tapteDagsverkGradertSykemelding.sum()]!!.toBigDecimal(),
                        dagsverkNevner = it[tapteDagsverk.sum()]!!.toBigDecimal(),
                        antallPersoner = it[antallPersoner.sum()]!!
                    )
                }
        }
    }
}
