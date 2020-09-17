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
public class VirksomhetsklassifikasjonerSynkroniseringRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public VirksomhetsklassifikasjonerSynkroniseringRepository(
            @Qualifier("sykefravarsstatistikkJdbcTemplate")
                    NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public OpprettEllerOppdaterResultat opprettEllerOppdaterSektorer(List<Sektor> sektorerIDatavarehus) {
        return opprettEllerOppdaterSektorer(sektorerIDatavarehus, new SektorIntegrasjonUtils(namedParameterJdbcTemplate));
    }

    public OpprettEllerOppdaterResultat opprettEllerOppdaterNæringer(List<Næring> næringer) {
        return opprettEllerOppdater(næringer, new NæringIntegrasjonUtils(namedParameterJdbcTemplate));
    }

    private OpprettEllerOppdaterResultat opprettEllerOppdaterSektorer(
            List<Sektor> sektorer,
            VirksomhetsklassifikasjonIntegrasjonUtils integrasjonUtils) {

        final OpprettEllerOppdaterResultat sluttResultat = new OpprettEllerOppdaterResultat();

        sektorer.forEach(
                sektor -> {
                    OpprettEllerOppdaterResultat result =
                            opprettEllerOppdater(
                                    sektor, integrasjonUtils);
                    sluttResultat.add(result);
                });

        return sluttResultat;
    }


    private OpprettEllerOppdaterResultat opprettEllerOppdater(
            List<? extends Virksomhetsklassifikasjon> virksomhetsklassifikasjoner,
            VirksomhetsklassifikasjonIntegrasjonUtils integrasjonUtils) {

        final OpprettEllerOppdaterResultat sluttResultat = new OpprettEllerOppdaterResultat();

        virksomhetsklassifikasjoner.forEach(
                virksomhetsklassifikasjon -> {
                    OpprettEllerOppdaterResultat result =
                            opprettEllerOppdater(
                                    virksomhetsklassifikasjon, integrasjonUtils);
                    sluttResultat.add(result);
                });

        return sluttResultat;
    }

    private OpprettEllerOppdaterResultat opprettEllerOppdater(
            Virksomhetsklassifikasjon virksomhetsklassifikasjon,
            VirksomhetsklassifikasjonIntegrasjonUtils integrasjonUtils) {
        final OpprettEllerOppdaterResultat resultat = new OpprettEllerOppdaterResultat();

        integrasjonUtils.getFetchFunction()
                .apply(virksomhetsklassifikasjon)
                .ifPresentOrElse(
                        eksisterende -> {
                            if (!eksisterende.equals(virksomhetsklassifikasjon)) {
                                integrasjonUtils.getUpdateFunction().apply(eksisterende, virksomhetsklassifikasjon);
                                resultat.setAntallRadOppdatert(1);
                            }
                        },
                        () -> {
                            integrasjonUtils.getCreateFunction().apply(virksomhetsklassifikasjon);
                            resultat.setAntallRadOpprettet(1);
                        });

        return resultat;
    }

    private OpprettEllerOppdaterResultat opprettEllerOppdaterSektor(
            Sektor sektor) {
        final OpprettEllerOppdaterResultat resultat = new OpprettEllerOppdaterResultat();
        Optional<Sektor> hentetSektor = hentSektor(sektor);

        if (hentetSektor.isPresent()) {
            if (!hentetSektor.get().equals(sektor)) {
                oppdater(sektor);
                resultat.setAntallRadOppdatert(1);
                return resultat;
            }
        } else {
            opprett(sektor);
            resultat.setAntallRadOpprettet(1);
        }
        return resultat;
    }



    // Sektor utils

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
                            "insert into SEKTOR (kode, navn)  values (:kode, :navn)", namedParameters);
    }

    public int oppdater(Sektor sektor) {
        SqlParameterSource namedParameters =
                new MapSqlParameterSource()
                        .addValue(KODE, sektor.getKode())
                        .addValue(NAVN, sektor.getNavn());

        return namedParameterJdbcTemplate.update(
                "update SEKTOR set navn = :navn where kode = :kode", namedParameters);
    }


    private static Sektor mapTilSektor(ResultSet rs) throws SQLException {
        return new Sektor(rs.getString(KODE), rs.getString(NAVN));
    }



    // Til senere
    private Optional<Næring> fetchNæring(Virksomhetsklassifikasjon næring) {
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
