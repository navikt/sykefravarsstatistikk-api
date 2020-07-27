package no.nav.arbeidsgiver.sykefravarsstatistikk.api.provisjonering;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næringsgruppering;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
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
    public static final String NARING_5SIFFER = "naering_kode";
    public static final String ORGNR = "orgnr";
    public static final String SUM_ANTALL_PERSONER = "sum_antall_personer";
    public static final String SUM_TAPTE_DAGSVERK = "sum_tapte_dagsverk";
    public static final String SUM_MULIGE_DAGSVERK = "sum_mulige_dagsverk";


    public static final String NAERING_KODE = "naering_kode";
    public static final String NAERING_BESKRIVELSE = "naering_besk_lang";
    public static final String GRUPPE1_KODE = "gruppe1_kode";
    public static final String GRUPPE1_BESKRIVELSE = "gruppe1_besk_lang";
    public static final String GRUPPE2_KODE = "gruppe2_kode";
    public static final String GRUPPE2_BESKRIVELSE = "gruppe2_besk_lang";
    public static final String GRUPPE3_KODE = "gruppe3_kode";
    public static final String GRUPPE3_BESKRIVELSE = "gruppe3_besk_lang";
    public static final String GRUPPE4_KODE = "gruppe4_kode";
    public static final String GRUPPE4_BESKRIVELSE = "gruppe4_besk_lang";


    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public DataverehusRepository(
            @Qualifier("datavarehusJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }


  /*
   Statistikk
  */

    public ÅrstallOgKvartal hentSisteÅrstallOgKvartalForSykefraværsstatistikk(Statistikkkilde type) {
        List<ÅrstallOgKvartal> alleÅrstallOgKvartal = namedParameterJdbcTemplate.query(
                String.format("select distinct arstall, kvartal " +
                        "from %s " +
                        "order by arstall desc, kvartal desc", type.tabell),
                new MapSqlParameterSource(),
                (resultSet, rowNum) ->
                        new ÅrstallOgKvartal(
                                resultSet.getInt(ARSTALL),
                                resultSet.getInt(KVARTAL)
                        )
        );
        return alleÅrstallOgKvartal.get(0);
    }


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
                        "from dt_p.agg_ia_sykefravar_land_v " +
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
                        "from dt_p.agg_ia_sykefravar_land_v " +
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

    public List<SykefraværsstatistikkNæring> hentSykefraværsstatistikkNæring5siffer(ÅrstallOgKvartal årstallOgKvartal) {
        SqlParameterSource namedParameters =
                new MapSqlParameterSource()
                        .addValue(ARSTALL, årstallOgKvartal.getÅrstall())
                        .addValue(KVARTAL, årstallOgKvartal.getKvartal());

        return namedParameterJdbcTemplate.query(
                "select arstall, kvartal, naering_kode, " +
                        "sum(antpers) as sum_antall_personer, " +
                        "sum(taptedv) as sum_tapte_dagsverk, " +
                        "sum(muligedv) as sum_mulige_dagsverk " +
                        "from dt_p.agg_ia_sykefravar_naring_kode " +
                        "where arstall = :arstall and kvartal = :kvartal " +
                        "group by arstall, kvartal, naering_kode",
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
                        "from dt_p.agg_ia_sykefravar_v " +
                        "where arstall = :arstall and kvartal = :kvartal " +
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

    public List<Næringsgruppering> hentAlleNæringsgrupperinger() {
        SqlParameterSource namedParameters = new MapSqlParameterSource();

        return namedParameterJdbcTemplate.query(
                "select naering_kode, naering_besk_lang, gruppe1_kode, gruppe1_besk_lang, gruppe2_kode, gruppe2_besk_lang, gruppe3_kode, gruppe3_besk_lang, gruppe4_kode, gruppe4_besk_lang from dt_p.dim_ia_naring",
                namedParameters,
                (resultSet, rowNum) ->
                        new Næringsgruppering(
                                resultSet.getString(NAERING_KODE),
                                resultSet.getString(NAERING_BESKRIVELSE),
                                resultSet.getString(GRUPPE1_KODE),
                                resultSet.getString(GRUPPE1_BESKRIVELSE),
                                resultSet.getString(GRUPPE2_KODE),
                                resultSet.getString(GRUPPE2_BESKRIVELSE),
                                resultSet.getString(GRUPPE3_KODE),
                                resultSet.getString(GRUPPE3_BESKRIVELSE),
                                resultSet.getString(GRUPPE4_KODE),
                                resultSet.getString(GRUPPE4_BESKRIVELSE)
                        ));
    }
}
