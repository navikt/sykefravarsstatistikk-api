package no.nav.tag.sykefravarsstatistikk.api.provisjonering;

import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


@Component
public class DataverehusRepository {


    public static final String SEKTORKODE = "SEKTORKODE";
    public static final String SEKTORNAVN = "SEKTORNAVN";

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    public DataverehusRepository(
            @Qualifier("datavarehusJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }


    public List<Sektor> hentAlleSektorer() {
        SqlParameterSource namedParameters = new MapSqlParameterSource();

        return namedParameterJdbcTemplate.query(
                "select SEKTORKODE, SEKTORNAVN from dt_p.V_DIM_IA_SEKTOR",
                namedParameters,
                (resultSet, rowNum) -> mapTilSektor(resultSet)
        );
    }


    private Sektor mapTilSektor(ResultSet rs) throws SQLException {
        return new Sektor (
                rs.getString(SEKTORKODE),
                rs.getString(SEKTORNAVN)
        );
    }

}
