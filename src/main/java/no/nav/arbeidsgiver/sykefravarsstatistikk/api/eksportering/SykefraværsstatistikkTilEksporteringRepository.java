package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring;
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

    /*
      Denne repository gjør tilgjengelig (til en ÅrstallOgKvartal) statistikk for:
       - SykefraværsstatistikkLand
       - SykefraværsstatistikkSektor
       - SykefraværsstatistikkNæring (2 siffer)
       - SykefraværsstatistikkNæring (5 siffer)
       - SykefraværsstatistikkVirksomhet
     */

    public List<SykefraværsstatistikkNæring> hentSykefraværprosentAlleNæringer5Siffer(
            ÅrstallOgKvartal årstallOgKvartal) {
        try {
            return namedParameterJdbcTemplate.query(
                    "select naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk " +
                            "from sykefravar_statistikk_naring5siffer " +
                            "where arstall = :arstall and kvartal = :kvartal order by arstall, kvartal, naring_kode",
                    new MapSqlParameterSource()
                            .addValue("arstall", årstallOgKvartal.getÅrstall())
                            .addValue("kvartal", årstallOgKvartal.getKvartal()),
                    (rs, rowNum) -> mapTilSykefraværsstatistikkNæring(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    public List<SykefraværsstatistikkNæring> hentSykefraværprosentAlleNæringer(
            ÅrstallOgKvartal årstallOgKvartal) {
        try {
            return namedParameterJdbcTemplate.query(
                    "select naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk " +
                            "from sykefravar_statistikk_naring " +
                            "where arstall = :arstall and kvartal = :kvartal order by arstall, kvartal, naring_kode",
                    new MapSqlParameterSource()
                            .addValue("arstall", årstallOgKvartal.getÅrstall())
                            .addValue("kvartal", årstallOgKvartal.getKvartal()),
                    (rs, rowNum) -> mapTilSykefraværsstatistikkNæring(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }
    public List<SykefraværsstatistikkVirksomhetUtenVarighet> hentSykefraværprosentAlleVirksomheter(
            ÅrstallOgKvartal årstallOgKvartal) {
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
                            .addValue("arstall", årstallOgKvartal.getÅrstall())
                            .addValue("kvartal", årstallOgKvartal.getKvartal()),
                    (rs, rowNum) -> mapTilSykefraværsstatistikkVirksomhet(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }


    // Utilities
    private SykefraværsstatistikkVirksomhetUtenVarighet mapTilSykefraværsstatistikkVirksomhet(ResultSet rs) throws SQLException {
        return new SykefraværsstatistikkVirksomhetUtenVarighet(
                rs.getInt("arstall"),
                rs.getInt("kvartal"),
                rs.getString("orgnr"),
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
}
