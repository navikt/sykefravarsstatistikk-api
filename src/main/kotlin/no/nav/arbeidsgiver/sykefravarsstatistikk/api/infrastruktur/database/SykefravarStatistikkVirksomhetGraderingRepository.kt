package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.VirksomhetMetadataMedNæringskode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.springframework.stereotype.Component

@Component
class SykefravarStatistikkVirksomhetGraderingRepository(
    override val database: Database
) : UsingExposed, Table("sykefravar_statistikk_virksomhet_med_gradering") {

    val orgnr = varchar("orgnr", 20)
    val næring = text("naring")
    val næringskode = text("naring_kode")
    val årstall = integer("arstall")
    val kvartal = integer("kvartal")
    val antallGraderteSykemeldinger = integer("antall_graderte_sykemeldinger")
    val tapteDagsverkGradertSykemelding = float("tapte_dagsverk_gradert_sykemelding")
    val antallSykemeldinger = integer("antall_sykemeldinger")
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

    fun opprettSykefraværsstatistikkVirksomhetMedGradering(
        sykefraværsstatistikk: List<SykefraværsstatistikkVirksomhetMedGradering>
    ): Int {
        return transaction {
            batchInsert(sykefraværsstatistikk, shouldReturnGeneratedValues = false) {
                this[orgnr] = it.orgnr
                this[næring] = it.næring
                this[næringskode] = it.næringkode
                this[årstall] = it.årstall
                this[kvartal] = it.kvartal
                this[antallGraderteSykemeldinger] = it.antallGraderteSykemeldinger
                this[tapteDagsverkGradertSykemelding] = it.tapteDagsverkGradertSykemelding.toFloat()
                this[antallSykemeldinger] = it.antallSykemeldinger
                this[antallPersoner] = it.antallPersoner
                this[tapteDagsverk] = it.tapteDagsverk.toFloat()
                this[muligeDagsverk] = it.muligeDagsverk.toFloat()
                this[rectype] = it.rectype
            }.count()
        }
    }

    fun hentVirksomhetMetadataNæringskode5Siffer(
        årstallOgKvartal: ÅrstallOgKvartal
    ): List<VirksomhetMetadataMedNæringskode> {
        return transaction {
            slice(orgnr, årstall, kvartal, næringskode)
                .select {
                    (årstall eq årstallOgKvartal.årstall) and (kvartal eq årstallOgKvartal.kvartal)
                }.groupBy(årstall, kvartal, orgnr, næringskode)
                .orderBy(
                    årstall to SortOrder.ASC,
                    kvartal to SortOrder.ASC,
                    orgnr to SortOrder.ASC,
                    næringskode to SortOrder.ASC
                ).map {
                    VirksomhetMetadataMedNæringskode(
                        Orgnr(it[orgnr]),
                        ÅrstallOgKvartal(it[årstall], it[kvartal]),
                        Næringskode(it[næringskode])
                    )
                }
        }
    }

    fun hentForOrgnr(inputOrgnr: Orgnr): List<UmaskertSykefraværForEttKvartal> {
        return transaction {
            slice(
                årstall,
                kvartal,
                tapteDagsverkGradertSykemelding.sum(),
                tapteDagsverk.sum(),
                antallPersoner.sum(),
            ).select {
                (orgnr eq inputOrgnr.verdi) and
                        (rectype eq DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET)
            }.groupBy(årstall, kvartal)
                .orderBy(årstall to SortOrder.ASC, kvartal to SortOrder.ASC)
                .map {
                    UmaskertSykefraværForEttKvartal(
                        ÅrstallOgKvartal(it[årstall], it[kvartal]),
                        it[tapteDagsverkGradertSykemelding.sum()]!!.toBigDecimal(),
                        it[tapteDagsverk.sum()]!!.toBigDecimal(),
                        it[antallPersoner.sum()]!!
                    )
                }
        }
    }

    fun hentForBransje(bransje: Bransje): List<UmaskertSykefraværForEttKvartal> {
        val bransjeidentifikator = if (bransje.erDefinertPåFemsiffernivå()) {
            næringskode
        } else {
            næring
        }
        return transaction {
            slice(
                årstall,
                kvartal,
                tapteDagsverkGradertSykemelding.sum(),
                tapteDagsverk.sum(),
                antallPersoner.sum(),
            ).select {
                (bransjeidentifikator inList bransje.identifikatorer) and
                        (rectype eq DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET)
            }.groupBy(årstall, kvartal)
                .orderBy(årstall to SortOrder.ASC, kvartal to SortOrder.ASC)
                .map {
                    UmaskertSykefraværForEttKvartal(
                        ÅrstallOgKvartal(it[årstall], it[kvartal]),
                        it[tapteDagsverkGradertSykemelding.sum()]!!.toBigDecimal(),
                        it[tapteDagsverk.sum()]!!.toBigDecimal(),
                        it[antallPersoner.sum()]!!
                    )
                }
        }
    }
}