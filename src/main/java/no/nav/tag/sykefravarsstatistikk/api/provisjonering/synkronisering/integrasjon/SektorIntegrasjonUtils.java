package no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering.integrasjon;

import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class SektorIntegrasjonUtils {

    public static final String KODE = "kode";
    public static final String NAVN = "navn";


    public static FetchVirksomhetsklassifikasjonFunction getHentSektorFunction(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        FetchVirksomhetsklassifikasjonFunction<Sektor> function =
                (Sektor sektor) -> {
                    SqlParameterSource namedParameters =
                            new MapSqlParameterSource().addValue(KODE, sektor.getKode());

                    try {
                        Sektor hentetSektor =
                                namedParameterJdbcTemplate.queryForObject(
                                        "select kode, navn from sektor where kode = :kode",
                                        namedParameters,
                                        (resultSet, rowNum) -> mapTilSektor(resultSet));
                        return Optional.of(hentetSektor);
                    } catch (EmptyResultDataAccessException erdae) {
                        return Optional.empty();
                    }
                };
        return function;
    }

    public static CreateVirksomhetsklassifikasjonFunction getCreateSektorFunction(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        CreateVirksomhetsklassifikasjonFunction<Sektor> function =
                (Sektor sektor) -> {
                    SqlParameterSource namedParameters =
                            new MapSqlParameterSource()
                                    .addValue(KODE, sektor.getKode())
                                    .addValue(NAVN, sektor.getNavn());

                    return namedParameterJdbcTemplate.update(
                            "insert into SEKTOR (kode, navn)  values (:kode, :navn)", namedParameters);
                };

        return function;
    }

    public static UpdateVirksomhetsklassifikasjonFunction getUpdateSektorFunction(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        UpdateVirksomhetsklassifikasjonFunction<Sektor> updateVirksomhetsklassifikasjonFunction =
                (Sektor eksisterendeSektor, Sektor sektor) -> {
                    SqlParameterSource namedParameters =
                            new MapSqlParameterSource()
                                    .addValue(KODE, eksisterendeSektor.getKode())
                                    .addValue(NAVN, sektor.getNavn());

                    return namedParameterJdbcTemplate.update(
                            "update SEKTOR set navn = :navn where kode = :kode", namedParameters);
                };

        return updateVirksomhetsklassifikasjonFunction;
    }


    private static Sektor mapTilSektor(ResultSet rs) throws SQLException {
        return new Sektor(rs.getString(KODE), rs.getString(NAVN));
    }

}

