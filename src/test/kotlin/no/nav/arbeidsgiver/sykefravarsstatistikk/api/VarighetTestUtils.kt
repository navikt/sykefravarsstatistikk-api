package no.nav.arbeidsgiver.sykefravarsstatistikk.api

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.Næringskode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.aggregert.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.ÅrstallOgKvartal
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

object VarighetTestUtils {

    fun leggTilVirksomhetsstatistikkMedVarighet(
        jdbcTemplate: NamedParameterJdbcTemplate,
        orgnr: String?,
        årstallOgKvartal: ÅrstallOgKvartal,
        varighetskategori: Varighetskategori,
        antallPersoner: Int,
        tapteDagsverk: Int,
        muligeDagsverk: Int
    ) {
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_virksomhet (arstall, kvartal, orgnr, varighet, "
                    + "antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:arstall, :kvartal, :orgnr, :varighet, :antall_personer, "
                    + ":tapte_dagsverk, :mulige_dagsverk)",
            MapSqlParameterSource()
                .addValue("arstall", årstallOgKvartal.årstall)
                .addValue("kvartal", årstallOgKvartal.kvartal)
                .addValue("orgnr", orgnr)
                .addValue("varighet", varighetskategori.kode)
                .addValue("antall_personer", antallPersoner)
                .addValue("tapte_dagsverk", tapteDagsverk)
                .addValue("mulige_dagsverk", muligeDagsverk)
        )
    }


    fun leggTilStatisitkkNæringMedVarighetForTotalVarighetskategori(
        jdbcTemplate: NamedParameterJdbcTemplate,
        næringskode: Næringskode,
        årstallOgKvartal: ÅrstallOgKvartal,
        antallPersoner: Int,
        muligeDagsverk: Int
    ) {
        leggTilStatisitkkNæringMedVarighet(
            jdbcTemplate,
            næringskode,
            årstallOgKvartal.årstall,
            årstallOgKvartal.kvartal,
            Varighetskategori.TOTAL.kode,
            antallPersoner,
            0,
            muligeDagsverk
        )
    }


    fun leggTilStatisitkkNæringMedVarighet(
        jdbcTemplate: NamedParameterJdbcTemplate,
        næringskode: Næringskode,
        årstallOgKvartal: ÅrstallOgKvartal,
        varighetskategori: Varighetskategori,
        tapteDagsverk: Int
    ) {
        leggTilStatisitkkNæringMedVarighet(
            jdbcTemplate,
            næringskode,
            årstallOgKvartal.årstall,
            årstallOgKvartal.kvartal,
            varighetskategori.kode,
            0,
            tapteDagsverk,
            0
        )
    }

    fun leggTilStatisitkkNæringMedVarighet(
        jdbcTemplate: NamedParameterJdbcTemplate,
        næringskode: Næringskode,
        årstall: Int,
        kvartal: Int,
        varighet: String?,
        antallPersoner: Int,
        tapteDagsverk: Int,
        muligeDagsverk: Int
    ) {
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_naring_med_varighet "
                    + "(arstall, kvartal, naring_kode, varighet, antall_personer, tapte_dagsverk, "
                    + "mulige_dagsverk) "
                    + "VALUES ("
                    + ":arstall, "
                    + ":kvartal, "
                    + ":naring_kode, "
                    + ":varighet, "
                    + ":antall_personer, "
                    + ":tapte_dagsverk, "
                    + ":mulige_dagsverk)",
            MapSqlParameterSource()
                .addValue("arstall", årstall)
                .addValue("kvartal", kvartal)
                .addValue("naring_kode", næringskode.femsifferIdentifikator)
                .addValue("varighet", varighet)
                .addValue("antall_personer", antallPersoner)
                .addValue("tapte_dagsverk", tapteDagsverk)
                .addValue("mulige_dagsverk", muligeDagsverk)
        )
    }
}
