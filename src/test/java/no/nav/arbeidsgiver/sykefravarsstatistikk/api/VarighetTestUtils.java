package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class VarighetTestUtils {

  public static void leggTilVirksomhetsstatistikkMedVarighet(
      NamedParameterJdbcTemplate jdbcTemplate,
      String orgnr,
      ÅrstallOgKvartal årstallOgKvartal,
      Varighetskategori varighetskategori,
      int antallPersoner,
      int tapteDagsverk,
      int muligeDagsverk) {
    jdbcTemplate.update(
        "insert into sykefravar_statistikk_virksomhet (arstall, kvartal, orgnr, varighet, "
            + "antall_personer, tapte_dagsverk, mulige_dagsverk) "
            + "VALUES (:arstall, :kvartal, :orgnr, :varighet, :antall_personer, "
            + ":tapte_dagsverk, :mulige_dagsverk)",
        new MapSqlParameterSource()
            .addValue("arstall", årstallOgKvartal.getÅrstall())
            .addValue("kvartal", årstallOgKvartal.getKvartal())
            .addValue("orgnr", orgnr)
            .addValue("varighet", varighetskategori.kode)
            .addValue("antall_personer", antallPersoner)
            .addValue("tapte_dagsverk", tapteDagsverk)
            .addValue("mulige_dagsverk", muligeDagsverk));
  }

  public static void leggTilStatisitkkNæringMedVarighetForTotalVarighetskategori(
      NamedParameterJdbcTemplate jdbcTemplate,
      Næringskode5Siffer næringskode5Siffer,
      ÅrstallOgKvartal årstallOgKvartal,
      int antallPersoner,
      int muligeDagsverk) {
    leggTilStatisitkkNæringMedVarighet(
        jdbcTemplate,
        næringskode5Siffer,
        årstallOgKvartal.getÅrstall(),
        årstallOgKvartal.getKvartal(),
        Varighetskategori.TOTAL.kode,
        antallPersoner,
        0,
        muligeDagsverk);
  }

  public static void leggTilStatisitkkNæringMedVarighet(
      NamedParameterJdbcTemplate jdbcTemplate,
      Næringskode5Siffer næringskode5Siffer,
      ÅrstallOgKvartal årstallOgKvartal,
      Varighetskategori varighetskategori,
      int tapteDagsverk) {
    leggTilStatisitkkNæringMedVarighet(
        jdbcTemplate,
        næringskode5Siffer,
        årstallOgKvartal.getÅrstall(),
        årstallOgKvartal.getKvartal(),
        varighetskategori.kode,
        0,
        tapteDagsverk,
        0);
  }

  public static void leggTilStatisitkkNæringMedVarighet(
      NamedParameterJdbcTemplate jdbcTemplate,
      Næringskode5Siffer næringskode5Siffer,
      int årstall,
      int kvartal,
      String varighet,
      int antallPersoner,
      int tapteDagsverk,
      int muligeDagsverk) {
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
        new MapSqlParameterSource()
            .addValue("arstall", årstall)
            .addValue("kvartal", kvartal)
            .addValue("naring_kode", næringskode5Siffer.getKode())
            .addValue("varighet", varighet)
            .addValue("antall_personer", antallPersoner)
            .addValue("tapte_dagsverk", tapteDagsverk)
            .addValue("mulige_dagsverk", muligeDagsverk));
  }
}
