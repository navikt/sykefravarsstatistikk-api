package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.Orgnr;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;

public class TestUtils {

    public static void insertStatistikkForVirksomhet(NamedParameterJdbcTemplate jdbcTemplate, Orgnr orgnr, int årstall, int kvartal, int antallPersoner, BigDecimal tapteDagsverk, int muligeDagsverk) {
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_virksomhet " +
                        "(orgnr, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:orgnr, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametreForStatistikk(årstall, kvartal, antallPersoner, tapteDagsverk, muligeDagsverk)
                        .addValue("orgnr", orgnr.getVerdi())
        );
    }

    private static MapSqlParameterSource parametreForStatistikk(int årstall, int kvartal, int antallPersoner, BigDecimal tapteDagsverk, int muligeDagsverk) {
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
