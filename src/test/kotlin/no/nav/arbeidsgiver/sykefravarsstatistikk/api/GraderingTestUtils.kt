package no.nav.arbeidsgiver.sykefravarsstatistikk.api

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.math.BigDecimal

object GraderingTestUtils {

    fun insertDataMedGradering(
        jdbcTemplate: NamedParameterJdbcTemplate,
        orgnr: String?,
        næring: String?,
        næringskode: String?,
        rectype: String?,
        årstallOgKvartal: ÅrstallOgKvartal,
        antallPersoner: Int,
        tapteDagsverkGradertSykemelding: BigDecimal?,
        tapteDagsverk: BigDecimal?,
        muligeDagsverk: BigDecimal?
    ) {
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_virksomhet_med_gradering ("
                    + "orgnr, "
                    + "naring, "
                    + "naring_kode, "
                    + "rectype, "
                    + "arstall, "
                    + "kvartal,"
                    + "antall_graderte_sykemeldinger, "
                    + "tapte_dagsverk_gradert_sykemelding, "
                    + "antall_sykemeldinger, "
                    + "antall_personer, "
                    + "tapte_dagsverk, "
                    + "mulige_dagsverk) "
                    + "VALUES ("
                    + ":orgnr, "
                    + ":naring, "
                    + ":naring_kode, "
                    + ":rectype, "
                    + ":arstall, "
                    + ":kvartal, "
                    + ":antall_graderte_sykemeldinger, "
                    + ":tapte_dagsverk_gradert_sykemelding, "
                    + ":antall_sykemeldinger , "
                    + ":antall_personer, "
                    + ":tapte_dagsverk, "
                    + ":mulige_dagsverk)",
            MapSqlParameterSource()
                .addValue("orgnr", orgnr)
                .addValue("naring", næring)
                .addValue("naring_kode", næringskode)
                .addValue("rectype", rectype)
                .addValue("arstall", årstallOgKvartal.årstall)
                .addValue("kvartal", årstallOgKvartal.kvartal)
                .addValue("antall_graderte_sykemeldinger", 0)
                .addValue("tapte_dagsverk_gradert_sykemelding", tapteDagsverkGradertSykemelding)
                .addValue("antall_sykemeldinger", 0)
                .addValue("antall_personer", antallPersoner)
                .addValue("tapte_dagsverk", tapteDagsverk)
                .addValue("mulige_dagsverk", muligeDagsverk)
        )
    }
}
