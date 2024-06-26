package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron.ImportEksportJobb
import org.jetbrains.exposed.sql.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
open class ImportEksportStatusRepository(
    @param:Qualifier("sykefravarsstatistikkDatabase") override val database: Database,
) : Table("import_eksport_status"), UsingExposed {
    val årstall = varchar("aarstall", 4)
    val kvartal = varchar("kvartal", 1)
    val fullførteJobber = text("fullforte_jobber").default("")

    override val primaryKey: PrimaryKey = PrimaryKey(årstall, kvartal)

    fun leggTilFullførtJobb(fullførtJobb: ImportEksportJobb, forKvartal: ÅrstallOgKvartal) {
        val alleredeFullførteJobber: List<FullførteJobber> =
            hentImportEksportStatus(forKvartal).firstOrNull()?.fullførteJobber ?: emptyList()

        val oppdatertListeMedJobber =
            alleredeFullførteJobber + FullførteJobber.fraDomene(fullførtJobb)

        lagreImportEksportStatus(ImportEksportStatus(forKvartal, oppdatertListeMedJobber))
    }

    fun hentFullførteJobber(årstallOgKvartal: ÅrstallOgKvartal): List<ImportEksportJobb> {
        return hentImportEksportStatus(årstallOgKvartal).firstOrNull()?.fullførteJobber?.mapNotNull { it.tilDomene() }
            ?: emptyList()
    }

    private fun hentImportEksportStatus(årstallOgKvartal: ÅrstallOgKvartal): List<ImportEksportStatus> {
        return transaction {
            selectAll()
                .where {
                    (årstall eq årstallOgKvartal.årstall.toString()) and (kvartal eq årstallOgKvartal.kvartal.toString())
                }
                .map {
                    ImportEksportStatus(
                        årstallOgKvartal = ÅrstallOgKvartal(it[årstall].toInt(), it[kvartal].toInt()),
                        fullførteJobber = it[fullførteJobber].splittTilListe()
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

private data class ImportEksportStatus(
    val årstallOgKvartal: ÅrstallOgKvartal,
    val fullførteJobber: List<FullførteJobber>
)

// Formålet til denne enumen er å adskille domene og database. Dermed kan ImportEksportJobb endres fritt, uten at det
// fører til kluss i databasen.
private enum class FullførteJobber {
    IMPORTERT_STATISTIKK,
    IMPORTERT_VIRKSOMHETDATA,
    EKSPORTERT_LEGACY,
    IMPORTERT_NÆRINGSKODEMAPPING,

    // Ikke lenger i bruk i koden, men finnes i databasen.
    FORBEREDT_NESTE_EKSPORT_LEGACY,
    EKSPORTERT_METADATA_VIRKSOMHET,
    EKSPORTERT_PER_STATISTIKKATEGORI;

    fun tilDomene(): ImportEksportJobb? = when (this) {
        IMPORTERT_STATISTIKK -> ImportEksportJobb.IMPORTERT_STATISTIKK
        IMPORTERT_VIRKSOMHETDATA -> ImportEksportJobb.IMPORTERT_VIRKSOMHETDATA
        EKSPORTERT_METADATA_VIRKSOMHET -> ImportEksportJobb.EKSPORTERT_METADATA_VIRKSOMHET
        EKSPORTERT_PER_STATISTIKKATEGORI -> ImportEksportJobb.EKSPORTERT_PER_STATISTIKKATEGORI

        // Legacy
        IMPORTERT_NÆRINGSKODEMAPPING, FORBEREDT_NESTE_EKSPORT_LEGACY, EKSPORTERT_LEGACY -> null
    }

    companion object {
        fun fraDomene(importEksportJobb: ImportEksportJobb): FullførteJobber = when (importEksportJobb) {
            ImportEksportJobb.IMPORTERT_STATISTIKK -> IMPORTERT_STATISTIKK
            ImportEksportJobb.IMPORTERT_VIRKSOMHETDATA -> IMPORTERT_VIRKSOMHETDATA
            ImportEksportJobb.EKSPORTERT_METADATA_VIRKSOMHET -> EKSPORTERT_METADATA_VIRKSOMHET
            ImportEksportJobb.EKSPORTERT_PER_STATISTIKKATEGORI -> EKSPORTERT_PER_STATISTIKKATEGORI
        }
    }
}


private fun String.splittTilListe(): List<FullførteJobber> {
    return split(",")
        .mapNotNull { it.ifEmpty { null } }
        .map { FullførteJobber.valueOf(it) }
}