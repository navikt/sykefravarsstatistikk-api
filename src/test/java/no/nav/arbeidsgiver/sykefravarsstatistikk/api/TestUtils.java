package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class TestUtils {

    public static MapSqlParameterSource parametreForStatistikk(int årstall, int kvartal, int antallPersoner, int tapteDagsverk, int muligeDagsverk) {
        return new MapSqlParameterSource()
                .addValue("arstall", årstall)
                .addValue("kvartal", kvartal)
                .addValue("antall_personer", antallPersoner)
                .addValue("tapte_dagsverk", tapteDagsverk)
                .addValue("mulige_dagsverk", muligeDagsverk);
    }

    public static void slettAllStatistikkFraDatabase(NamedParameterJdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("delete from sykefravar_statistikk_virksomhet", new MapSqlParameterSource());
        jdbcTemplate.update("delete from sykefravar_statistikk_naring", new MapSqlParameterSource());
        jdbcTemplate.update("delete from sykefravar_statistikk_naring5siffer", new MapSqlParameterSource());
        jdbcTemplate.update("delete from sykefravar_statistikk_sektor", new MapSqlParameterSource());
        jdbcTemplate.update("delete from sykefravar_statistikk_land", new MapSqlParameterSource());
        jdbcTemplate.update("delete from naring", new MapSqlParameterSource());
    }
}
