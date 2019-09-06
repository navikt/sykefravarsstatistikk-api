package no.nav.tag.sykefravarsstatistikk.api.repository;

import lombok.SneakyThrows;
import no.nav.tag.sykefravarsstatistikk.api.domain.stats.LandStatistikk;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

@Component
public class SykefravarprosentRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    public SykefravarprosentRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }


    @SneakyThrows
    public LandStatistikk hentLandStatistikk(int arstall, int kvartal) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("arstall", arstall)
                .addValue("kvartal", kvartal);

        LandStatistikk landStatistikk = namedParameterJdbcTemplate.queryForObject(
                "SELECT * FROM SYKEFRAVAR_STATISTIK_LAND where arstall = :arstall and kvartal = :kvartal",
                namedParameters,
                new LandStatistikkRowMapper());
        return landStatistikk;
    }
}
