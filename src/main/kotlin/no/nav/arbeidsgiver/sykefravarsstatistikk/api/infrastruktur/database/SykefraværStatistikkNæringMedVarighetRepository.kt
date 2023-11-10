package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import ia.felles.definisjoner.bransjer.BransjeId
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Næring
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkNæringMedVarighet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.UmaskertSykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Component

@Component
class SykefraværStatistikkNæringMedVarighetRepository(
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
    ) = hentSykefraværstatistikk(næring, Varighetskategori.langtidsvarigheter)

    fun hentKorttidsfravær(
        næring: Næring
    ) = hentSykefraværstatistikk(næring, Varighetskategori.kortidsvarigheter)


    private fun hentSykefraværstatistikk(
        næringa: Næring,
        varigheter: List<Varighetskategori>
    ): List<UmaskertSykefraværForEttKvartal> {
        return transaction {
            select {
                (næringskode like stringLiteral("${næringa.tosifferIdentifikator}%")) and
                        (varighet inList varigheter.map { it.kode!! })
            }
                .orderBy(årstall to SortOrder.ASC, kvartal to SortOrder.ASC, varighet to SortOrder.ASC)
                .map {
                    UmaskertSykefraværForEttKvartal(
                        årstallOgKvartal = ÅrstallOgKvartal(it[årstall], it[kvartal]),
                        dagsverkTeller = it[tapteDagsverk].toBigDecimal(),
                        dagsverkNevner = it[muligeDagsverk].toBigDecimal(),
                        antallPersoner = it[antallPersoner],
                    )
                }
        }
    }

    fun hentLangtidsfravær(bransjeId: BransjeId) = when (bransjeId) {
        is BransjeId.Næring -> hentLangtidsfravær(Næring(bransjeId.næring))
        is BransjeId.Næringskoder -> hentFravær(
            næringskoder = bransjeId,
            varigheter = Varighetskategori.langtidsvarigheter
        )
    }

    fun hentKorttidsfravær(bransjeId: BransjeId) = when (bransjeId) {
        is BransjeId.Næring -> hentKorttidsfravær(Næring(bransjeId.næring))
        is BransjeId.Næringskoder -> hentFravær(
            næringskoder = bransjeId,
            varigheter = Varighetskategori.kortidsvarigheter
        )
    }

    private fun hentFravær(
        næringskoder: BransjeId.Næringskoder,
        varigheter: List<Varighetskategori>
    ): List<UmaskertSykefraværForEttKvartal> {
        return transaction {
            select {
                (næringskode inList næringskoder.næringskoder) and
                        (varighet inList varigheter.map { it.kode!! })
            }.orderBy(årstall to SortOrder.ASC, kvartal to SortOrder.ASC, varighet to SortOrder.ASC)
                .map {
                    UmaskertSykefraværForEttKvartal(
                        årstallOgKvartal = ÅrstallOgKvartal(it[årstall], it[kvartal]),
                        dagsverkTeller = it[tapteDagsverk].toBigDecimal(),
                        dagsverkNevner = it[muligeDagsverk].toBigDecimal(),
                        antallPersoner = it[antallPersoner],
                    )
                }
        }
    }

    fun slettKvartal(årstallOgKvartal: ÅrstallOgKvartal): Int {
        return transaction {
            deleteWhere { (årstall eq årstallOgKvartal.årstall) and (kvartal eq årstallOgKvartal.kvartal) }
        }
    }
}
