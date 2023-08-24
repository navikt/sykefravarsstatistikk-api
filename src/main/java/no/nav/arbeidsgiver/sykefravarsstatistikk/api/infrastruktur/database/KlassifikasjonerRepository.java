package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Næring;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KlassifikasjonerRepository {

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public KlassifikasjonerRepository(
      @Qualifier("sykefravarsstatistikkJdbcTemplate")
          NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }

  public Næring hentNæring(String kode) {
    SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("kode", kode);

    return namedParameterJdbcTemplate.queryForObject(
        "SELECT * FROM naring WHERE kode = :kode",
        namedParameters,
        (rs, rowNum) -> new Næring(rs.getString("kode"), rs.getString("navn")));
  }

  public List<Næring> hentAlleNæringer() {

    return namedParameterJdbcTemplate.query(
        "SELECT * FROM naring ",
        (rs, rowNum) -> new Næring(rs.getString("kode"), rs.getString("navn")));
  }
}
