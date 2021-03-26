package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

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
public class AlleNaringRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public AlleNaringRepository(@Qualifier("sykefravarsstatistikkJdbcTemplate")
                                        NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }


    public List<SykefraværsstatistikkNæring> hentSykefraværprosentAlleNæringerForEttKvartal(
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
