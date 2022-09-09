package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer;

import io.vavr.control.Option;
import io.vavr.control.Try;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class PubliseringsdatoerRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;


  public PubliseringsdatoerRepository(
      @Qualifier("sykefravarsstatistikkJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate
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
              rs.getString("aktivitet")));
    } catch (EmptyResultDataAccessException e) {
      log.error("Fant ikke publiseringsdatoer i databasen, returnerer tom liste");
      return Collections.emptyList();
    }
  }


  public void oppdaterPubliseringsdatoer(List<PubliseringsdatoDbDto> data) {
    int antallRadetSlettet =
        jdbcTemplate.update("delete from publiseringsdatoer", new MapSqlParameterSource());
    log.info("Antall rader slettet fra 'publiseringsdatoer': " + antallRadetSlettet);

    int antallRaderSattInn = data
        .stream()
        .mapToInt(publiseringsdato -> jdbcTemplate.update("insert into publiseringsdatoer "
                + "(rapport_periode, "
                + "offentlig_dato, "
                + "oppdatert_i_dvh, "
                + "aktivitet) "
                + "values "
                + "(:rapport_periode, "
                + ":offentlig_dato, "
                + ":oppdatert_i_dvh, "
                + ":aktivitet)",
            new MapSqlParameterSource()
                .addValue("rapport_periode", publiseringsdato.getRapportPeriode())
                .addValue("offentlig_dato", publiseringsdato.getOffentligDato())
                .addValue("oppdatert_i_dvh", publiseringsdato.getOppdatertDato())
                .addValue("aktivitet", publiseringsdato.getAktivitet())))
        .sum();
    log.info("Antall rader satt inn i 'publiseringsdatoer': " + antallRaderSattInn);
  }


  public void oppdaterSisteImporttidspunkt(ÅrstallOgKvartal årstallOgKvartal) {
    jdbcTemplate.update(
        "insert into importtidspunkt (aarstall, kvartal, importert) values "
            + "(:aarstall, :kvartal, :importert)",
        new MapSqlParameterSource()
            .addValue("aarstall", årstallOgKvartal.getÅrstall())
            .addValue("kvartal", årstallOgKvartal.getKvartal())
            .addValue("importert", LocalDateTime.now()));
    log.info("Oppdaterte tidspunkt for import av sykefraværstatistikk for " + årstallOgKvartal);
  }

  public Option<ImporttidspunktDto> hentSisteImporttidspunkt() {
    return Try.of(this::hentSisteImporttidspunktFraDb)
        .onFailure(feil -> log.error("Klarte ikke hente ut siste importtidspunkt: " + feil))
        .toOption();
  }


  private ImporttidspunktDto hentSisteImporttidspunktFraDb() {
    return jdbcTemplate
        .query("select * from importtidspunkt order by importert desc "
                + "fetch first 1 rows only",
            new HashMap<>(),
            (rs, rowNum) -> new ImporttidspunktDto(
                rs.getTimestamp("importert"),
                new ÅrstallOgKvartal(
                    rs.getInt("aarstall"),
                    rs.getInt("kvartal")))
        ).get(0);
  }
}
