package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkForNæringskode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.UsingExposed
import org.jetbrains.exposed.sql.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class DatavarehusNæringskodeRepository(
    @param:Qualifier("datavarehusDatabase") override val database: Database,
) : UsingExposed, Table("dt_p.agg_ia_sykefravar_naring_kode") {
    val årstall = integer("arstall")
    val kvartal = integer("kvartal")
    val næringskode = varchar("naring_kode", 5)
    val antallPersoner = integer("antpers")
    val tapteDagsverk = double("taptedv")
    val muligeDagsverk = double("muligedv")

    fun hentFor(årstallOgKvartal: ÅrstallOgKvartal): List<SykefraværsstatistikkForNæringskode> {
        return transaction {
            slice(
                årstall, kvartal, næringskode, antallPersoner.sum(), tapteDagsverk.sum(), muligeDagsverk.sum()
            ).select {
                (årstall eq årstallOgKvartal.årstall) and (kvartal eq årstallOgKvartal.kvartal)
            }.groupBy(
                årstall, kvartal, næringskode
            ).map {
                SykefraværsstatistikkForNæringskode(
                    årstall = it[årstall],
                    kvartal = it[kvartal],
                    antallPersoner = it[antallPersoner.sum()]!!,
                    tapteDagsverk = it[tapteDagsverk.sum()]!!.toBigDecimal(),
                    muligeDagsverk = it[muligeDagsverk.sum()]!!.toBigDecimal(),
                    næringskode = it[næringskode]
                )
            }
        }
    }

    fun hentSisteKvartal(): ÅrstallOgKvartal {
        return transaction {
            slice(årstall, kvartal)
                .selectAll()
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