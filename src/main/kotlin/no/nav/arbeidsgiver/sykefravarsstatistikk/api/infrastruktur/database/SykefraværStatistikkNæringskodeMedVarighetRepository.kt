package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import ia.felles.definisjoner.bransjer.BransjeId
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Næring
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkNæringMedVarighet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.UmaskertSykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class SykefraværStatistikkNæringskodeMedVarighetRepository(
    @param:Qualifier("sykefravarsstatistikkDatabase")
    override val database: Database
) : UsingExposed, Table("sykefravar_statistikk_naring_med_varighet") {

    val årstall = integer("arstall")
    val kvartal = integer("kvartal")
    val næringskode = varchar("naring_kode", length = 5)
    val tapteDagsverk = double("tapte_dagsverk")
    val muligeDagsverk = double("mulige_dagsverk")
    val antallPersoner = integer("antall_personer")
    val varighet = char("varighet")

    fun settInn(
        sykefraværsstatistikk: List<SykefraværsstatistikkNæringMedVarighet>
    ): Int {
        return transaction {
            batchInsert(sykefraværsstatistikk, shouldReturnGeneratedValues = false) {
                this[årstall] = it.årstall
                this[kvartal] = it.kvartal
                this[næringskode] = it.næringkode!!
                this[tapteDagsverk] = it.tapteDagsverk.toDouble()
                this[muligeDagsverk] = it.muligeDagsverk.toDouble()
                this[antallPersoner] = it.antallPersoner
                this[varighet] = it.varighet!!
            }.count()
        }
    }

    fun hentLangtidsfravær(
        næring: Næring
    ) = hentSykefravær(næring, Varighetskategori.langtidsvarigheter)

    fun hentKorttidsfravær(
        næring: Næring
    ) = hentSykefravær(næring, Varighetskategori.kortidsvarigheter)


    fun hentLangtidsfravær(bransjeId: BransjeId) = when (bransjeId) {
        is BransjeId.Næring -> hentLangtidsfravær(Næring(bransjeId.næring))
        is BransjeId.Næringskoder -> hentSykefravær(
            næringskoder = bransjeId,
            varigheter = Varighetskategori.langtidsvarigheter
        )
    }

    fun hentKorttidsfravær(bransjeId: BransjeId) = when (bransjeId) {
        is BransjeId.Næring -> hentKorttidsfravær(Næring(bransjeId.næring))
        is BransjeId.Næringskoder -> hentSykefravær(
            næringskoder = bransjeId,
            varigheter = Varighetskategori.kortidsvarigheter
        )
    }


    fun slettKvartal(årstallOgKvartal: ÅrstallOgKvartal): Int {
        return transaction {
            deleteWhere { (årstall eq årstallOgKvartal.årstall) and (kvartal eq årstallOgKvartal.kvartal) }
        }
    }

    private fun hentSykefravær(
        næringa: Næring,
        varigheter: List<Varighetskategori>
    ): List<UmaskertSykefraværForEttKvartal> {
        return transaction {
            select(
                årstall,
                kvartal,
                næringskode,
                varighet,
                tapteDagsverk.sum(),
                muligeDagsverk.sum(),
                antallPersoner.sum(),
            )
                .where {
                    (næringskode like stringLiteral("${næringa.tosifferIdentifikator}%")) and
                            (varighet inList varigheter.map { it.kode!! })
                }
                .groupBy(årstall, kvartal, næringskode, varighet)
                .orderBy(årstall to SortOrder.ASC, kvartal to SortOrder.ASC)
                .map {
                    UmaskertSykefraværForEttKvartal(
                        årstallOgKvartal = ÅrstallOgKvartal(it[årstall], it[kvartal]),
                        dagsverkTeller = it[tapteDagsverk.sum()]!!.toBigDecimal(),
                        dagsverkNevner = it[muligeDagsverk.sum()]!!.toBigDecimal(),
                        antallPersoner = it[antallPersoner.sum()]!!,
                    )
                }
        }
    }

    private fun hentSykefravær(
        næringskoder: BransjeId.Næringskoder,
        varigheter: List<Varighetskategori>
    ): List<UmaskertSykefraværForEttKvartal> {
        return transaction {
            select(
                årstall,
                kvartal,
                næringskode,
                varighet,
                muligeDagsverk.sum(),
                tapteDagsverk.sum(),
                antallPersoner.sum()
            )
                .where {
                    (næringskode inList næringskoder.næringskoder) and
                            (varighet inList varigheter.map { it.kode!! })
                }
                .groupBy(årstall, kvartal, næringskode, varighet)
                .orderBy(årstall to SortOrder.ASC, kvartal to SortOrder.ASC, varighet to SortOrder.ASC)
                .map {
                    UmaskertSykefraværForEttKvartal(
                        årstallOgKvartal = ÅrstallOgKvartal(it[årstall], it[kvartal]),
                        dagsverkTeller = it[tapteDagsverk.sum()]!!.toBigDecimal(),
                        dagsverkNevner = it[muligeDagsverk.sum()]!!.toBigDecimal(),
                        antallPersoner = it[antallPersoner.sum()]!!,
                    )
                }
        }
    }
}
