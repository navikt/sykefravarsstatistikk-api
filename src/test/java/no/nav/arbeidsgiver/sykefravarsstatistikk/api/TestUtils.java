package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.statistikk.SykefraværsstatistikkSektorUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public class TestUtils {

  public static final Næring PRODUKSJON_NYTELSESMIDLER =
      new Næring("10", "Produksjon av nærings- og nytelsesmidler");

  public static final ÅrstallOgKvartal SISTE_PUBLISERTE_KVARTAL = new ÅrstallOgKvartal(2022, 1);

  public static ÅrstallOgKvartal sisteKvartalMinus(int n) {
    return SISTE_PUBLISERTE_KVARTAL.minusKvartaler(n);
  }

  public static MapSqlParameterSource parametreForStatistikk(
      int årstall, int kvartal, int antallPersoner, int tapteDagsverk, int muligeDagsverk) {
    return new MapSqlParameterSource()
        .addValue("arstall", årstall)
        .addValue("kvartal", kvartal)
        .addValue("antall_personer", antallPersoner)
        .addValue("tapte_dagsverk", tapteDagsverk)
        .addValue("mulige_dagsverk", muligeDagsverk);
  }

  public static void slettAllStatistikkFraDatabase(NamedParameterJdbcTemplate jdbcTemplate) {
    jdbcTemplate.update(
        "delete from sykefravar_statistikk_virksomhet", new MapSqlParameterSource());
    jdbcTemplate.update("delete from sykefravar_statistikk_naring", new MapSqlParameterSource());
    jdbcTemplate.update(
        "delete from sykefravar_statistikk_naring_med_varighet", new MapSqlParameterSource());
    jdbcTemplate.update(
        "delete from sykefravar_statistikk_virksomhet_med_gradering", new MapSqlParameterSource());
    jdbcTemplate.update(
        "delete from sykefravar_statistikk_naring5siffer", new MapSqlParameterSource());
    jdbcTemplate.update("delete from sykefravar_statistikk_sektor", new MapSqlParameterSource());
    jdbcTemplate.update("delete from sykefravar_statistikk_land", new MapSqlParameterSource());
  }

  public static void slettAllEksportDataFraDatabase(NamedParameterJdbcTemplate jdbcTemplate) {
    jdbcTemplate.update("delete from virksomhet_metadata", new MapSqlParameterSource());
    jdbcTemplate.update("delete from eksport_per_kvartal", new MapSqlParameterSource());
    jdbcTemplate.update("delete from kafka_utsending_historikk", new MapSqlParameterSource());
    jdbcTemplate.update(
        "delete from virksomheter_bekreftet_eksportert", new MapSqlParameterSource());
  }

  public static void opprettTestVirksomhetMetaData(
      NamedParameterJdbcTemplate jdbcTemplate, int årstall, int kvartal, String orgnr) {
    opprettTestVirksomhetMetaData(jdbcTemplate, årstall, kvartal, orgnr, false);
  }

  public static int opprettTestVirksomhetMetaData(
      NamedParameterJdbcTemplate jdbcTemplate,
      int årstall,
      int kvartal,
      String orgnr,
      boolean eksportert) {
    SqlParameterSource parametre =
        new MapSqlParameterSource()
            .addValue("orgnr", orgnr)
            .addValue("årstall", årstall)
            .addValue("kvartal", kvartal)
            .addValue("eksportert", eksportert);
    return jdbcTemplate.update(
        "insert into eksport_per_kvartal "
            + "(orgnr, arstall, kvartal, eksportert) "
            + "values "
            + "(:orgnr, :årstall, :kvartal, :eksportert)",
        parametre);
  }

  public static void opprettStatistikkForLand(NamedParameterJdbcTemplate jdbcTemplate) {
    jdbcTemplate.update(
        "insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, "
            + "tapte_dagsverk, mulige_dagsverk) "
            + "VALUES (:arstall, :kvartal, :antall_personer, :tapte_dagsverk, "
            + ":mulige_dagsverk)",
        parametreForStatistikk(
            SISTE_PUBLISERTE_KVARTAL.getÅrstall(),
            SISTE_PUBLISERTE_KVARTAL.getKvartal(),
            10,
            4,
            100));
    jdbcTemplate.update(
        "insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, "
            + "tapte_dagsverk, mulige_dagsverk) "
            + "VALUES (:arstall, :kvartal, :antall_personer, :tapte_dagsverk, "
            + ":mulige_dagsverk)",
        parametreForStatistikk(
            SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).getÅrstall(),
            SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).getKvartal(),
            10,
            5,
            100));
    jdbcTemplate.update(
        "insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, "
            + "tapte_dagsverk, mulige_dagsverk) "
            + "VALUES (:arstall, :kvartal, :antall_personer, :tapte_dagsverk, "
            + ":mulige_dagsverk)",
        parametreForStatistikk(
            SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).getÅrstall(),
            SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).getKvartal(),
            10,
            6,
            100));
  }

  public static void opprettStatistikkForSektor(NamedParameterJdbcTemplate jdbcTemplate) {

    new SykefraværsstatistikkSektorUtils(jdbcTemplate)
        .getBatchCreateFunction(
            List.of(
                new SykefraværsstatistikkSektor(
                    SISTE_PUBLISERTE_KVARTAL.getÅrstall(),
                    SISTE_PUBLISERTE_KVARTAL.getKvartal(),
                    "1",
                    10,
                    new BigDecimal("657853.346702"),
                    new BigDecimal("13558710.866603"))))
        .apply();
  }

  public static void opprettStatistikkForNæring5Siffer(
          NamedParameterJdbcTemplate jdbcTemplate,
          BedreNæringskode næringskode5Siffer,
          int årstall,
          int kvartal,
          int tapteDagsverk,
          int muligeDagsverk,
          int antallPersoner) {

    MapSqlParameterSource parametre =
        parametreForStatistikk(årstall, kvartal, antallPersoner, tapteDagsverk, muligeDagsverk);
    parametre.addValue("naring_kode", næringskode5Siffer.getFemsifferIdentifikator());
    jdbcTemplate.update(
        "insert into sykefravar_statistikk_naring5siffer "
            + "(naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, "
            + "mulige_dagsverk) "
            + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, "
            + ":tapte_dagsverk, :mulige_dagsverk)",
        parametre);
  }

  public static void opprettStatistikkForNæringer(NamedParameterJdbcTemplate jdbcTemplate) {
    opprettStatistikkForNæring(
        jdbcTemplate,
        new Næring("10", ""),
        SISTE_PUBLISERTE_KVARTAL.getÅrstall(),
        SISTE_PUBLISERTE_KVARTAL.getKvartal(),
        20000,
        1000000,
        50);
    opprettStatistikkForNæring(
        jdbcTemplate,
        new Næring("10", ""),
        SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).getÅrstall(),
        SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).getKvartal(),
        30000,
        1000000,
        50);
    opprettStatistikkForNæring(
        jdbcTemplate,
        new Næring("10", ""),
        SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).getÅrstall(),
        SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).getKvartal(),
        40000,
        1000000,
        50);
    opprettStatistikkForNæring(
        jdbcTemplate,
        new Næring("10", ""),
        SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3).getÅrstall(),
        SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3).getKvartal(),
        50000,
        1000000,
        50);
    opprettStatistikkForNæring(
        jdbcTemplate,
        new Næring("10", ""),
        SISTE_PUBLISERTE_KVARTAL.minusKvartaler(4).getÅrstall(),
        SISTE_PUBLISERTE_KVARTAL.minusKvartaler(4).getKvartal(),
        60000,
        1000000,
        50);
    opprettStatistikkForNæring(
        jdbcTemplate,
        new Næring("88", ""),
        SISTE_PUBLISERTE_KVARTAL.getÅrstall(),
        SISTE_PUBLISERTE_KVARTAL.getKvartal(),
        25000,
        1000000,
        50);
    opprettStatistikkForNæring(
        jdbcTemplate,
        new Næring("88", ""),
        SISTE_PUBLISERTE_KVARTAL.minusEttÅr().getÅrstall(),
        SISTE_PUBLISERTE_KVARTAL.minusEttÅr().getKvartal(),
        30000,
        1000000,
        50);
  }

  public static void opprettStatistikkForNæring(
      NamedParameterJdbcTemplate jdbcTemplate,
      Næring næring,
      int årstall,
      int kvartal,
      int tapteDagsverk,
      int muligeDagsverk,
      int antallPersoner) {

    MapSqlParameterSource parametre =
        parametreForStatistikk(årstall, kvartal, antallPersoner, tapteDagsverk, muligeDagsverk);
    parametre.addValue("naring_kode", næring.getKode());
    jdbcTemplate.update(
        "insert into sykefravar_statistikk_naring "
            + "(naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, "
            + "mulige_dagsverk) "
            + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, "
            + ":tapte_dagsverk, :mulige_dagsverk)",
        parametre);
  }

  public static void opprettStatistikkForVirksomhet(
      NamedParameterJdbcTemplate jdbcTemplate,
      String orgnr,
      int årstall,
      int kvartal,
      int tapteDagsverk,
      int muligeDagsverk,
      int antallPersoner) {

    MapSqlParameterSource parametre =
        parametreForStatistikk(årstall, kvartal, antallPersoner, tapteDagsverk, muligeDagsverk);
    parametre.addValue("orgnr", orgnr);
    parametre.addValue("varighet", "A");

    jdbcTemplate.update(
        "insert into sykefravar_statistikk_virksomhet (arstall, kvartal, orgnr, varighet,"
            + " antall_personer, tapte_dagsverk, mulige_dagsverk) "
            + "VALUES (:arstall, :kvartal, :orgnr, :varighet, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
        parametre);
  }

  public static void slettAlleImporttidspunkt(NamedParameterJdbcTemplate jdbcTemplate) {
    jdbcTemplate.update("delete from importtidspunkt", new MapSqlParameterSource());
  }

  public static void skrivSisteImporttidspunktTilDb(NamedParameterJdbcTemplate jdbcTemplate) {
    skrivImporttidspunktTilDb(
        jdbcTemplate,
        new ImporttidspunktDto(
            Timestamp.valueOf("2022-06-02 10:00:00.0"), SISTE_PUBLISERTE_KVARTAL));
  }

  public static List<KafkaUtsendingHistorikkData> hentAlleKafkaUtsendingHistorikkData(
      NamedParameterJdbcTemplate jdbcTemplate) {
    return jdbcTemplate.query(
        "select orgnr, key_json, value_json, opprettet " + "from kafka_utsending_historikk ",
        new MapSqlParameterSource(),
        (resultSet, rowNum) ->
            new KafkaUtsendingHistorikkData(
                resultSet.getString("orgnr"),
                resultSet.getString("key_json"),
                resultSet.getString("value_json"),
                resultSet.getTimestamp("opprettet").toLocalDateTime()));
  }

  public static void opprettUtsendingHistorikk(
      NamedParameterJdbcTemplate jdbcTemplate,
      KafkaUtsendingHistorikkData kafkaUtsendingHistorikkData) {
    MapSqlParameterSource parametre = new MapSqlParameterSource();
    parametre.addValue("orgnr", kafkaUtsendingHistorikkData.orgnr);
    parametre.addValue("key", kafkaUtsendingHistorikkData.key);
    parametre.addValue("value", kafkaUtsendingHistorikkData.value);

    jdbcTemplate.update(
        "insert into kafka_utsending_historikk (orgnr, key_json, value_json) "
            + "VALUES (:orgnr, :key, :value)",
        parametre);
  }

  private static void skrivImporttidspunktTilDb(
      NamedParameterJdbcTemplate jdbcTemplate, ImporttidspunktDto importtidspunkt) {
    jdbcTemplate.update(
        "insert into importtidspunkt (aarstall, kvartal, importert) values "
            + "(:aarstall, :kvartal, :importert)",
        new MapSqlParameterSource()
            .addValue("aarstall", importtidspunkt.getGjeldendePeriode().getÅrstall())
            .addValue("kvartal", importtidspunkt.getGjeldendePeriode().getKvartal())
            .addValue("importert", importtidspunkt.getImportertDato()));
  }
}
