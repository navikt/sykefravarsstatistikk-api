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

    public Optional<Virksomhetsklassifikasjon> hent(
            Virksomhetsklassifikasjon virksomhetsklassifikasjon,
            Klassifikasjonskilde klassifikasjonskilde
    ) {
        SqlParameterSource namedParameters =
                new MapSqlParameterSource().addValue(KODE, virksomhetsklassifikasjon.getKode());

        try {
            Virksomhetsklassifikasjon hentetVirksomhetsklassifikasjon =
                    namedParameterJdbcTemplate.queryForObject(
                            "select kode, navn from " + klassifikasjonskilde.tabell + " where kode = :kode",
                            namedParameters,
                            (resultSet, rowNum) -> mapTilVirksomhetsklassifikasjon(resultSet, klassifikasjonskilde));
            return Optional.of(hentetVirksomhetsklassifikasjon);
        } catch (EmptyResultDataAccessException erdae) {
            return Optional.empty();
        }
    }

    public int opprett(
            Virksomhetsklassifikasjon virksomhetsklassifikasjon,
            Klassifikasjonskilde klassifikasjonskilde
    ) {
        SqlParameterSource namedParameters =
                new MapSqlParameterSource()
                        .addValue(KODE, virksomhetsklassifikasjon.getKode())
                        .addValue(NAVN, virksomhetsklassifikasjon.getNavn());

        return namedParameterJdbcTemplate.update(
                "insert into " + klassifikasjonskilde.tabell + " (kode, navn)  values (:kode, :navn)",
                namedParameters);
    }

    public int oppdater(
            Virksomhetsklassifikasjon virksomhetsklassifikasjon,
            Klassifikasjonskilde klassifikasjonskilde
    ) {
        SqlParameterSource namedParameters =
                new MapSqlParameterSource()
                        .addValue(KODE, virksomhetsklassifikasjon.getKode())
                        .addValue(NAVN, virksomhetsklassifikasjon.getNavn());

        return namedParameterJdbcTemplate.update(
                "update "+klassifikasjonskilde.tabell+" set navn = :navn where kode = :kode",
                namedParameters
        );
    }

    private static Virksomhetsklassifikasjon mapTilVirksomhetsklassifikasjon
            (ResultSet rs, Klassifikasjonskilde klassifikasjonskilde) throws SQLException {
        switch (klassifikasjonskilde) {
            case SEKTOR:
                return new Sektor(rs.getString(KODE), rs.getString(NAVN));
            case NÆRING:
                return new Næring(rs.getString(KODE), rs.getString(NAVN));
            default:
                throw new IllegalArgumentException();
        }
    }
}
