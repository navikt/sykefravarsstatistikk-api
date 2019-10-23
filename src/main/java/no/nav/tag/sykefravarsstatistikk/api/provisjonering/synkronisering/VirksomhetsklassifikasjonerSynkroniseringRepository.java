package no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering;

import no.nav.tag.sykefravarsstatistikk.api.domene.OpprettEllerOppdaterResultat;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Profile({"local", "dev"})
@Component
public class VirksomhetsklassifikasjonerSynkroniseringRepository {
    public static final String KODE = "kode";
    public static final String NAVN = "navn";

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    public VirksomhetsklassifikasjonerSynkroniseringRepository(
            @Qualifier("sykefravarsstatistikkJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate
    )
    {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }


    public OpprettEllerOppdaterResultat opprettEllerOppdaterSektorer(List<Sektor> sektorerIDatavarehus) {
        final OpprettEllerOppdaterResultat sluttResultat = new OpprettEllerOppdaterResultat();

        sektorerIDatavarehus.stream().forEach(
                sektor -> {
                    OpprettEllerOppdaterResultat result = opprettEllerOppdaterSektor(sektor);
                    sluttResultat.add(result);
                });

        return sluttResultat;
    }


    private OpprettEllerOppdaterResultat opprettEllerOppdaterSektor(Sektor sektor) {
        final OpprettEllerOppdaterResultat resultat = new OpprettEllerOppdaterResultat();

        hentSektor(sektor.getKode())
            .ifPresentOrElse(
                eksisterendeSektor -> {
                  if (!eksisterendeSektor.equals(sektor)) {
                    update(eksisterendeSektor, sektor);
                    resultat.setAntallRadOppdatert(1);
                  }
                },
                () -> {
                  create(sektor);
                  resultat.setAntallRadOpprettet(1);
                });

        return resultat;
    }

    private Optional<Sektor> hentSektor(String kode) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue(KODE, kode);

        try {
            Sektor sektor = namedParameterJdbcTemplate.queryForObject(
                    "select kode, navn from sektor where kode = :kode",
                    namedParameters,
                    (resultSet, rowNum) -> mapTilSektor(resultSet)
            );
            return Optional.of(sektor);
        } catch (EmptyResultDataAccessException erdae) {
            return Optional.empty();
        }
    }

    private int create(Sektor sektor) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue(KODE, sektor.getKode())
                .addValue(NAVN, sektor.getNavn());

        return namedParameterJdbcTemplate.update(
                "insert into SEKTOR (kode, navn)  values (:kode, :navn)",
                namedParameters);
    }

    private int update(Sektor eksisterendeSektor, Sektor sektor) {
                SqlParameterSource namedParameters = new MapSqlParameterSource()
                        .addValue(KODE, eksisterendeSektor.getKode())
                        .addValue(NAVN, sektor.getNavn());

        return namedParameterJdbcTemplate.update(
                "update SEKTOR set navn = :navn where kode = :kode",
                namedParameters);
    }

    private Sektor mapTilSektor(ResultSet rs) throws SQLException {
        return new Sektor (
                rs.getString(KODE),
                rs.getString(NAVN)
        );
    }


}
