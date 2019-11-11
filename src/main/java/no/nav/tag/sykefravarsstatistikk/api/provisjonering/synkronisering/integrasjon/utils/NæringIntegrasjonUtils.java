package no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering.integrasjon.utils;

import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
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

public class NæringIntegrasjonUtils extends VirksomhetsklassifikasjonIntegrasjon
    implements VirksomhetsklassifikasjonIntegrasjonUtils {


    public NæringIntegrasjonUtils(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        super(namedParameterJdbcTemplate);
    }

    @Override
    public FetchVirksomhetsklassifikasjonFunction getFetchFunction() {
        FetchVirksomhetsklassifikasjonFunction<Næring> function =
                (Næring næring) -> {
                    SqlParameterSource namedParameters =
                            new MapSqlParameterSource().addValue(KODE, næring.getKode());

                    try {
                        Næring hentetNæring =
                                namedParameterJdbcTemplate.queryForObject(
                                        "select naringsgruppe_kode, kode, navn from naring where kode = :kode",
                                        namedParameters,
                                        (resultSet, rowNum) -> mapTilNæring(resultSet));
                        return Optional.of(hentetNæring);
                    } catch (EmptyResultDataAccessException erdae) {
                        return Optional.empty();
                    }
                };
        return function;
    }

    @Override
    public CreateVirksomhetsklassifikasjonFunction getCreateFunction() {
        CreateVirksomhetsklassifikasjonFunction<Næring> function =
                (Næring næring) -> {
                    SqlParameterSource namedParameters =
                            new MapSqlParameterSource()
                                    .addValue(KODE, næring.getKode())
                                    .addValue(NARINGSGRUPPE_KODE, næring.getNæringsgruppeKode())
                                    .addValue(NAVN, næring.getNavn());

                    return namedParameterJdbcTemplate.update(
                            "insert into naring (kode, naringsgruppe_kode, navn)  " +
                                    "values (:kode, :naringsgruppe_kode, :navn)",
                            namedParameters
                    );
                };

        return function;
    }

    @Override
    public UpdateVirksomhetsklassifikasjonFunction getUpdateFunction() {
        UpdateVirksomhetsklassifikasjonFunction<Næring> updateVirksomhetsklassifikasjonFunction =
                (Næring eksisterendeNæring, Næring næring) -> {
                    SqlParameterSource namedParameters =
                            new MapSqlParameterSource()
                                    .addValue(KODE, eksisterendeNæring.getKode())
                                    .addValue(NARINGSGRUPPE_KODE, eksisterendeNæring.getNæringsgruppeKode())
                                    .addValue(NAVN, næring.getNavn());

                    return namedParameterJdbcTemplate.update(
                            "update naring set navn = :navn, naringsgruppe_kode = :naringsgruppe_kode " +
                                    "where kode = :kode",
                            namedParameters
                    );
                };

        return updateVirksomhetsklassifikasjonFunction;
    }


    private static Næring mapTilNæring(ResultSet rs) throws SQLException {
        return new Næring(rs.getString(NARINGSGRUPPE_KODE), rs.getString(KODE), rs.getString(NAVN));
    }
}

