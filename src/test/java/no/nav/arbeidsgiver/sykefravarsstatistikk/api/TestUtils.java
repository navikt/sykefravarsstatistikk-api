package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class TestUtils {

    /**
     * H2 DB og PostgreSQL har forskjellige syntax for ID (primary key)
     * H2 DB bruker 'bigint auto_increment' hvor PostgreSQL bruker 'serial'
     * Denne metoden endrer feltet ID ut i fra migreringsscript slik at H2 klarer å auto-increment feltet ID ved 'insert'
     * OBS: dette gjelder ikke applikasjon når den kjører lokalt: da er H2 DB startet med dialect PostgreSQL
     */
    public static void setAutoincrementPrimaryKeyForH2Db(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            String tabell
    ) {
        namedParameterJdbcTemplate.getJdbcTemplate().execute(String.format("alter table %s drop column id", tabell));
        namedParameterJdbcTemplate.getJdbcTemplate().execute(
                String.format(
                        "alter table %s add id bigint auto_increment",
                        tabell
                )
        );
    }

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

    public static void slettAllEksportDataFraDatabase(NamedParameterJdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("delete from virksomhet_metadata", new MapSqlParameterSource());
        jdbcTemplate.update("delete from eksport_per_kvartal", new MapSqlParameterSource());
        jdbcTemplate.update("delete from kafka_utsending_historikk", new MapSqlParameterSource());
        jdbcTemplate.update("delete from virksomheter_bekreftet_eksportert", new MapSqlParameterSource());
    }
}
