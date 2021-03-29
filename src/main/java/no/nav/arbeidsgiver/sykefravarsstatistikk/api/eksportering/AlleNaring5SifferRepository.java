package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring;
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
public class AlleNaring5SifferRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public AlleNaring5SifferRepository(@Qualifier("sykefravarsstatistikkJdbcTemplate")
                                               NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }


    public List<SykefraværsstatistikkNæring> hentSykefraværprosentAlleNæringer5SifferForEttKvartal(
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
    }/* public List<Map<ÅrstallOgKvartal, SykefraværsstatistikkNæring>> hentSykefraværprosentAlleNæringerForEttKvartal(
            ÅrstallOgKvartal årstallOgKvartal) {
        try {
            return namedParameterJdbcTemplate.query(
                    "select naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk " +
                            "from sykefravar_statistikk_naring5siffer order by arstall, kvartal, naring_kode",
                    new MapSqlParameterSource(),
                    (rs, rowNum) -> mapTilSykefraværsstatistikkNæring(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }*/


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
    /*private Map<ÅrstallOgKvartal, SykefraværsstatistikkNæring> mapTilSykefraværsstatistikkNæring(ResultSet rs) throws SQLException {
        Map<ÅrstallOgKvartal, SykefraværsstatistikkNæring> årstallOgKvartalSykefraværsstatistikkNæringHashMap = new HashMap<>();
        årstallOgKvartalSykefraværsstatistikkNæringHashMap.put(
                new ÅrstallOgKvartal(
                        rs.getInt("arstall"),
                        rs.getInt("kvartal")),
                new SykefraværsstatistikkNæring(
                        rs.getInt("arstall"),
                        rs.getInt("kvartal"),
                        rs.getString("naring_kode"),
                        rs.getInt("antall_personer"),
                        rs.getBigDecimal("tapte_dagsverk"),
                        rs.getBigDecimal("mulige_dagsverk")
                ));

        return årstallOgKvartalSykefraværsstatistikkNæringHashMap;
    }*/
}
