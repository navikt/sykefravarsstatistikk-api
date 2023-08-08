package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import java.math.BigDecimal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class GraderingTestUtils {

  public static void insertDataMedGradering(
      NamedParameterJdbcTemplate jdbcTemplate,
      String orgnr,
      String næringskodeToSiffer,
      String næringskodeFemSiffer,
      String rectype,
      ÅrstallOgKvartal årstallOgKvartal,
      int antallPersoner,
      BigDecimal tapteDagsverkGradertSykemelding,
      BigDecimal tapteDagsverk,
      BigDecimal muligeDagsverk) {
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
        new MapSqlParameterSource()
            .addValue("orgnr", orgnr)
            .addValue("naring", næringskodeToSiffer)
            .addValue("naring_kode", næringskodeFemSiffer)
            .addValue("rectype", rectype)
            .addValue("arstall", årstallOgKvartal.getÅrstall())
            .addValue("kvartal", årstallOgKvartal.getKvartal())
            .addValue("antall_graderte_sykemeldinger", 0)
            .addValue("tapte_dagsverk_gradert_sykemelding", tapteDagsverkGradertSykemelding)
            .addValue("antall_sykemeldinger", 0)
            .addValue("antall_personer", antallPersoner)
            .addValue("tapte_dagsverk", tapteDagsverk)
            .addValue("mulige_dagsverk", muligeDagsverk));
  }
}
