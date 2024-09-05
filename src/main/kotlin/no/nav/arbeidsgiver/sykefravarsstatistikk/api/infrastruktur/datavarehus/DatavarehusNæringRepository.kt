package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkForNæring
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.UsingExposed
import org.jetbrains.exposed.sql.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class DatavarehusNæringRepository(
    @param:Qualifier("datavarehusDatabase") override val database: Database
) : UsingExposed, Table("dvh_syfra.v_agg_ia_sykefravar_naring") {
    val årstall = integer("arstall")
    val kvartal = integer("kvartal")
    val næring = varchar("naring", 2)
    val antallPersoner = integer("antpers")
    val tapteDagsverk = double("taptedv")
    val muligeDagsverk = double("muligedv")
    val kjønn = varchar("kjonn", 1)

    fun hentFor(årstallOgKvartal: ÅrstallOgKvartal): List<SykefraværsstatistikkForNæring> {
        return transaction {
            select(
                årstall, kvartal, næring, antallPersoner.sum(), tapteDagsverk.sum(), muligeDagsverk.sum()
            ).where {
                (kjønn neq "X") and (næring neq "X") and
                        (årstall eq årstallOgKvartal.årstall) and (kvartal eq årstallOgKvartal.kvartal)
            }.groupBy(
                årstall, kvartal, næring
            ).map {
                SykefraværsstatistikkForNæring(
                    årstall = it[årstall],
                    kvartal = it[kvartal],
                    antallPersoner = it[antallPersoner.sum()]!!,
                    tapteDagsverk = it[tapteDagsverk.sum()]!!.toBigDecimal(),
                    muligeDagsverk = it[muligeDagsverk.sum()]!!.toBigDecimal(),
                    næring = it[næring]
                )
            }
        }
    }

    fun hentSisteKvartal(): ÅrstallOgKvartal {
        return transaction {
            select(årstall, kvartal)
                .orderBy(årstall to SortOrder.DESC, kvartal to SortOrder.DESC)
                .limit(1).map {
                    ÅrstallOgKvartal(
                        årstall = it[årstall],
                        kvartal = it[kvartal]
                    )
                }.first()
        }
    }
}