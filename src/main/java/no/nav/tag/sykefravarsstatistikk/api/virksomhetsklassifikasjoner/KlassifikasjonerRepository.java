package no.nav.tag.sykefravarsstatistikk.api.virksomhetsklassifikasjoner;

import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class KlassifikasjonerRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public KlassifikasjonerRepository(
            @Qualifier("sykefravarsstatistikkJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate
    )
    {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }


    public Sektor hentSektor(String kode) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("kode", kode);

        return namedParameterJdbcTemplate.queryForObject(
                "SELECT * FROM SEKTOR WHERE kode = :kode",
                namedParameters,
                (rs, rowNum) -> mapTilSektor(rs)
        );
    }

    protected Sektor mapTilSektor(ResultSet rs) throws SQLException {
        return new Sektor(
                rs.getString("kode"),
                rs.getString("navn")
        );
    }
}
