package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class PubliseringsdatoerRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;


    public PubliseringsdatoerRepository(
          @Qualifier("sykefravarsstatistikkJdbcTemplate")
          NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        this.jdbcTemplate = namedParameterJdbcTemplate;
    }


    public List<PubliseringsdatoDbDto> hentPubliseringsdatoer() {
        try {
            return jdbcTemplate.query("select * from publiseringsdatoer",
                  new HashMap<>(),
                  (rs, rowNum) -> new PubliseringsdatoDbDto(
                        rs.getInt("rapport_periode"),
                        rs.getDate("offentlig_dato"),
                        rs.getDate("oppdatert_i_dvh"),
                        rs.getString("aktivitet")
                  )
            );
        } catch (EmptyResultDataAccessException e) {
            log.error("Fant ikke publiseringsdatoer i databasen, returnerer tom liste");
            return Collections.emptyList();
        }
    }

    public void oppdaterPubliseringsdatoer(List<PubliseringsdatoDbDto> data) {
        int antallRadetSlettet = jdbcTemplate.update(
              "delete from publiseringsdatoer", new MapSqlParameterSource()
        );
        log.info("Antall rader slettet fra 'publiseringsdatoer': " + antallRadetSlettet);

        int antallRaderSattInn =
              data.stream().mapToInt(
                    publiseringsdato -> jdbcTemplate.update(
                          "insert into publiseringsdatoer "
                                + "(rapport_periode, offentlig_dato, oppdatert_i_dvh, aktivitet) "
                                + "values "
                                + "(:rapport_periode, :offentlig_dato, :oppdatert_i_dvh, "
                                + ":aktivitet)",
                          new MapSqlParameterSource()
                                .addValue("rapport_periode", publiseringsdato.getRapportPeriode())
                                .addValue("offentlig_dato", publiseringsdato.getOffentligDato())
                                .addValue("oppdatert_i_dvh", publiseringsdato.getOppdatertDato())
                                .addValue("aktivitet", publiseringsdato.getAktivitet())
                    )).sum();
        log.info("Antall rader satt inn i 'publiseringsdatoer': " + antallRaderSattInn);
    }

    public void oppdaterSisteImporttidspunkt(ÅrstallOgKvartal årstallOgKvartal) {
        jdbcTemplate.update(
              "insert into importtidspunkt "
                    + "(aarstall, kvartal) "
                    + "values "
                    + "(:aarstall, :kvartal)",
              new MapSqlParameterSource()
                    .addValue("aarstall", årstallOgKvartal.getÅrstall())
                    .addValue("kvartal", årstallOgKvartal.getKvartal())
        );
        log.info("Oppdaterte tidspunkt for import av sykefraværstatistikk for " + årstallOgKvartal);
    }

    public ImporttidspunktDto hentSisteImporttidspunktMedPeriode() {
        return jdbcTemplate.query(
              "select * from importtidspunkt "
                    + "order by importert desc "
                    + "limit 1",
              new HashMap<>(),
              (rs, rowNum) -> new ImporttidspunktDto(
                    rs.getTimestamp("importert"),
                    rs.getString("aarstall"),
                    rs.getString("kvartal")
              )
        ).get(0);
    }
}