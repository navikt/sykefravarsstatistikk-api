package no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.utils;

import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.SykefraværsstatistikkNæring;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.CreateSykefraværsstatistikkFunction;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.DeleteSykefraværsstatistikkFunction;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public class SykefraværsstatistikkNæringUtils extends SykefraværsstatistikkIntegrasjon
    implements SykefraværsstatistikkIntegrasjonUtils {


  public SykefraværsstatistikkNæringUtils(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
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
                      "delete from sykefravar_statistikk_naring where arstall = :%s and kvartal = :%s",
                      ARSTALL, KVARTAL),
                  namedParameters);
          return antallSlettet;
        };
    return function;
  }

  @Override
  public CreateSykefraværsstatistikkFunction getCreateFunction() {
    CreateSykefraværsstatistikkFunction<SykefraværsstatistikkNæring> function =
        (SykefraværsstatistikkNæring sykefraværsstatistikkNæring) -> {
          SqlParameterSource namedParameters =
              new MapSqlParameterSource()
                  .addValue(ARSTALL, sykefraværsstatistikkNæring.getÅrstall())
                  .addValue(KVARTAL, sykefraværsstatistikkNæring.getKvartal())
                  .addValue(NÆRING_KODE, sykefraværsstatistikkNæring.getNæringkode())
                  .addValue(ANTALL_PERSONER, sykefraværsstatistikkNæring.getAntallPersoner())
                  .addValue(TAPTE_DAGSVERK, sykefraværsstatistikkNæring.getTapteDagsverk())
                  .addValue(MULIGE_DAGSVERK, sykefraværsstatistikkNæring.getMuligeDagsverk());

          return namedParameterJdbcTemplate.update(
              String.format(
                  "insert into sykefravar_statistikk_naring " +
                          "(arstall, kvartal, naring_kode, antall_personer, tapte_dagsverk, mulige_dagsverk)  " +
                          "values " +
                          "(:arstall, :kvartal, :naring_kode, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                  ARSTALL,
                  KVARTAL,
                  NÆRING_KODE,
                  ANTALL_PERSONER,
                  TAPTE_DAGSVERK,
                  MULIGE_DAGSVERK),
              namedParameters);
        };

    return function;
  }
}
