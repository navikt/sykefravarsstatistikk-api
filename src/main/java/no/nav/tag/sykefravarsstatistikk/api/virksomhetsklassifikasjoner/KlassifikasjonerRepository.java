package no.nav.tag.sykefravarsstatistikk.api.virksomhetsklassifikasjoner;

import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næringsgruppering;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

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
        validerLengdePåNæringskode(næringskode5siffer, 5);

        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue(KODE_5SIFFER, næringskode5siffer);

        return namedParameterJdbcTemplate.queryForObject(
                "SELECT * FROM naringsgruppering WHERE kode_5siffer = :kode_5siffer",
                namedParameters,
                (rs, rowNum) -> mapTilNæringsgruppering(rs)
        );
    }

    public List<Næringsgruppering> hentNæringsgrupperingerTilhørendeNæringskode2siffer(String næringskode2siffer) {
        validerLengdePåNæringskode(næringskode2siffer, 2);

        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue(KODE_2SIFFER, næringskode2siffer);

        List<Næringsgruppering> næringsgrupperinger = namedParameterJdbcTemplate.query(
                "SELECT * FROM naringsgruppering WHERE kode_2siffer = :kode_2siffer",
                namedParameters,
                (rs, rowNum) -> mapTilNæringsgruppering(rs)
        );

        return næringsgrupperinger;
    }

    private void validerLengdePåNæringskode(String næringskode, int ønsketLengde) {
        if (næringskode.length() != ønsketLengde) {
            throw new IllegalArgumentException("Ugyldig næringskode: " + næringskode + ". Må ha lengde " + ønsketLengde);
        }
    }

    private Næringsgruppering mapTilNæringsgruppering(ResultSet rs) throws SQLException {
        return new Næringsgruppering(
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
        );
    }

    protected Sektor mapTilSektor(ResultSet rs) throws SQLException {
        return new Sektor(
                rs.getString("kode"),
                rs.getString("navn")
        );
    }
}
