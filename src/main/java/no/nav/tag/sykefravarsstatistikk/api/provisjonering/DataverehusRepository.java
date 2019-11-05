package no.nav.tag.sykefravarsstatistikk.api.provisjonering;

import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.SykefraværsstatistikkLand;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næringsgruppe;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.util.List;

@Profile({"local", "dev"})
@Component
public class DataverehusRepository {

  public static final String NARGRPKODE = "NARGRPKODE";
  public static final String NARGRPNAVN = "NARGRPNAVN";
  public static final String NARINGKODE = "NARINGKODE";
  public static final String NARINGNAVN = "NARINGNAVN";
  public static final String SEKTORKODE = "SEKTORKODE";
  public static final String SEKTORNAVN = "SEKTORNAVN";


    public static final String ARSTALL = "ARSTALL";
    public static final String KVARTAL = "KVARTAL";
    public static final String SUM_TAPTE_DAGSVERK = "SUM_TAPTE_DAGSVERK";
    public static final String SUM_MULIGE_DAGSVERK = "SUM_MULIGE_DAGSVERK";
    public static final String SUM_ANTALL_PERSONER = "SUM_ANTALL_PERSONER";


    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public DataverehusRepository(
      @Qualifier("datavarehusJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }


  /*
   Statistikk
  */

  public List<SykefraværsstatistikkLand> hentSykefraværsstatistikkLand(ÅrstallOgKvartal årstallOgKvartal) {
    SqlParameterSource namedParameters =
        new MapSqlParameterSource()
            .addValue(ARSTALL, årstallOgKvartal.getÅrstall())
            .addValue(KVARTAL, årstallOgKvartal.getKvartal());

    return namedParameterJdbcTemplate.query(
        "select ARSTALL, KVARTAL, sum(TAPTEDV) as SUM_TAPTE_DAGSVERK, sum(MULIGEDV) as SUM_MULIGE_DAGSVERK, " +
                " sum(antpers) as SUM_ANTALL_PERSONER from dt_p.V_AGG_IA_SYKEFRAVAR_LAND "
            + "where KJONN != 'X' AND NARING != 'X' "
            + "AND arstall = :ARSTALL and kvartal = :KVARTAL "
            + "GROUP BY arstall, kvartal ORDER BY arstall, kvartal;",
        namedParameters,
        (resultSet, rowNum) ->
            new SykefraværsstatistikkLand(
                resultSet.getInt(ARSTALL),
                resultSet.getInt(KVARTAL),
                resultSet.getBigDecimal(SUM_TAPTE_DAGSVERK),
                resultSet.getBigDecimal(SUM_MULIGE_DAGSVERK)));
  }

  /*
   Dimensjoner
  */

  public List<Sektor> hentAlleSektorer() {
    SqlParameterSource namedParameters = new MapSqlParameterSource();

    return namedParameterJdbcTemplate.query(
        "select SEKTORKODE, SEKTORNAVN from dt_p.V_DIM_IA_SEKTOR",
        namedParameters,
        (resultSet, rowNum) ->
            new Sektor(resultSet.getString(SEKTORKODE), resultSet.getString(SEKTORNAVN)));
  }

  public List<Næringsgruppe> hentAlleNæringsgrupper() {
    SqlParameterSource namedParameters = new MapSqlParameterSource();

    return namedParameterJdbcTemplate.query(
        "select NARGRPKODE, NARGRPNAVN from dt_p.V_DIM_IA_FGRP_NARING_SN2007",
        namedParameters,
        (resultSet, rowNum) ->
            new Næringsgruppe(resultSet.getString(NARGRPKODE), resultSet.getString(NARGRPNAVN)));
  }

  public List<Næring> hentAlleNæringer() {
    SqlParameterSource namedParameters = new MapSqlParameterSource();

    return namedParameterJdbcTemplate.query(
        "select NARINGKODE, NARGRPKODE, NARINGNAVN from dt_p.V_DIM_IA_NARING_SN2007",
        namedParameters,
        (resultSet, rowNum) ->
            new Næring(
                resultSet.getString(NARGRPKODE),
                resultSet.getString(NARINGKODE),
                resultSet.getString(NARINGNAVN)));
  }
}
