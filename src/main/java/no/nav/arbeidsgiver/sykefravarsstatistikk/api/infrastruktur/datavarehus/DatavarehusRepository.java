package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Sektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.StatistikkildeDvh;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.SykefraværsstatistikkLand;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.SykefraværsstatistikkNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.SykefraværsstatistikkNæringMedVarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.SykefraværsstatistikkSektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.SykefraværsstatistikkVirksomhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.SykefraværsstatistikkVirksomhetMedGradering;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Orgenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.PubliseringsdatoDbDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

@Component
public class DatavarehusRepository {

  public static final String NARINGKODE = "naringkode";
  public static final String NARINGNAVN = "naringnavn";
  public static final String SEKTORKODE = "sektorkode";
  public static final String SEKTORNAVN = "sektornavn";

  public static final String ARSTALL = "arstall";
  public static final String KVARTAL = "kvartal";
  public static final String SEKTOR = "sektor";
  public static final String NARING = "naring";
  public static final String NARING_5SIFFER = "naering_kode";
  public static final String ORGNR = "orgnr";
  public static final String OFFNAVN = "offnavn";
  public static final String VARIGHET = "varighet";
  public static final String SUM_TAPTE_DAGSVERK_GS = "sum_tapte_dagsverk_gs";
  public static final String SUM_ANTALL_PERSONER = "sum_antall_personer";
  public static final String SUM_TAPTE_DAGSVERK = "sum_tapte_dagsverk";
  public static final String SUM_MULIGE_DAGSVERK = "sum_mulige_dagsverk";
  public static final String SUM_ANTALL_GRADERTE_SYKEMELDINGER =
      "sum_antall_graderte_sykemeldinger";
  public static final String SUM_ANTALL_SYKEMELDINGER = "sum_antall_sykemeldinger";
  public static final String RECTYPE = "rectype";

  public static final String RECTYPE_FOR_FORETAK = "1";
  public static final String RECTYPE_FOR_VIRKSOMHET = "2";
  public static final String RECTYPE_FOR_ORGANISASJONSLEDD = "3";

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public DatavarehusRepository(
      @Qualifier("datavarehusJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }

  /*
   Statistikk
  */

  public ÅrstallOgKvartal hentSisteÅrstallOgKvartalForSykefraværsstatistikk(
      StatistikkildeDvh type) {
    List<ÅrstallOgKvartal> alleÅrstallOgKvartal =
        namedParameterJdbcTemplate.query(
            String.format(
                "select distinct arstall, kvartal "
                    + "from %s "
                    + "order by arstall desc, kvartal desc",
                type.tabell),
            new MapSqlParameterSource(),
            (resultSet, rowNum) ->
                new ÅrstallOgKvartal(resultSet.getInt(ARSTALL), resultSet.getInt(KVARTAL)));
    return alleÅrstallOgKvartal.get(0);
  }

  public List<SykefraværsstatistikkLand> hentSykefraværsstatistikkLand(
      ÅrstallOgKvartal årstallOgKvartal) {
    SqlParameterSource namedParameters =
        new MapSqlParameterSource()
            .addValue(ARSTALL, årstallOgKvartal.getÅrstall())
            .addValue(KVARTAL, årstallOgKvartal.getKvartal());

    return namedParameterJdbcTemplate.query(
        "select arstall, kvartal, "
            + "sum(antpers) as sum_antall_personer, "
            + "sum(taptedv) as sum_tapte_dagsverk, "
            + "sum(muligedv) as sum_mulige_dagsverk "
            + "from dt_p.agg_ia_sykefravar_land_v "
            + "where kjonn != 'X' and naring != 'X' "
            + "and arstall = :arstall and kvartal = :kvartal "
            + "group by arstall, kvartal",
        namedParameters,
        (resultSet, rowNum) ->
            new SykefraværsstatistikkLand(
                resultSet.getInt(ARSTALL),
                resultSet.getInt(KVARTAL),
                resultSet.getInt(SUM_ANTALL_PERSONER),
                resultSet.getBigDecimal(SUM_TAPTE_DAGSVERK),
                resultSet.getBigDecimal(SUM_MULIGE_DAGSVERK)));
  }

  public List<SykefraværsstatistikkSektor> hentSykefraværsstatistikkSektor(
      ÅrstallOgKvartal årstallOgKvartal) {
    SqlParameterSource namedParameters =
        new MapSqlParameterSource()
            .addValue(ARSTALL, årstallOgKvartal.getÅrstall())
            .addValue(KVARTAL, årstallOgKvartal.getKvartal());

    return namedParameterJdbcTemplate.query(
        "select arstall, kvartal, sektor, "
            + "sum(antpers) as sum_antall_personer, "
            + "sum(taptedv) as sum_tapte_dagsverk, "
            + "sum(muligedv) as sum_mulige_dagsverk "
            + "from dt_p.agg_ia_sykefravar_land_v "
            + "where kjonn != 'X' and naring != 'X' "
            + "and arstall = :arstall and kvartal = :kvartal "
            + "group by arstall, kvartal, sektor",
        namedParameters,
        (resultSet, rowNum) ->
            new SykefraværsstatistikkSektor(
                resultSet.getInt(ARSTALL),
                resultSet.getInt(KVARTAL),
                resultSet.getString(SEKTOR),
                resultSet.getInt(SUM_ANTALL_PERSONER),
                resultSet.getBigDecimal(SUM_TAPTE_DAGSVERK),
                resultSet.getBigDecimal(SUM_MULIGE_DAGSVERK)));
  }

  public List<SykefraværsstatistikkNæring> hentSykefraværsstatistikkNæring(
      ÅrstallOgKvartal årstallOgKvartal) {
    SqlParameterSource namedParameters =
        new MapSqlParameterSource()
            .addValue(ARSTALL, årstallOgKvartal.getÅrstall())
            .addValue(KVARTAL, årstallOgKvartal.getKvartal());

    return namedParameterJdbcTemplate.query(
        "select arstall, kvartal, naring, "
            + "sum(antpers) as sum_antall_personer, "
            + "sum(taptedv) as sum_tapte_dagsverk, "
            + "sum(muligedv) as sum_mulige_dagsverk "
            + "from dt_p.v_agg_ia_sykefravar_naring "
            + "where kjonn != 'X' and naring != 'X' "
            + "and arstall = :arstall and kvartal = :kvartal "
            + "group by arstall, kvartal, naring",
        namedParameters,
        (resultSet, rowNum) ->
            new SykefraværsstatistikkNæring(
                resultSet.getInt(ARSTALL),
                resultSet.getInt(KVARTAL),
                resultSet.getString(NARING),
                resultSet.getInt(SUM_ANTALL_PERSONER),
                resultSet.getBigDecimal(SUM_TAPTE_DAGSVERK),
                resultSet.getBigDecimal(SUM_MULIGE_DAGSVERK)));
  }

  public List<SykefraværsstatistikkNæring> hentSykefraværsstatistikkNæring5siffer(
      ÅrstallOgKvartal årstallOgKvartal) {
    SqlParameterSource namedParameters =
        new MapSqlParameterSource()
            .addValue(ARSTALL, årstallOgKvartal.getÅrstall())
            .addValue(KVARTAL, årstallOgKvartal.getKvartal());

    return namedParameterJdbcTemplate.query(
        "select arstall, kvartal, naering_kode, "
            + "sum(antpers) as sum_antall_personer, "
            + "sum(taptedv) as sum_tapte_dagsverk, "
            + "sum(muligedv) as sum_mulige_dagsverk "
            + "from dt_p.agg_ia_sykefravar_naring_kode "
            + "where arstall = :arstall and kvartal = :kvartal "
            + " group by arstall, kvartal, naering_kode",
        namedParameters,
        (resultSet, rowNum) ->
            new SykefraværsstatistikkNæring(
                resultSet.getInt(ARSTALL),
                resultSet.getInt(KVARTAL),
                resultSet.getString(NARING_5SIFFER),
                resultSet.getInt(SUM_ANTALL_PERSONER),
                resultSet.getBigDecimal(SUM_TAPTE_DAGSVERK),
                resultSet.getBigDecimal(SUM_MULIGE_DAGSVERK)));
  }

  public List<SykefraværsstatistikkVirksomhet> hentSykefraværsstatistikkVirksomhet(
      ÅrstallOgKvartal årstallOgKvartal) {
    SqlParameterSource namedParameters =
        new MapSqlParameterSource()
            .addValue(ARSTALL, årstallOgKvartal.getÅrstall())
            .addValue(KVARTAL, årstallOgKvartal.getKvartal());

    return namedParameterJdbcTemplate.query(
        "select arstall, kvartal, orgnr, varighet, rectype, "
            + "sum(antpers) as sum_antall_personer, "
            + "sum(taptedv) as sum_tapte_dagsverk, "
            + "sum(muligedv) as sum_mulige_dagsverk "
            + "from dt_p.agg_ia_sykefravar_v "
            + "where arstall = :arstall and kvartal = :kvartal "
            + "group by arstall, kvartal, orgnr, varighet, rectype",
        namedParameters,
        (resultSet, rowNum) ->
            new SykefraværsstatistikkVirksomhet(
                resultSet.getInt(ARSTALL),
                resultSet.getInt(KVARTAL),
                resultSet.getString(ORGNR),
                resultSet.getString(VARIGHET),
                resultSet.getString(RECTYPE),
                resultSet.getInt(SUM_ANTALL_PERSONER),
                resultSet.getBigDecimal(SUM_TAPTE_DAGSVERK),
                resultSet.getBigDecimal(SUM_MULIGE_DAGSVERK)));
  }

  public List<SykefraværsstatistikkNæringMedVarighet> hentSykefraværsstatistikkNæringMedVarighet(
      ÅrstallOgKvartal årstallOgKvartal) {
    SqlParameterSource namedParameters =
        new MapSqlParameterSource()
            .addValue(ARSTALL, årstallOgKvartal.getÅrstall())
            .addValue(KVARTAL, årstallOgKvartal.getKvartal());

    return namedParameterJdbcTemplate.query(
        "select arstall, kvartal, naering_kode, varighet, "
            + "sum(antpers) as sum_antall_personer, "
            + "sum(taptedv) as sum_tapte_dagsverk, "
            + "sum(muligedv) as sum_mulige_dagsverk "
            + "from dt_p.agg_ia_sykefravar_v "
            + "where arstall = :arstall and kvartal = :kvartal and varighet is not null "
            + "and rectype='"
            + RECTYPE_FOR_VIRKSOMHET
            + "'"
            + "group by arstall, kvartal, naering_kode, varighet",
        namedParameters,
        (resultSet, rowNum) ->
            new SykefraværsstatistikkNæringMedVarighet(
                resultSet.getInt(ARSTALL),
                resultSet.getInt(KVARTAL),
                resultSet.getString(NARING_5SIFFER),
                resultSet.getString(VARIGHET),
                resultSet.getInt(SUM_ANTALL_PERSONER),
                resultSet.getBigDecimal(SUM_TAPTE_DAGSVERK),
                resultSet.getBigDecimal(SUM_MULIGE_DAGSVERK)));
  }

  public List<SykefraværsstatistikkVirksomhetMedGradering>
      hentSykefraværsstatistikkVirksomhetMedGradering(ÅrstallOgKvartal årstallOgKvartal) {
    SqlParameterSource namedParameters =
        new MapSqlParameterSource()
            .addValue(ARSTALL, årstallOgKvartal.getÅrstall())
            .addValue(KVARTAL, årstallOgKvartal.getKvartal())
            .addValue(RECTYPE, RECTYPE_FOR_VIRKSOMHET);

    return namedParameterJdbcTemplate.query(
        "select arstall, kvartal, orgnr, naring, naering_kode, rectype, "
            + "sum(taptedv_gs) as sum_tapte_dagsverk_gs, "
            + "sum(antall_gs) as sum_antall_graderte_sykemeldinger, "
            + "sum(antall) as sum_antall_sykemeldinger, "
            + "sum(antpers) as sum_antall_personer, "
            + "sum(taptedv) as sum_tapte_dagsverk, "
            + "sum(mulige_dv) as sum_mulige_dagsverk "
            + "from dt_p.agg_ia_sykefravar_v_2 "
            + "where arstall = :arstall and kvartal = :kvartal "
            + "group by arstall, kvartal, orgnr, naring, naering_kode, rectype",
        namedParameters,
        (resultSet, rowNum) ->
            new SykefraværsstatistikkVirksomhetMedGradering(
                resultSet.getInt(ARSTALL),
                resultSet.getInt(KVARTAL),
                resultSet.getString(ORGNR),
                resultSet.getString(NARING),
                resultSet.getString(NARING_5SIFFER),
                resultSet.getString(RECTYPE),
                resultSet.getInt(SUM_ANTALL_GRADERTE_SYKEMELDINGER),
                resultSet.getBigDecimal(SUM_TAPTE_DAGSVERK_GS),
                resultSet.getInt(SUM_ANTALL_SYKEMELDINGER),
                resultSet.getInt(SUM_ANTALL_PERSONER),
                resultSet.getBigDecimal(SUM_TAPTE_DAGSVERK),
                resultSet.getBigDecimal(SUM_MULIGE_DAGSVERK)));
  }

  /*
   Klassifikasjoner
  */

  public List<Sektor> hentAlleSektorer() {
    SqlParameterSource namedParameters = new MapSqlParameterSource();

    return namedParameterJdbcTemplate.query(
        "select sektorkode, sektornavn from dt_p.v_dim_ia_sektor",
        namedParameters,
        (resultSet, rowNum) ->
            new Sektor(resultSet.getString(SEKTORKODE), resultSet.getString(SEKTORNAVN)));
  }

  public List<Næring> hentAlleNæringer() {
    SqlParameterSource namedParameters = new MapSqlParameterSource();

    return namedParameterJdbcTemplate.query(
        "select naringkode, naringnavn from dt_p.v_dim_ia_naring_sn2007",
        namedParameters,
        (resultSet, rowNum) ->
            new Næring(resultSet.getString(NARINGKODE), resultSet.getString(NARINGNAVN)));
  }

  public List<Orgenhet> hentOrgenheter(ÅrstallOgKvartal årstallOgKvartal) {

    MapSqlParameterSource namedParameters =
        new MapSqlParameterSource()
            .addValue(ARSTALL, årstallOgKvartal.getÅrstall())
            .addValue(KVARTAL, årstallOgKvartal.getKvartal());

    return namedParameterJdbcTemplate.query(
        "select distinct orgnr, rectype, sektor, substr(primærnæringskode, 1,2) as naring, primærnæringskode as naringskode, arstall, kvartal "
            + "from dt_p.agg_ia_sykefravar_v_2 "
            + "where arstall = :arstall and kvartal = :kvartal "
            + "and length(trim(orgnr)) = 9 "
            + "and rectype=2 "
            + "and primærnæringskode is not null "
            + "and primærnæringskode != '00.000'",
        namedParameters,
        ((resultSet, i) ->
            new Orgenhet(
                new Orgnr(resultSet.getString(ORGNR)),
                "", // henter ikke lenger navn for virksomheter, da dette ikke er i bruk
                resultSet.getString(RECTYPE),
                resultSet.getString(SEKTOR),
                resultSet.getString(NARING),
                resultSet.getString("naringskode").replace(".", ""),
                new ÅrstallOgKvartal(resultSet.getInt(ARSTALL), resultSet.getInt(KVARTAL)))));
  }

  public List<PubliseringsdatoDbDto> hentPubliseringsdatoerFraDvh() {
    try {
      return namedParameterJdbcTemplate.query(
          "select rapport_periode, offentlig_dato, oppdatert_dato, aktivitet "
              + "from dk_p.publiseringstabell "
              + "where TABELL_NAVN = 'AGG_FAK_SYKEFRAVAR_DIA' "
              + "and PERIODE_TYPE = 'KVARTAL' "
              + "order by offentlig_dato desc",
          new HashMap<>(),
          (rs, rowNum) ->
              new PubliseringsdatoDbDto(
                  rs.getInt("rapport_periode"),
                  rs.getDate("offentlig_dato"),
                  rs.getDate("oppdatert_dato"),
                  rs.getString("aktivitet")));
    } catch (EmptyResultDataAccessException e) {
      return Collections.emptyList();
    }
  }
}
