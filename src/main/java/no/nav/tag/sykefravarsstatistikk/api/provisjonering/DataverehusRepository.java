package no.nav.tag.sykefravarsstatistikk.api.provisjonering;

import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.*;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataverehusRepository {

    public static final String NARINGKODE = "naringkode";
    public static final String NARINGNAVN = "naringnavn";
    public static final String SEKTORKODE = "sektorkode";
    public static final String SEKTORNAVN = "sektornavn";


    public static final String ARSTALL = "arstall";
    public static final String KVARTAL = "kvartal";
    public static final String SEKTOR = "sektor";
    public static final String NARING = "naring";
    public static final String ORGNR = "orgnr";
    public static final String SUM_ANTALL_PERSONER = "sum_antall_personer";
    public static final String SUM_TAPTE_DAGSVERK = "sum_tapte_dagsverk";
    public static final String SUM_MULIGE_DAGSVERK = "sum_mulige_dagsverk";


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
                "select arstall, kvartal, " +
                        "sum(antpers) as sum_antall_personer, " +
                        "sum(taptedv) as sum_tapte_dagsverk, " +
                        "sum(muligedv) as sum_mulige_dagsverk " +
                        "from dt_p.v_agg_ia_sykefravar_land " +
                        "where kjonn != 'X' and naring != 'X' " +
                        "and arstall = :arstall and kvartal = :kvartal " +
                        "group by arstall, kvartal",
                namedParameters,
                (resultSet, rowNum) ->
                        new SykefraværsstatistikkLand(
                                resultSet.getInt(ARSTALL),
                                resultSet.getInt(KVARTAL),
                                resultSet.getInt(SUM_ANTALL_PERSONER),
                                resultSet.getBigDecimal(SUM_TAPTE_DAGSVERK),
                                resultSet.getBigDecimal(SUM_MULIGE_DAGSVERK)));
    }

    public List<SykefraværsstatistikkSektor> hentSykefraværsstatistikkSektor(ÅrstallOgKvartal årstallOgKvartal) {
        SqlParameterSource namedParameters =
                new MapSqlParameterSource()
                        .addValue(ARSTALL, årstallOgKvartal.getÅrstall())
                        .addValue(KVARTAL, årstallOgKvartal.getKvartal());

        return namedParameterJdbcTemplate.query(
                "select arstall, kvartal, sektor, " +
                        "sum(antpers) as sum_antall_personer, " +
                        "sum(taptedv) as sum_tapte_dagsverk, " +
                        "sum(muligedv) as sum_mulige_dagsverk " +
                        "from dt_p.v_agg_ia_sykefravar_land " +
                        "where kjonn != 'X' and naring != 'X' " +
                        "and arstall = :arstall and kvartal = :kvartal " +
                        "group by arstall, kvartal, sektor",
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

    public List<SykefraværsstatistikkNæring> hentSykefraværsstatistikkNæring(ÅrstallOgKvartal årstallOgKvartal) {
        SqlParameterSource namedParameters =
                new MapSqlParameterSource()
                        .addValue(ARSTALL, årstallOgKvartal.getÅrstall())
                        .addValue(KVARTAL, årstallOgKvartal.getKvartal());

        return namedParameterJdbcTemplate.query(
                "select arstall, kvartal, naring, " +
                        "sum(antpers) as sum_antall_personer, " +
                        "sum(taptedv) as sum_tapte_dagsverk, " +
                        "sum(muligedv) as sum_mulige_dagsverk " +
                        "from dt_p.v_agg_ia_sykefravar_naring " +
                        "where kjonn != 'X' and naring != 'X' " +
                        "and arstall = :arstall and kvartal = :kvartal " +
                        "group by arstall, kvartal, naring",
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

    public List<SykefraværsstatistikkVirksomhet> hentSykefraværsstatistikkVirksomhet(ÅrstallOgKvartal årstallOgKvartal) {
        SqlParameterSource namedParameters =
                new MapSqlParameterSource()
                        .addValue(ARSTALL, årstallOgKvartal.getÅrstall())
                        .addValue(KVARTAL, årstallOgKvartal.getKvartal());

        return namedParameterJdbcTemplate.query(
                "select arstall, kvartal, orgnr, " +
                        "sum(antpers) as sum_antall_personer, " +
                        "sum(taptedv) as sum_tapte_dagsverk, " +
                        "sum(muligedv) as sum_mulige_dagsverk " +
                        "from dt_p.v_agg_ia_sykefravar " +
                        "where kjonn != 'X' and naring != 'X' " +
                        "and arstall = :arstall and kvartal = :kvartal " +
                        "group by arstall, kvartal, orgnr",
                namedParameters,
                (resultSet, rowNum) ->
                        new SykefraværsstatistikkVirksomhet(
                                resultSet.getInt(ARSTALL),
                                resultSet.getInt(KVARTAL),
                                resultSet.getString(ORGNR),
                                resultSet.getInt(SUM_ANTALL_PERSONER),
                                resultSet.getBigDecimal(SUM_TAPTE_DAGSVERK),
                                resultSet.getBigDecimal(SUM_MULIGE_DAGSVERK)));
    }


    /*
     Dimensjoner
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
                        new Næring(
                                resultSet.getString(NARINGKODE),
                                resultSet.getString(NARINGNAVN)));
    }
}
