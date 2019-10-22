package no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering;

import no.nav.tag.sykefravarsstatistikk.api.domene.Tuple;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
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
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class VirksomhetsklassifikasjonerSynkroniseringRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    public VirksomhetsklassifikasjonerSynkroniseringRepository(
            @Qualifier("applikasjonJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }


    public Tuple<Integer, Integer> opprettEllerOppdaterSektorer(List<Sektor> sektorerIDatavarehus) {
        final AtomicInteger antallOppdaterteSektorer  = new AtomicInteger(0);
        final AtomicInteger antallOpprettetSektorer = new AtomicInteger(0);

        sektorerIDatavarehus.stream().forEach(
                sektorIDatavarehus ->
                        hentSektor(sektorIDatavarehus.getKode())
                                .ifPresentOrElse(
                                        eksisterendeSektor -> {
                                            if (!eksisterendeSektor.equals(sektorIDatavarehus)) {
                                                update(eksisterendeSektor, sektorIDatavarehus);
                                                antallOppdaterteSektorer.getAndIncrement();
                                            }
                                        },
                                        () -> {
                                            antallOpprettetSektorer.getAndIncrement();
                                            create(sektorIDatavarehus);
                                        }
                                )
        );
        return new Tuple<>(antallOpprettetSektorer.get(), antallOppdaterteSektorer.get());
    }

    private Optional<Sektor> hentSektor(String kode) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("kode", kode);

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
                .addValue("kode", sektor.getKode())
                .addValue("navn", sektor.getNavn());

        return namedParameterJdbcTemplate.update(
                "insert into SEKTOR (kode, navn)  values (:kode, :navn)",
                namedParameters);
    }

    private int update(Sektor eksisterendeSektor, Sektor sektor) {
                SqlParameterSource namedParameters = new MapSqlParameterSource()
                        .addValue("kode", eksisterendeSektor.getKode())
                        .addValue("navn", sektor.getNavn());

        return namedParameterJdbcTemplate.update(
                "update SEKTOR set navn = :navn where kode = :kode",
                namedParameters);
    }

    private Sektor mapTilSektor(ResultSet rs) throws SQLException {
        return new Sektor (
                rs.getString("kode"),
                rs.getString("navn")
        );
    }


}
