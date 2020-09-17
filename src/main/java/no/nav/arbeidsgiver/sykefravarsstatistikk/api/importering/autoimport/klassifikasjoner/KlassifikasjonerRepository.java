package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.klassifikasjoner;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Sektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Virksomhetsklassifikasjon;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.OpprettEllerOppdaterResultat;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.klassifikasjoner.VirksomhetsklassifikasjonIntegrasjon.KODE;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.klassifikasjoner.VirksomhetsklassifikasjonIntegrasjon.NAVN;

@Component
public class KlassifikasjonerRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public KlassifikasjonerRepository(
            @Qualifier("sykefravarsstatistikkJdbcTemplate")
                    NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public Optional<Sektor> hentSektor(Sektor sektor) {
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
    }

    public int opprett(Sektor sektor) {
        SqlParameterSource namedParameters =
                new MapSqlParameterSource()
                        .addValue(KODE, sektor.getKode())
                        .addValue(NAVN, sektor.getNavn());

        return namedParameterJdbcTemplate.update(
                "insert into SEKTOR (kode, navn)  values (:kode, :navn)",
                namedParameters);
    }

    public int opprett(Næring næring) {
        SqlParameterSource namedParameters =
                new MapSqlParameterSource()
                        .addValue(KODE, næring.getKode())
                        .addValue(NAVN, næring.getNavn());

        return namedParameterJdbcTemplate.update(
                "insert into naring (kode, navn)   values (:kode, :navn)",
                namedParameters
        );
    }


    public int oppdater(Sektor sektor) {
        SqlParameterSource namedParameters =
                new MapSqlParameterSource()
                        .addValue(KODE, sektor.getKode())
                        .addValue(NAVN, sektor.getNavn());

        return namedParameterJdbcTemplate.update(
                "update SEKTOR set navn = :navn where kode = :kode",
                namedParameters
        );
    }

    public int oppdater(Næring næring) {

        SqlParameterSource namedParameters =
                new MapSqlParameterSource()
                        .addValue(KODE, næring.getKode())
                        .addValue(NAVN, næring.getNavn());

        return namedParameterJdbcTemplate.update(
                "update naring set navn = :navn where kode = :kode",
                namedParameters
        );


    }


    private static Sektor mapTilSektor(ResultSet rs) throws SQLException {
        return new Sektor(rs.getString(KODE), rs.getString(NAVN));
    }


    public Optional<Næring> hentNæring(Virksomhetsklassifikasjon næring) {
        SqlParameterSource namedParameters =
                new MapSqlParameterSource().addValue(KODE, næring.getKode());

        try {
            Næring hentetNæring =
                    namedParameterJdbcTemplate.queryForObject(
                            "select kode, navn from naring where kode = :kode",
                            namedParameters,
                            (resultSet, rowNum) -> mapTilNæring(resultSet));
            return Optional.of(hentetNæring);
        } catch (EmptyResultDataAccessException erdae) {
            return Optional.empty();
        }
    }

    private static Næring mapTilNæring(ResultSet rs) throws SQLException {
        return new Næring(rs.getString(KODE), rs.getString(NAVN));
    }
}
