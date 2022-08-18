package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;

public class GraderingTestUtils {

    public static void insertDataMedGradering(
          NamedParameterJdbcTemplate jdbcTemplate,
          String orgnr,
          String næring,
          String næringskode,
          String rectype,
          ÅrstallOgKvartal årstallOgKvartal,
          int antallGraderteSykemeldinger,
          int antallSykemeldinger,
          int antallPersoner,
          BigDecimal tapteDagsverkGradertSykemelding,
          BigDecimal tapteDagsverk,
          BigDecimal muligeDagsverk
    ) {
        jdbcTemplate.update(
              "insert into sykefravar_statistikk_virksomhet_med_gradering (" +
                    "orgnr, " +
                    "naring, " +
                    "naring_kode, " +
                    "rectype, " +
                    "arstall, " +
                    "kvartal," +
                    "antall_graderte_sykemeldinger, " +
                    "tapte_dagsverk_gradert_sykemelding, " +
                    "antall_sykemeldinger, " +
                    "antall_personer, " +
                    "tapte_dagsverk, " +
                    "mulige_dagsverk) "
                    + "VALUES (" +
                    ":orgnr, " +
                    ":naring, " +
                    ":naring_kode, " +
                    ":rectype, " +
                    ":arstall, " +
                    ":kvartal, " +
                    ":antall_graderte_sykemeldinger, " +
                    ":tapte_dagsverk_gradert_sykemelding, " +
                    ":antall_sykemeldinger , " +
                    ":antall_personer, " +
                    ":tapte_dagsverk, " +
                    ":mulige_dagsverk)",
              parametre(
                    orgnr,
                    næring,
                    næringskode,
                    rectype,
                    årstallOgKvartal.getÅrstall(),
                    årstallOgKvartal.getKvartal(),
                    antallGraderteSykemeldinger,
                    tapteDagsverkGradertSykemelding,
                    antallSykemeldinger,
                    antallPersoner,
                    tapteDagsverk,
                    muligeDagsverk
              )
        );
    }

    private static MapSqlParameterSource parametre(
          String orgnr,
          String naring,
          String næringskode,
          String rectype,
          int årstall,
          int kvartal,
          int antallGraderteSykemeldinger,
          BigDecimal tapteDagsverkGradertSykemelding,
          int antallSykemeldinger,
          int antallPersoner,
          BigDecimal tapteDagsverk,
          BigDecimal muligeDagsverk
    ) {
        return new MapSqlParameterSource()
              .addValue("orgnr", orgnr)
              .addValue("naring", naring)
              .addValue("naring_kode", næringskode)
              .addValue("rectype", rectype)
              .addValue("arstall", årstall)
              .addValue("kvartal", kvartal)
              .addValue("antall_graderte_sykemeldinger", antallGraderteSykemeldinger)
              .addValue("tapte_dagsverk_gradert_sykemelding", tapteDagsverkGradertSykemelding)
              .addValue("antall_sykemeldinger", antallSykemeldinger)
              .addValue("antall_personer", antallPersoner)
              .addValue("tapte_dagsverk", tapteDagsverk)
              .addValue("mulige_dagsverk", muligeDagsverk);
    }
}
