package no.nav.tag.sykefravarsstatistikk.api.repository;

import no.nav.tag.sykefravarsstatistikk.api.domain.stats.LandStatistikk;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LandStatistikkRowMapper implements RowMapper<LandStatistikk> {

    @Override
    public LandStatistikk mapRow(ResultSet rs, int rowNum) throws SQLException {

        return LandStatistikk.builder()
                .arstall(rs.getInt("arstall"))
                .kvartal(rs.getInt("kvartal"))
                .tapteDagsverk(rs.getBigDecimal("tapte_dagsverk"))
                .muligeDagsverk(rs.getBigDecimal("mulige_dagsverk"))
                .build();
    }
}
