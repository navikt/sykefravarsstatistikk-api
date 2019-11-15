package no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.utils;

import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.SykefraværsstatistikkVirksomhet;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.CreateSykefraværsstatistikkFunction;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.DeleteSykefraværsstatistikkFunction;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public class SykefraværsstatistikkVirksomhetUtils extends SykefraværsstatistikkIntegrasjon
    implements SykefraværsstatistikkIntegrasjonUtils {


  public SykefraværsstatistikkVirksomhetUtils(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
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
                      "delete from sykefravar_statistikk_virksomhet where arstall = :%s and kvartal = :%s",
                      ARSTALL, KVARTAL),
                  namedParameters);
          return antallSlettet;
        };
    return function;
  }

  @Override
  public CreateSykefraværsstatistikkFunction getCreateFunction() {
    CreateSykefraværsstatistikkFunction<SykefraværsstatistikkVirksomhet> function =
        (SykefraværsstatistikkVirksomhet sykefraværsstatistikkVirksomhet) -> {
          SqlParameterSource namedParameters =
              new MapSqlParameterSource()
                  .addValue(ARSTALL, sykefraværsstatistikkVirksomhet.getÅrstall())
                  .addValue(KVARTAL, sykefraværsstatistikkVirksomhet.getKvartal())
                  .addValue(ORGNR, sykefraværsstatistikkVirksomhet.getOrgnr())
                  .addValue(ANTALL_PERSONER, sykefraværsstatistikkVirksomhet.getAntallPersoner())
                  .addValue(TAPTE_DAGSVERK, sykefraværsstatistikkVirksomhet.getTapteDagsverk())
                  .addValue(MULIGE_DAGSVERK, sykefraværsstatistikkVirksomhet.getMuligeDagsverk());

          return namedParameterJdbcTemplate.update(
              String.format(
                  "insert into sykefravar_statistikk_virksomhet " +
                          "(arstall, kvartal, orgnr, antall_personer, tapte_dagsverk, mulige_dagsverk)  " +
                          "values " +
                          "(:arstall, :kvartal, :orgnr, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                  ARSTALL,
                  KVARTAL,
                  ORGNR,
                  ANTALL_PERSONER,
                  TAPTE_DAGSVERK,
                  MULIGE_DAGSVERK),
              namedParameters);
        };

    return function;
  }
}
