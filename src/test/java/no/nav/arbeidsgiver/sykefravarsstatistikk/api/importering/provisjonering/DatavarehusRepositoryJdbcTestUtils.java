package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.SEKTOR;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET;

public class DatavarehusRepositoryJdbcTestUtils {

  public static void cleanUpTestDb(NamedParameterJdbcTemplate jdbcTemplate) {
    delete(jdbcTemplate, "dt_p.v_dim_ia_orgenhet");
    delete(jdbcTemplate, "dt_p.v_dim_ia_naring_sn2007");
    delete(jdbcTemplate, "dt_p.v_dim_ia_sektor");
    delete(jdbcTemplate, "dt_p.agg_ia_sykefravar_land_v");
    delete(jdbcTemplate, "dt_p.agg_ia_sykefravar_v_2");
    delete(jdbcTemplate, "dt_p.agg_ia_sykefravar_v");
  }

  public static int delete(NamedParameterJdbcTemplate jdbcTemplate, String tabell) {
    return jdbcTemplate.update(
        String.format("delete from %s", tabell), new MapSqlParameterSource());
  }

  public static void insertSektorInDvhTabell(
      NamedParameterJdbcTemplate jdbcTemplate, String kode, String navn) {
    MapSqlParameterSource params =
        new MapSqlParameterSource().addValue("sektorkode", kode).addValue("sektornavn", navn);

    jdbcTemplate.update(
        "insert into dt_p.v_dim_ia_sektor (sektorkode, sektornavn) "
            + "values (:sektorkode, :sektornavn)",
        params);
  }

  public static void insertNæringInDvhTabell(
      NamedParameterJdbcTemplate jdbcTemplate,
      String næringkode,
      String næringsgruppekode,
      String næringnavn) {
    MapSqlParameterSource naringParams =
        new MapSqlParameterSource()
            .addValue("naringkode", næringkode)
            .addValue("nargrpkode", næringsgruppekode)
            .addValue("naringnavn", næringnavn);

    jdbcTemplate.update(
        "insert into dt_p.v_dim_ia_naring_sn2007 (naringkode, nargrpkode, naringnavn) "
            + "values (:naringkode, :nargrpkode, :naringnavn)",
        naringParams);
  }

  public static void insertOrgenhetInDvhTabell(
      NamedParameterJdbcTemplate jdbcTemplate,
      String orgnr,
      String sektor,
      String næring,
      String offnavn,
      int årstall,
      int kvartal) {
    MapSqlParameterSource naringParams =
        new MapSqlParameterSource()
            .addValue("orgnr", orgnr)
            .addValue("sektor", sektor)
            .addValue("naring", næring)
            .addValue("offnavn", offnavn)
            .addValue("årstall", årstall)
            .addValue("kvartal", kvartal);

    jdbcTemplate.update(
        "insert into dt_p.v_dim_ia_orgenhet (orgnr, offnavn, rectype, sektor, naring, arstall, kvartal) "
            + "values (:orgnr, :offnavn, '2', :sektor, :naring, :årstall, :kvartal)",
        naringParams);
  }

  public static void insertSykefraværsstatistikkLandInDvhTabell(
      NamedParameterJdbcTemplate jdbcTemplate,
      int årstall,
      int kvartal,
      int antallPersoner,
      long taptedagsverk,
      long muligedagsverk) {
    MapSqlParameterSource params =
        new MapSqlParameterSource()
            .addValue("arstall", årstall)
            .addValue("kvartal", kvartal)
            .addValue("antpers", antallPersoner)
            .addValue("taptedv", taptedagsverk)
            .addValue("muligedv", muligedagsverk);

    jdbcTemplate.update(
        "insert into dt_p.agg_ia_sykefravar_land_v ("
            + "arstall, kvartal, "
            + "naring, naringnavn, "
            + "alder, kjonn, "
            + "fylkbo, fylknavn, "
            + "varighet, sektor, sektornavn, "
            + "taptedv, muligedv, antpers) "
            + "values ("
            + ":arstall, :kvartal, "
            + "'41', 'Bygge- og anleggsvirksomhet', "
            + "'D', 'M', "
            + "'06', 'Buskerud', "
            + "'B', '1', 'Statlig forvaltning', "
            + ":taptedv, :muligedv, :antpers)",
        params);
  }

  public static void insertSykefraværsstatistikkVirksomhetInDvhTabell(
      NamedParameterJdbcTemplate jdbcTemplate,
      int årstall,
      int kvartal,
      int antallPersoner,
      String orgnr,
      String næringskode5siffer,
      Varighetskategori varighet,
      String kjonn,
      long taptedagsverk,
      long muligedagsverk) {
    insertSykefraværsstatistikkVirksomhetInDvhTabell(
        jdbcTemplate,
        årstall,
        kvartal,
        antallPersoner,
        orgnr,
        næringskode5siffer,
        varighet,
        kjonn,
        taptedagsverk,
        muligedagsverk,
        RECTYPE_FOR_VIRKSOMHET);
  }

  public static void insertSykefraværsstatistikkVirksomhetInDvhTabell(
      NamedParameterJdbcTemplate jdbcTemplate,
      int årstall,
      int kvartal,
      int antallPersoner,
      String orgnr,
      String næringskode5siffer,
      Varighetskategori varighet,
      String kjonn,
      long taptedagsverk,
      long muligedagsverk,
      String rectype) {
    MapSqlParameterSource params =
        new MapSqlParameterSource()
            .addValue("arstall", årstall)
            .addValue("kvartal", kvartal)
            .addValue("antpers", antallPersoner)
            .addValue("orgnr", orgnr)
            .addValue("varighet", varighet.kode)
            .addValue("naering_kode", næringskode5siffer)
            .addValue("sektor", SEKTOR)
            .addValue("kjonn", kjonn)
            .addValue("taptedv", taptedagsverk)
            .addValue("muligedv", muligedagsverk)
            .addValue("rectype", rectype);

    jdbcTemplate.update(
        "insert into dt_p.agg_ia_sykefravar_v ("
            + "arstall, kvartal, "
            + "orgnr, naering_kode, sektor, storrelse, fylkarb, "
            + "alder, kjonn,  fylkbo, "
            + "sftype, varighet, "
            + "taptedv, muligedv, antpers, rectype) "
            + "values ("
            + ":arstall, :kvartal, "
            + ":orgnr, :naering_kode, :sektor, 'G', '03', "
            + "'B', :kjonn, '02', "
            + "'L', :varighet, "
            + ":taptedv, :muligedv, :antpers, :rectype)",
        params);
  }

  public static void insertSykefraværsstatistikkNæringInDvhTabell(
      NamedParameterJdbcTemplate jdbcTemplate,
      int årstall,
      int kvartal,
      int antallPersoner,
      String næring,
      String kjonn,
      long taptedagsverk,
      long muligedagsverk) {
    MapSqlParameterSource params =
        new MapSqlParameterSource()
            .addValue("arstall", årstall)
            .addValue("kvartal", kvartal)
            .addValue("antpers", antallPersoner)
            .addValue("naring", næring)
            .addValue("kjonn", kjonn)
            .addValue("taptedv", taptedagsverk)
            .addValue("muligedv", muligedagsverk);

    jdbcTemplate.update(
        "insert into dt_p.v_agg_ia_sykefravar_naring ("
            + "arstall, kvartal, "
            + "naring, "
            + "alder, kjonn, "
            + "taptedv, muligedv, antpers) "
            + "values ("
            + ":arstall, :kvartal, "
            + ":naring, "
            + "'A', :kjonn, "
            + ":taptedv, :muligedv, :antpers)",
        params);
  }

  public static void insertSykefraværsstatistikkNærin5SiffergInDvhTabell(
      NamedParameterJdbcTemplate jdbcTemplate,
      int årstall,
      int kvartal,
      int antallPersoner,
      String næringKode,
      String kjonn,
      long taptedagsverk,
      long muligedagsverk) {
    MapSqlParameterSource params =
        new MapSqlParameterSource()
            .addValue("arstall", årstall)
            .addValue("kvartal", kvartal)
            .addValue("antpers", antallPersoner)
            .addValue("næringKode", næringKode)
            .addValue("kjonn", kjonn)
            .addValue("taptedv", taptedagsverk)
            .addValue("muligedv", muligedagsverk);

    jdbcTemplate.update(
        "insert into dt_p.agg_ia_sykefravar_naring_kode ("
            + "arstall, kvartal, "
            + "naering_kode, "
            + "alder, kjonn, "
            + "taptedv, muligedv, antpers) "
            + "values ("
            + ":arstall, :kvartal, "
            + ":næringKode, "
            + "'A', :kjonn, "
            + ":taptedv, :muligedv, :antpers)",
        params);
  }

  public static void insertSykefraværsstatistikkVirksomhetGraderingInDvhTabell(
      NamedParameterJdbcTemplate jdbcTemplate,
      int årstall,
      int kvartal,
      int antallPersoner,
      String orgnr,
      String næring,
      String næringskode5siffer,
      String alder,
      String kjonn,
      String fylkbo,
      String kommnr,
      long taptedagsverkGradertSykemelding,
      int antallGradertSykemeldinger,
      int antallSykemeldinger,
      long taptedagsverk,
      long muligedagsverk,
      String rectype) {
    MapSqlParameterSource params =
        new MapSqlParameterSource()
            .addValue("arstall", årstall)
            .addValue("kvartal", kvartal)
            .addValue("orgnr", orgnr)
            .addValue("naring", næring)
            .addValue("naering_kode", næringskode5siffer)
            .addValue("alder", alder)
            .addValue("kjonn", kjonn)
            .addValue("fylkbo", fylkbo)
            .addValue("kommnr", kommnr)
            .addValue("rectype", rectype)
            .addValue("antall_gs", antallGradertSykemeldinger)
            .addValue("taptedv_gs", taptedagsverkGradertSykemelding)
            .addValue("antall", antallSykemeldinger)
            .addValue("taptedv", taptedagsverk)
            .addValue("mulige_dv", muligedagsverk)
            .addValue("antpers", antallPersoner);

    jdbcTemplate.update(
        "insert into dt_p.agg_ia_sykefravar_v_2 ("
            + "arstall, kvartal, "
            + "orgnr, naring, naering_kode, "
            + "alder, kjonn,  fylkbo, "
            + "kommnr, rectype, "
            + "antall_gs, taptedv_gs, "
            + "antall, "
            + "taptedv, mulige_dv, antpers) "
            + "values ("
            + ":arstall, :kvartal, "
            + ":orgnr,:naring, :naering_kode, :alder, "
            + ":kjonn, :fylkbo, :kommnr, "
            + ":rectype, "
            + ":antall_gs, :taptedv_gs, "
            + ":antall, "
            + ":taptedv, :mulige_dv, :antpers)",
        params);
  }

  public static void insertSykefraværsstatistikkVirksomhetGraderingInDvhTabell(
      NamedParameterJdbcTemplate namedParameterJdbcTemplate,
      int årstall,
      int kvartal,
      int antallPersoner,
      String orgnrVirksomhet1,
      String næringskode2siffer,
      String næringskode5siffer,
      long tapteDagsverkGradertSykemelding,
      int antallGradertSykemeldinger,
      int antallSykemeldinger,
      long tapteDagsverk,
      long muligeDagsverk) {
    insertSykefraværsstatistikkVirksomhetGraderingInDvhTabell(
        namedParameterJdbcTemplate,
        årstall,
        kvartal,
        antallPersoner,
        orgnrVirksomhet1,
        næringskode2siffer,
        næringskode5siffer,
        "A",
        "M",
        "03",
        "4200",
        tapteDagsverkGradertSykemelding,
        antallGradertSykemeldinger,
        antallSykemeldinger,
        tapteDagsverk,
        muligeDagsverk,
        RECTYPE_FOR_VIRKSOMHET);
  }
}
