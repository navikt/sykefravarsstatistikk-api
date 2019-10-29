package no.nav.tag.sykefravarsstatistikk.api.provisjonering;

import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næringsgruppe;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


@Profile({"local", "dev"})
@Component
public class DataverehusRepository {


    public static final String NARGRPKODE = "NARGRPKODE";
    public static final String NARGRPNAVN = "NARGRPNAVN";
    public static final String NARINGKODE = "NARINGKODE";
    public static final String NARINGNAVN = "NARINGNAVN";
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

    public List<Næringsgruppe> hentAlleNæringsgrupper() {
        SqlParameterSource namedParameters = new MapSqlParameterSource();

        return namedParameterJdbcTemplate.query(
                "select NARGRPKODE, NARGRPNAVN from dt_p.V_DIM_IA_FGRP_NARING_SN2007",
                namedParameters,
                (resultSet, rowNum) -> mapTilNæringsgrupper(resultSet)
        );
    }

    public List<Næring> hentAlleNæringer() {
        SqlParameterSource namedParameters = new MapSqlParameterSource();

        return namedParameterJdbcTemplate.query(
                "select NARINGKODE, NARGRPKODE, NARINGNAVN from dt_p.V_DIM_IA_NARING_SN2007",
                namedParameters,
                (resultSet, rowNum) -> mapTilNæring(resultSet)
        );
    }


    private Næringsgruppe mapTilNæringsgrupper(ResultSet rs) throws SQLException {
        return new Næringsgruppe(
                rs.getString(NARGRPKODE),
                rs.getString(NARGRPNAVN)
        );
    }

    private Næring mapTilNæring(ResultSet rs) throws SQLException {
        return new Næring(
                rs.getString(NARGRPKODE),
                rs.getString(NARINGKODE),
                rs.getString(NARINGNAVN)
        );
    }

    private Sektor mapTilSektor(ResultSet rs) throws SQLException {
        return new Sektor (
                rs.getString(SEKTORKODE),
                rs.getString(SEKTORNAVN)
        );
    }

}
