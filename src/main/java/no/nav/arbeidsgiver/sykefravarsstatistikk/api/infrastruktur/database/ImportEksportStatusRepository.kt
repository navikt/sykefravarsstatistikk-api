package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ImportEksportStatus
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert
import org.springframework.stereotype.Repository

@Repository
object ImportEksportStatusRepository : Table("import_eksport_status") {
    val årstall = varchar("aarstall", 4)
    val kvartal = varchar("kvartal", 1)
    val importertStatistikk = bool("importert_statistikk").default(false)
    val importertVirksomhetsdata = bool("importert_virksomhetsdata").default(false)
    val forberedtNesteEksport = bool("forberedt_neste_eksport").default(false)
    val eksportertPåKafka = bool("eksportert_paa_kafka").default(false)
    override val primaryKey: PrimaryKey = PrimaryKey(årstall, kvartal)

    fun settImportEksportStatus(importEksportStatus: ImportEksportStatus) {
        transaction {
            upsert {
                it[årstall] = importEksportStatus.årstallOgKvartal.årstall.toString()
                it[kvartal] = importEksportStatus.årstallOgKvartal.kvartal.toString()
                it[importertStatistikk] = importEksportStatus.importertStatistikk
                it[importertVirksomhetsdata] = importEksportStatus.importertVirksomhetsdata
                it[forberedtNesteEksport] = importEksportStatus.forberedtNesteEksport
                it[eksportertPåKafka] = importEksportStatus.eksportertPåKafka
            }
        }
    }

    fun hentImportEksportStatus(
        årstallOgKvartal: ÅrstallOgKvartal
    ): List<ImportEksportStatus> {
        return transaction {
            select {
                årstall eq årstallOgKvartal.årstall.toString()
                kvartal eq årstallOgKvartal.kvartal.toString()
            }.map {
                ImportEksportStatusDao(
                    årstall = it[årstall],
                    kvartal = it[kvartal],
                    importertStatistikk = it[importertStatistikk],
                    importertVirksomhetsdata = it[importertVirksomhetsdata],
                    forberedtNesteEksport = it[forberedtNesteEksport],
                    eksportertPåKafka = it[eksportertPåKafka],
                )
            }
        }
    }
}

data class ImportEksportStatusDao(
    val årstall: String,
    val kvartal: String,
    override val importertStatistikk: Boolean,
    override val importertVirksomhetsdata: Boolean,
    override val forberedtNesteEksport: Boolean,
    override val eksportertPåKafka: Boolean,
) : ImportEksportStatus {
    override val årstallOgKvartal: ÅrstallOgKvartal = ÅrstallOgKvartal(årstall.toInt(), kvartal.toInt())
}
