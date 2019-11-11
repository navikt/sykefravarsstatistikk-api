package no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.utils;

import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.SykefraværsstatistikkLand;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.CreateSykefraværsstatistikkFunction;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.DeleteSykefraværsstatistikkFunction;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public class SykefraværsstatistikkLandUtils extends SykefraværsstatistikkIntegrasjon
    implements SykefraværsstatistikkIntegrasjonUtils {


  public SykefraværsstatistikkLandUtils(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
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
                      "delete from sykefravar_statistikk_land where arstall = :%s and kvartal = :%s",
                      ARSTALL, KVARTAL),
                  namedParameters);
          return antallSlettet;
        };
    return function;
  }

  @Override
  public CreateSykefraværsstatistikkFunction getCreateFunction() {
    CreateSykefraværsstatistikkFunction<SykefraværsstatistikkLand> function =
        (SykefraværsstatistikkLand sykefraværsstatistikkLand) -> {
          SqlParameterSource namedParameters =
              new MapSqlParameterSource()
                  .addValue(ARSTALL, sykefraværsstatistikkLand.getÅrstall())
                  .addValue(KVARTAL, sykefraværsstatistikkLand.getKvartal())
                  .addValue(ANTALL_PERSONER, sykefraværsstatistikkLand.getAntallPersoner())
                  .addValue(TAPTE_DAGSVERK, sykefraværsstatistikkLand.getTapteDagsverk())
                  .addValue(MULIGE_DAGSVERK, sykefraværsstatistikkLand.getMuligeDagsverk());

          return namedParameterJdbcTemplate.update(
              String.format(
                  "insert into sykefravar_statistikk_land " +
                          "(arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk)  " +
                          "values (:arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                  ARSTALL,
                  KVARTAL,
                  ANTALL_PERSONER,
                  TAPTE_DAGSVERK,
                  MULIGE_DAGSVERK),
              namedParameters);
        };

    return function;
  }
}
