package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkLand
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.UsingExposed
import org.jetbrains.exposed.sql.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class DatavarehusLandRespository(
    @param:Qualifier("datavarehusDatabase")
    override val database: Database
) : UsingExposed, Table("dt_p.agg_ia_sykefravar_land_v") {
    private val årstall = integer("arstall")
    private val kvartal = integer("kvartal")
    private val antallPersoner = integer("antpers")
    private val tapteDagsverk = double("taptedv")
    private val muligeDagsverk = double("muligedv")
    private val kjønn = varchar("kjonn", 1)
    private val næring = varchar("naring", 1)

    fun hentFor(årstallOgKvartal: ÅrstallOgKvartal): List<SykefraværsstatistikkLand> {
        return transaction {
            slice(
                årstall, kvartal, antallPersoner.sum(), tapteDagsverk.sum(), muligeDagsverk.sum()
            ).select {
                (kjønn neq "X") and (næring neq "X") and
                        (årstall eq årstallOgKvartal.årstall) and (kvartal eq årstallOgKvartal.kvartal)
            }.groupBy(
                årstall, kvartal
            ).map {
                SykefraværsstatistikkLand(
                    årstall = it[årstall],
                    kvartal = it[kvartal],
                    antallPersoner = it[antallPersoner.sum()]!!,
                    tapteDagsverk = it[tapteDagsverk.sum()]!!.toBigDecimal(),
                    muligeDagsverk = it[muligeDagsverk.sum()]!!.toBigDecimal()
                )
            }
        }
    }
}