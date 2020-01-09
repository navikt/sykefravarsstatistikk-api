package no.nav.tag.sykefravarsstatistikk.api.virksomhetsklassifikasjoner;

import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næringsgruppering;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering.NæringsgrupperingSynkroniseringRepository.*;

@Component
public class KlassifikasjonerRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public KlassifikasjonerRepository(
            @Qualifier("sykefravarsstatistikkJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
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

    public Næring hentNæring(String kode) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("kode", kode);

        return namedParameterJdbcTemplate.queryForObject(
                "SELECT * FROM naring WHERE kode = :kode",
                namedParameters,
                (rs, rowNum) -> new Næring(
                        rs.getString("kode"),
                        rs.getString("navn")
                )

        );
    }

    public Næringsgruppering hentNæringsgruppering(String næringskode5siffer) {
        if (næringskode5siffer.length() != 5) {
            throw new IllegalArgumentException("Ugyldig næringskode: " + næringskode5siffer + ". Må ha lengde 5.");
        }

        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue(KODE_5SIFFER, næringskode5siffer);

        return namedParameterJdbcTemplate.queryForObject(
                "SELECT * FROM naringsgruppering WHERE kode_5siffer = :kode_5siffer",
                namedParameters,
                (rs, rowNum) -> new Næringsgruppering(
                        rs.getString(KODE_5SIFFER),
                        rs.getString(BESKRIVELSE_5SIFFER),
                        rs.getString(KODE_4SIFFER),
                        rs.getString(BESKRIVELSE_4SIFFER),
                        rs.getString(KODE_3SIFFER),
                        rs.getString(BESKRIVELSE_3SIFFER),
                        rs.getString(KODE_2SIFFER),
                        rs.getString(BESKRIVELSE_2SIFFER),
                        rs.getString(KODE_HOVEDOMRADE),
                        rs.getString(BESKRIVELSE_HOVEDOMRADE)

                )
        );
    }


    protected Sektor mapTilSektor(ResultSet rs) throws SQLException {
        return new Sektor(
                rs.getString("kode"),
                rs.getString("navn")
        );
    }
}
