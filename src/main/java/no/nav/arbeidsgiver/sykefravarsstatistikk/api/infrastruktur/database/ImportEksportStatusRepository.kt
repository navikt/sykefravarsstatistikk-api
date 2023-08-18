package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ImportEksportJobb
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
    val fullførteJobber = text("fullforte_jobber").default("")

    override val primaryKey: PrimaryKey = PrimaryKey(årstall, kvartal)

    fun leggTilFullførtJobb(fullførtJobb: ImportEksportJobb, forKvartal: ÅrstallOgKvartal) {
        val alleredeFullførteJobber: List<FullførteJobber> =
            hentImportEksportStatus(forKvartal).firstOrNull()?.fullførteJobber
                ?: emptyList()

        val oppdatertListeMedJobber = alleredeFullførteJobber + FullførteJobber.fraDomene(fullførtJobb)

        lagreImportEksportStatus(ImportEksportStatus(forKvartal, oppdatertListeMedJobber))
    }

    fun hentFullførteJobber(årstallOgKvartal: ÅrstallOgKvartal): List<ImportEksportJobb> {
        return hentImportEksportStatus(årstallOgKvartal).firstOrNull()?.fullførteJobber?.map { it.tilDomene() }
            ?: emptyList()
    }

    private fun hentImportEksportStatus(årstallOgKvartal: ÅrstallOgKvartal): List<ImportEksportStatus> {
        return transaction {
            select {
                årstall eq årstallOgKvartal.årstall.toString()
                kvartal eq årstallOgKvartal.kvartal.toString()
            }.map {
                ImportEksportStatus(
                    årstallOgKvartal = ÅrstallOgKvartal(it[årstall].toInt(), it[kvartal].toInt()),
                    fullførteJobber = it[fullførteJobber].splittTilFullførteJobber()
                )
            }
        }
    }

    private fun lagreImportEksportStatus(importEksportStatus: ImportEksportStatus) {
        transaction {
            upsert {
                it[årstall] = importEksportStatus.årstallOgKvartal.årstall.toString()
                it[kvartal] = importEksportStatus.årstallOgKvartal.kvartal.toString()
                it[fullførteJobber] = importEksportStatus.fullførteJobber.joinToString(",")
            }
        }
    }
}

private open class ImportEksportStatus(
    val årstallOgKvartal: ÅrstallOgKvartal,
    val fullførteJobber: List<FullførteJobber>
)

private enum class FullførteJobber {
    IMPORTERT_STATISTIKK,
    IMPORTERT_VIRKSOMHETDATA,
    IMPORTERT_NÆRINGSKODEMAPPING,
    FORBEREDT_NESTE_EKSPORT,
    EKSPORTERT_PÅ_KAFKA;

    fun tilDomene(): ImportEksportJobb = when (this) {
        IMPORTERT_STATISTIKK -> ImportEksportJobb.IMPORTERT_STATISTIKK
        IMPORTERT_VIRKSOMHETDATA -> ImportEksportJobb.IMPORTERT_VIRKSOMHETDATA
        IMPORTERT_NÆRINGSKODEMAPPING -> ImportEksportJobb.IMPORTERT_NÆRINGSKODEMAPPING
        FORBEREDT_NESTE_EKSPORT -> ImportEksportJobb.FORBEREDT_NESTE_EKSPORT
        EKSPORTERT_PÅ_KAFKA -> ImportEksportJobb.EKSPORTERT_PÅ_KAFKA
    }

    companion object {
        fun fraDomene(importEksportJobb: ImportEksportJobb): FullførteJobber = when (importEksportJobb) {
            ImportEksportJobb.IMPORTERT_STATISTIKK -> IMPORTERT_STATISTIKK
            ImportEksportJobb.IMPORTERT_VIRKSOMHETDATA -> IMPORTERT_VIRKSOMHETDATA
            ImportEksportJobb.IMPORTERT_NÆRINGSKODEMAPPING -> IMPORTERT_NÆRINGSKODEMAPPING
            ImportEksportJobb.FORBEREDT_NESTE_EKSPORT -> FORBEREDT_NESTE_EKSPORT
            ImportEksportJobb.EKSPORTERT_PÅ_KAFKA -> EKSPORTERT_PÅ_KAFKA
        }
    }
}


private fun String.splittTilFullførteJobber(): List<FullførteJobber> {
    return split(",")
        .mapNotNull { it.ifEmpty { null } }
        .map { FullførteJobber.valueOf(it) }
}