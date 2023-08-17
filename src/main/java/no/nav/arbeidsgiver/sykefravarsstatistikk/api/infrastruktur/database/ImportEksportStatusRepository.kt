package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ImportEksportJobb
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ImportEksportStatus
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.upsert
import org.springframework.stereotype.Component

@Component
open class ImportEksportStatusRepository(
    override val database: Database,
) : Table("import_eksport_status"), UsingExposed {
    val årstall = varchar("aarstall", 4)
    val kvartal = varchar("kvartal", 1)

    val importertStatistikk = bool("importert_statistikk")
    val importertVirksomhetsdata = bool("importert_virksomhetsdata")
    val importertNæringskodemapping = bool("importert_naringskodemapping")
    val forberedtNesteEksport = bool("forberedt_neste_eksport")
    val eksportertPåKafka = bool("eksportert_paa_kafka")
    override val primaryKey: PrimaryKey = PrimaryKey(årstall, kvartal)

    fun settImportEksportStatus(importEksportStatus: ImportEksportStatus) {
        transaction {
            upsert {
                it[årstall] = importEksportStatus.årstallOgKvartal.årstall.toString()
                it[kvartal] = importEksportStatus.årstallOgKvartal.kvartal.toString()
                it[importertStatistikk] = importEksportStatus.importertStatistikk
                it[importertVirksomhetsdata] = importEksportStatus.importertVirksomhetsdata
                it[importertNæringskodemapping] = importEksportStatus.importertNæringskodemapping
                it[forberedtNesteEksport] = importEksportStatus.forberedtNesteEksport
                it[eksportertPåKafka] = importEksportStatus.eksportertPåKafka
            }
        }
    }

    fun markerJobbSomKjørt(
        årstallOgKvartal: ÅrstallOgKvartal,
        importEksportJobb: ImportEksportJobb
    ) {
        transaction {
            upsert {
                it[årstall] = årstallOgKvartal.årstall.toString()
                it[kvartal] = årstallOgKvartal.kvartal.toString()
                when (importEksportJobb) {
                    ImportEksportJobb.IMPORTERT_STATISTIKK -> it[importertStatistikk] = true
                    ImportEksportJobb.IMPORTERT_VIRKSOMHETDATA -> it[importertVirksomhetsdata] = true
                    ImportEksportJobb.IMPORTERT_NÆRINGSKODEMAPPING -> it[importertNæringskodemapping] = true
                    ImportEksportJobb.FORBEREDT_NESTE_EKSPORT -> it[forberedtNesteEksport] = true
                    ImportEksportJobb.EKSPORTERT_PÅ_KAFKA -> it[eksportertPåKafka] = true
                }
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
                    importertNæringskodemapping = it[importertNæringskodemapping],
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
    override val importertNæringskodemapping: Boolean,
    override val forberedtNesteEksport: Boolean,
    override val eksportertPåKafka: Boolean,
) : ImportEksportStatus {
    override val årstallOgKvartal: ÅrstallOgKvartal = ÅrstallOgKvartal(årstall.toInt(), kvartal.toInt())
}
