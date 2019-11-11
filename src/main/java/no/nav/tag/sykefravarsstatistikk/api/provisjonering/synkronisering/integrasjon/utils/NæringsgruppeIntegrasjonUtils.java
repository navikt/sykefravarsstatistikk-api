package no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering.integrasjon.utils;

import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næringsgruppe;
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

public class NæringsgruppeIntegrasjonUtils extends VirksomhetsklassifikasjonIntegrasjon
    implements VirksomhetsklassifikasjonIntegrasjonUtils {


    public NæringsgruppeIntegrasjonUtils(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        super(namedParameterJdbcTemplate);
    }

    @Override
    public FetchVirksomhetsklassifikasjonFunction getFetchFunction() {
        FetchVirksomhetsklassifikasjonFunction<Næringsgruppe> function =
                (Næringsgruppe næringsgruppe) -> {
                    SqlParameterSource namedParameters =
                            new MapSqlParameterSource().addValue(KODE, næringsgruppe.getKode());

                    try {
                        Næringsgruppe hentetNæringsgruppe =
                                namedParameterJdbcTemplate.queryForObject(
                                        "select kode, navn from naringsgruppe where kode = :kode",
                                        namedParameters,
                                        (resultSet, rowNum) -> mapTilNæringsgruppe(resultSet));
                        return Optional.of(hentetNæringsgruppe);
                    } catch (EmptyResultDataAccessException erdae) {
                        return Optional.empty();
                    }
                };
        return function;
    }

    @Override
    public CreateVirksomhetsklassifikasjonFunction getCreateFunction() {
        CreateVirksomhetsklassifikasjonFunction<Næringsgruppe> function =
                (Næringsgruppe næringsgruppe) -> {
                    SqlParameterSource namedParameters =
                            new MapSqlParameterSource()
                                    .addValue(KODE, næringsgruppe.getKode())
                                    .addValue(NAVN, næringsgruppe.getNavn());

                    return namedParameterJdbcTemplate.update(
                            "insert into naringsgruppe (kode, navn)  " +
                                    "values (:kode, :navn)",
                            namedParameters
                    );
                };

        return function;
    }

    @Override
    public UpdateVirksomhetsklassifikasjonFunction getUpdateFunction() {
        UpdateVirksomhetsklassifikasjonFunction<Næringsgruppe> updateVirksomhetsklassifikasjonFunction =
                (Næringsgruppe eksisterendeNæring, Næringsgruppe næringsgruppe) -> {
                    SqlParameterSource namedParameters =
                            new MapSqlParameterSource()
                                    .addValue(KODE, eksisterendeNæring.getKode())
                                    .addValue(NAVN, næringsgruppe.getNavn());

                    return namedParameterJdbcTemplate.update(
                            "update naringsgruppe set navn = :navn where kode = :kode",
                            namedParameters
                    );
                };

        return updateVirksomhetsklassifikasjonFunction;
    }


    private static Næringsgruppe mapTilNæringsgruppe(ResultSet rs) throws SQLException {
        return new Næringsgruppe(rs.getString(KODE), rs.getString(NAVN));
    }

}

