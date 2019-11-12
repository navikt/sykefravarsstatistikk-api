package no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.utils;

import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.SykefraværsstatistikkSektor;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.CreateSykefraværsstatistikkFunction;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.DeleteSykefraværsstatistikkFunction;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public class SykefraværsstatistikkSektorUtils extends SykefraværsstatistikkIntegrasjon
    implements SykefraværsstatistikkIntegrasjonUtils {


  public SykefraværsstatistikkSektorUtils(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    super(namedParameterJdbcTemplate);
  }

  @Override
  public DeleteSykefraværsstatistikkFunction getDeleteFunction() {
    DeleteSykefraværsstatistikkFunction function =
        (ÅrstallOgKvartal årstallOgKvartal) -> {
          SqlParameterSource namedParameters =
              new MapSqlParameterSource()
                  .addValue(ARSTALL, årstallOgKvartal.getÅrstall())
                  .addValue(KVARTAL, årstallOgKvartal.getKvartal());

          int antallSlettet =
              namedParameterJdbcTemplate.update(
                  String.format(
                      "delete from sykefravar_statistikk_sektor where arstall = :%s and kvartal = :%s",
                      ARSTALL, KVARTAL),
                  namedParameters);
          return antallSlettet;
        };
    return function;
  }

  @Override
  public CreateSykefraværsstatistikkFunction getCreateFunction() {
    CreateSykefraværsstatistikkFunction<SykefraværsstatistikkSektor> function =
        (SykefraværsstatistikkSektor sykefraværsstatistikkSektor) -> {
          SqlParameterSource namedParameters =
              new MapSqlParameterSource()
                  .addValue(ARSTALL, sykefraværsstatistikkSektor.getÅrstall())
                  .addValue(KVARTAL, sykefraværsstatistikkSektor.getKvartal())
                  .addValue(SEKTOR_KODE, sykefraværsstatistikkSektor.getSektorkode())
                  .addValue(ANTALL_PERSONER, sykefraværsstatistikkSektor.getAntallPersoner())
                  .addValue(TAPTE_DAGSVERK, sykefraværsstatistikkSektor.getTapteDagsverk())
                  .addValue(MULIGE_DAGSVERK, sykefraværsstatistikkSektor.getMuligeDagsverk());

          return namedParameterJdbcTemplate.update(
              String.format(
                  "insert into sykefravar_statistikk_sektor " +
                          "(arstall, kvartal, sektor_kode, antall_personer, tapte_dagsverk, mulige_dagsverk)  " +
                          "values " +
                          "(:arstall, :kvartal, :sektor_kode, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                  ARSTALL,
                  KVARTAL,
                  SEKTOR_KODE,
                  ANTALL_PERSONER,
                  TAPTE_DAGSVERK,
                  MULIGE_DAGSVERK),
              namedParameters);
        };

    return function;
  }
}
