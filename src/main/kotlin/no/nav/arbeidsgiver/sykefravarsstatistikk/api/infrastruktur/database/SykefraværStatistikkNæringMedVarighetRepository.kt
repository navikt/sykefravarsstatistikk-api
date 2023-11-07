package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.UmaskertSykefraværForEttKvartalMedVarighet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Bransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Næring
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkNæringMedVarighet
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
                this[varighet] = it.varighet!!.first()
            }.count()
        }
    }

    fun hentSykefraværMedVarighetNæring(
        næringa: Næring
    ): List<UmaskertSykefraværForEttKvartalMedVarighet> {
        return transaction {
            select {
                (næringskode like stringLiteral("${næringa.tosifferIdentifikator}%")) and
                        (varighet inList listOf(
                            'A', 'B', 'C', 'D', 'E', 'F', 'X'
                        ))
            }
                .orderBy(årstall to SortOrder.ASC, kvartal to SortOrder.ASC, varighet to SortOrder.ASC)
                .map {
                    UmaskertSykefraværForEttKvartalMedVarighet(
                        årstallOgKvartal = ÅrstallOgKvartal(it[årstall], it[kvartal]),
                        tapteDagsverk = it[tapteDagsverk].toBigDecimal(),
                        muligeDagsverk = it[muligeDagsverk].toBigDecimal(),
                        antallPersoner = it[antallPersoner],
                        varighet = Varighetskategori.fraKode(it[varighet].toString())
                    )
                }
        }
    }

    fun hentSykefraværMedVarighetBransje(
        bransje: Bransje
    ): List<UmaskertSykefraværForEttKvartalMedVarighet> {
        return if (bransje.erDefinertPåTosiffernivå()) {
            hentSykefraværMedVarighetNæring(Næring(bransje.identifikatorer.first()))
        } else {
            transaction {
                select {
                    (næringskode inList bransje.identifikatorer) and
                            (varighet inList listOf(
                                'A', 'B', 'C', 'D', 'E', 'F', 'X'
                            ))
                }.orderBy(årstall to SortOrder.ASC, kvartal to SortOrder.ASC, varighet to SortOrder.ASC)
                    .map {
                        UmaskertSykefraværForEttKvartalMedVarighet(
                            årstallOgKvartal = ÅrstallOgKvartal(it[årstall], it[kvartal]),
                            tapteDagsverk = it[tapteDagsverk].toBigDecimal(),
                            muligeDagsverk = it[muligeDagsverk].toBigDecimal(),
                            antallPersoner = it[antallPersoner],
                            varighet = Varighetskategori.fraKode(it[varighet].toString())
                        )
                    }
            }
        }
    }

    fun slettKvartal(årstallOgKvartal: ÅrstallOgKvartal): Int {
        return transaction {
            deleteWhere { (årstall eq årstallOgKvartal.årstall) and (kvartal eq årstallOgKvartal.kvartal) }
        }
    }
}
