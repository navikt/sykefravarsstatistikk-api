package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Kvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkLand;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkSektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhetUtenVarighet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Component
public class SykefraværsstatistikkTilEksporteringRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public SykefraværsstatistikkTilEksporteringRepository(@Qualifier("sykefravarsstatistikkJdbcTemplate")
                                               NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }


    public SykefraværsstatistikkLand hentSykefraværprosentLand(
            Kvartal kvartal) {
        try {
            List<SykefraværsstatistikkLand> resultat = namedParameterJdbcTemplate.query(
                    "select arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk " +
                            "from sykefravar_statistikk_land " +
                            "where arstall = :arstall and kvartal = :kvartal order by arstall, kvartal",
                    new MapSqlParameterSource()
                            .addValue("arstall", kvartal.getÅrstall())
                            .addValue("kvartal", kvartal.getKvartal()),
                    (rs, rowNum) -> mapTilSykefraværsstatistikkLand(rs)
            );

            if (resultat.size() != 1) {
                return null;
            }
            return resultat.get(0);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<SykefraværsstatistikkSektor> hentSykefraværprosentAlleSektorer(
            Kvartal kvartal) {
        try {
            return namedParameterJdbcTemplate.query(
                    "select sektor_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk " +
                            "from sykefravar_statistikk_sektor " +
                            "where arstall = :arstall and kvartal = :kvartal " +
                            "order by arstall, kvartal, sektor_kode",
                    new MapSqlParameterSource()
                            .addValue("arstall", kvartal.getÅrstall())
                            .addValue("kvartal", kvartal.getKvartal()),
                    (rs, rowNum) -> mapTilSykefraværsstatistikkSektor(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    public List<SykefraværsstatistikkNæring> hentSykefraværprosentAlleNæringer(
            Kvartal kvartal) {
        try {
            return namedParameterJdbcTemplate.query(
                    "select naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk " +
                            "from sykefravar_statistikk_naring " +
                            "where arstall = :arstall and kvartal = :kvartal order by arstall, kvartal, naring_kode",
                    new MapSqlParameterSource()
                            .addValue("arstall", kvartal.getÅrstall())
                            .addValue("kvartal", kvartal.getKvartal()),
                    (rs, rowNum) -> mapTilSykefraværsstatistikkNæring(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    public List<SykefraværsstatistikkNæring5Siffer> hentSykefraværprosentAlleNæringer5Siffer(
            Kvartal kvartal) {
        try {
            return namedParameterJdbcTemplate.query(
                    "select naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk " +
                            "from sykefravar_statistikk_naring5siffer " +
                            "where arstall = :arstall and kvartal = :kvartal order by arstall, kvartal, naring_kode",
                    new MapSqlParameterSource()
                            .addValue("arstall", kvartal.getÅrstall())
                            .addValue("kvartal", kvartal.getKvartal()),
                    (rs, rowNum) -> mapTilSykefraværsstatistikkNæring5Siffer(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    public List<SykefraværsstatistikkVirksomhetUtenVarighet> hentSykefraværprosentAlleVirksomheter(
            Kvartal kvartal) {
        try {
            return namedParameterJdbcTemplate.query(
                    "select arstall, kvartal, orgnr, " +
                            "sum(tapte_dagsverk) as tapte_dagsverk, " +
                            "sum(mulige_dagsverk) as mulige_dagsverk, " +
                            "sum(antall_personer) as antall_personer " +
                            "from sykefravar_statistikk_virksomhet " +
                            "where arstall = :arstall and kvartal = :kvartal " +
                            "group by arstall, kvartal, orgnr",
                    new MapSqlParameterSource()
                            .addValue("arstall", kvartal.getÅrstall())
                            .addValue("kvartal", kvartal.getKvartal()),
                    (rs, rowNum) -> mapTilSykefraværsstatistikkVirksomhet(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }


    // Utilities
    private SykefraværsstatistikkLand mapTilSykefraværsstatistikkLand(ResultSet rs) throws SQLException {
        return new SykefraværsstatistikkLand(
                rs.getInt("arstall"),
                rs.getInt("kvartal"),
                rs.getInt("antall_personer"),
                rs.getBigDecimal("tapte_dagsverk"),
                rs.getBigDecimal("mulige_dagsverk")
        );
    }

    private SykefraværsstatistikkSektor mapTilSykefraværsstatistikkSektor(ResultSet rs) throws SQLException {
        return new SykefraværsstatistikkSektor(
                rs.getInt("arstall"),
                rs.getInt("kvartal"),
                rs.getString("sektor_kode"),
                rs.getInt("antall_personer"),
                rs.getBigDecimal("tapte_dagsverk"),
                rs.getBigDecimal("mulige_dagsverk")
        );
    }

    private SykefraværsstatistikkNæring mapTilSykefraværsstatistikkNæring(ResultSet rs) throws SQLException {
        return new SykefraværsstatistikkNæring(
                rs.getInt("arstall"),
                rs.getInt("kvartal"),
                rs.getString("naring_kode"),
                rs.getInt("antall_personer"),
                rs.getBigDecimal("tapte_dagsverk"),
                rs.getBigDecimal("mulige_dagsverk")
        );
    }

    private SykefraværsstatistikkNæring5Siffer mapTilSykefraværsstatistikkNæring5Siffer(
            ResultSet rs
    ) throws SQLException {
        return new SykefraværsstatistikkNæring5Siffer(
                rs.getInt("arstall"),
                rs.getInt("kvartal"),
                rs.getString("naring_kode"),
                rs.getInt("antall_personer"),
                rs.getBigDecimal("tapte_dagsverk"),
                rs.getBigDecimal("mulige_dagsverk")
        );
    }

    private SykefraværsstatistikkVirksomhetUtenVarighet mapTilSykefraværsstatistikkVirksomhet(
            ResultSet rs
    ) throws SQLException {
        return new SykefraværsstatistikkVirksomhetUtenVarighet(
                rs.getInt("arstall"),
                rs.getInt("kvartal"),
                rs.getString("orgnr"),
                rs.getInt("antall_personer"),
                rs.getBigDecimal("tapte_dagsverk"),
                rs.getBigDecimal("mulige_dagsverk")
        );
    }
}
