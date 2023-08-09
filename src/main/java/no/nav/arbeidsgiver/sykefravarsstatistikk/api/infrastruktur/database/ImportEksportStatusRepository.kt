package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
open class ImportEksportStatusRepository(
    @param:Qualifier("sykefravarsstatistikkJdbcTemplate") private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
) {


    fun initStatus(årstallOgKvartal: ÅrstallOgKvartal) {
        namedParameterJdbcTemplate.update(
            "insert into import_eksport_status (aarstall, kvartal, importert_statistikk, importert_virksomhetsdata, forberedt_neste_eksport, eksportert_paa_kafka)" +
                    " values (:aarstall, :kvartal, :importertStatistikk, :importertVirksomhetsdata, :forberedtNesteEksport, :eksportertPåKafka)",
            mapOf(
                "aarstall" to årstallOgKvartal.årstall.toString(),
                "kvartal" to årstallOgKvartal.kvartal.toString(),
                "importertStatistikk" to false,
                "importertVirksomhetsdata" to false,
                "forberedtNesteEksport" to false,
                "eksportertPåKafka" to false
            )
        )
    }

    fun hentImportEksportStatus(
        årstallOgKvartal: ÅrstallOgKvartal
    ): List<ImportEksportStatusDao> {
        return namedParameterJdbcTemplate.query(
            "select * from import_eksport_status",
            //mapOf("aarstall" to årstallOgKvartal.årstall.toString(), "kvartal" to årstallOgKvartal.kvartal.toString())
        ) { row, _ ->
            ImportEksportStatusDao(
                årstall = row.getString("aarstall"),
                kvartal = row.getString("kvartal"),
                importertStatistikk = row.getBoolean("importert_statistikk"),
                importertVirksomhetsdata = row.getBoolean("importert_virksomhetsdata"),
                forberedtNesteEksport = row.getBoolean("forberedt_neste_eksport"),
                eksportertPåKafka = row.getBoolean("eksportert_paa_kafka")
            )
        }
    }
}

data class ImportEksportStatusDao(
    val årstall: String,
    val kvartal: String,
    val importertStatistikk: Boolean,
    val importertVirksomhetsdata: Boolean,
    val forberedtNesteEksport: Boolean,
    val eksportertPåKafka: Boolean,
)
