package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
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
        jdbcTemplate.update("delete from sykefravar_statistikk_naring_med_varighet", new MapSqlParameterSource());
        jdbcTemplate.update("delete from sykefravar_statistikk_virksomhet_med_gradering", new MapSqlParameterSource());
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

    public static void oppretteStatistikkForLand(NamedParameterJdbcTemplate jdbcTemplate) {
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametreForStatistikk(ÅrstallOgKvartal.SISTE_PUBLISERTE_KVARTAL.getÅrstall(), ÅrstallOgKvartal.SISTE_PUBLISERTE_KVARTAL.getKvartal(), 10, 4, 100)
        );
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametreForStatistikk(ÅrstallOgKvartal.SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).getÅrstall(), ÅrstallOgKvartal.SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).getKvartal(), 10, 5, 100)
        );
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametreForStatistikk(ÅrstallOgKvartal.SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).getÅrstall(), ÅrstallOgKvartal.SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).getKvartal(), 10, 6, 100)
        );

    }
}
