package no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering.integrasjon.utils;

import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering.integrasjon.CreateVirksomhetsklassifikasjonFunction;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering.integrasjon.FetchVirksomhetsklassifikasjonFunction;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering.integrasjon.UpdateVirksomhetsklassifikasjonFunction;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class SektorIntegrasjonUtils extends VirksomhetsklassifikasjonIntegrasjon
    implements VirksomhetsklassifikasjonIntegrasjonUtils {

    public static final String KODE = "kode";
    public static final String NAVN = "navn";

    public SektorIntegrasjonUtils(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        super(namedParameterJdbcTemplate);
    }


    @Override
    public FetchVirksomhetsklassifikasjonFunction getFetchFunction() {
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

    @Override
    public CreateVirksomhetsklassifikasjonFunction getCreateFunction() {
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

    public UpdateVirksomhetsklassifikasjonFunction getUpdateFunction() {
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

