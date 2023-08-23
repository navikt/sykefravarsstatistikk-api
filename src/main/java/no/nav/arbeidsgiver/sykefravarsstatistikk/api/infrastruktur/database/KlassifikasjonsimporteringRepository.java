package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Klassifikasjonskilde;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Sektor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Component
public class KlassifikasjonsimporteringRepository {

    private static final String KODE = "kode";
    private static final String NAVN = "navn";

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public KlassifikasjonsimporteringRepository(
            @Qualifier("sykefravarsstatistikkJdbcTemplate")
            NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public Optional<Næring> hentNæring(
            Næring virksomhetsklassifikasjon) {
        SqlParameterSource namedParameters =
                new MapSqlParameterSource().addValue(KODE, virksomhetsklassifikasjon.getKode());

        try {
            Næring hentetVirksomhetsklassifikasjon =
                    namedParameterJdbcTemplate.queryForObject(
                            "select kode, navn from " + Klassifikasjonskilde.NÆRING.tabell + " where kode = :kode",
                            namedParameters,
                            (resultSet, rowNum) ->
                                    mapTilNæring(resultSet));
            return Optional.of(hentetVirksomhetsklassifikasjon);
        } catch (EmptyResultDataAccessException erdae) {
            return Optional.empty();
        }
    }

    public Optional<Sektor> hentSektor(
            Sektor virksomhetsklassifikasjon) {
        SqlParameterSource namedParameters =
                new MapSqlParameterSource().addValue(KODE, virksomhetsklassifikasjon.getKode());

        try {
            Sektor hentetVirksomhetsklassifikasjon =
                    namedParameterJdbcTemplate.queryForObject(
                            "select kode, navn from " + Klassifikasjonskilde.SEKTOR.tabell + " where kode = :kode",
                            namedParameters,
                            (resultSet, rowNum) ->
                                    mapTilSektor(resultSet));
            return Optional.of(hentetVirksomhetsklassifikasjon);
        } catch (EmptyResultDataAccessException erdae) {
            return Optional.empty();
        }
    }

    public int opprettNæring(
            Næring virksomhetsklassifikasjon) {
        SqlParameterSource namedParameters =
                new MapSqlParameterSource()
                        .addValue(KODE, virksomhetsklassifikasjon.getKode())
                        .addValue(NAVN, virksomhetsklassifikasjon.getNavn());

        return namedParameterJdbcTemplate.update(
                "insert into " + Klassifikasjonskilde.NÆRING.tabell + " (kode, navn)  values (:kode, :navn)",
                namedParameters);
    }

    public int opprettSektor(
            Sektor virksomhetsklassifikasjon) {
        SqlParameterSource namedParameters =
                new MapSqlParameterSource()
                        .addValue(KODE, virksomhetsklassifikasjon.getKode())
                        .addValue(NAVN, virksomhetsklassifikasjon.getNavn());

        return namedParameterJdbcTemplate.update(
                "insert into " + Klassifikasjonskilde.SEKTOR.tabell + " (kode, navn)  values (:kode, :navn)",
                namedParameters);
    }

    public int oppdaterNæring(
            Næring næring) {
        SqlParameterSource namedParameters =
                new MapSqlParameterSource()
                        .addValue(KODE, næring.getKode())
                        .addValue(NAVN, næring.getNavn());

        return namedParameterJdbcTemplate.update(
                "update " + Klassifikasjonskilde.NÆRING.tabell + " set navn = :navn where kode = :kode",
                namedParameters);
    }

    public int oppdaterSektor(
            Sektor virksomhetsklassifikasjon) {
        SqlParameterSource namedParameters =
                new MapSqlParameterSource()
                        .addValue(KODE, virksomhetsklassifikasjon.getKode())
                        .addValue(NAVN, virksomhetsklassifikasjon.getNavn());

        return namedParameterJdbcTemplate.update(
                "update " + Klassifikasjonskilde.SEKTOR.tabell + " set navn = :navn where kode = :kode",
                namedParameters);
    }

    private static Næring mapTilNæring(
            ResultSet rs) throws SQLException {
        return new Næring(rs.getString(KODE), rs.getString(NAVN));
    }

    private static Sektor mapTilSektor(
            ResultSet rs) throws SQLException {
        return new Sektor(rs.getString(KODE), rs.getString(NAVN));
    }
}
