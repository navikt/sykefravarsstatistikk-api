package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.Bransje;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Component
public class KvartalsvisSykefraværRepository {

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public KvartalsvisSykefraværRepository(
      @Qualifier("sykefravarsstatistikkJdbcTemplate")
          NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }

  public List<SykefraværForEttKvartal> hentKvartalsvisSykefraværprosentLand() {
    try {
      return namedParameterJdbcTemplate.query(
          "SELECT tapte_dagsverk, mulige_dagsverk, antall_personer, arstall, kvartal "
              + "FROM sykefravar_statistikk_land "
              + "ORDER BY arstall, kvartal ",
          new HashMap<>(),
          (rs, rowNum) -> mapTilKvartalsvisSykefraværprosent(rs));
    } catch (EmptyResultDataAccessException e) {
      return Collections.emptyList();
    }
  }

  public List<SykefraværForEttKvartal> hentKvartalsvisSykefraværprosentSektor(Sektor sektor) {
    try {
      return namedParameterJdbcTemplate.query(
          "SELECT tapte_dagsverk, mulige_dagsverk, antall_personer, arstall, kvartal "
              + "FROM sykefravar_statistikk_sektor "
              + "where sektor_kode = :sektorKode "
              + "ORDER BY arstall, kvartal ",
          new MapSqlParameterSource().addValue("sektorKode", sektor.getKode()),
          (rs, rowNum) -> mapTilKvartalsvisSykefraværprosent(rs));
    } catch (EmptyResultDataAccessException e) {
      return Collections.emptyList();
    }
  }

  public List<SykefraværForEttKvartal> hentKvartalsvisSykefraværprosentNæring(Næring næring) {
    try {
      return namedParameterJdbcTemplate.query(
          "SELECT tapte_dagsverk, mulige_dagsverk, antall_personer, arstall, kvartal "
              + "FROM sykefravar_statistikk_naring "
              + "where naring_kode = :naringKode "
              + "ORDER BY arstall, kvartal ",
          new MapSqlParameterSource().addValue("naringKode", næring.getKode()),
          (rs, rowNum) -> mapTilKvartalsvisSykefraværprosent(rs));
    } catch (EmptyResultDataAccessException e) {
      return Collections.emptyList();
    }
  }

  public List<SykefraværForEttKvartal> hentKvartalsvisSykefraværprosentBransje(Bransje bransje) {
    try {
      return namedParameterJdbcTemplate.query(
          "SELECT sum(tapte_dagsverk) as tapte_dagsverk, sum(mulige_dagsverk) as mulige_dagsverk, sum(antall_personer) as antall_personer, arstall, kvartal "
              + "FROM sykefravar_statistikk_naring5siffer "
              + "where naring_kode in (:naringKoder) "
              + "group by arstall, kvartal "
              + "ORDER BY arstall, kvartal ",
          new MapSqlParameterSource()
              .addValue("naringKoder", bransje.getKoderSomSpesifisererNæringer()),
          (rs, rowNum) -> mapTilKvartalsvisSykefraværprosent(rs));
    } catch (EmptyResultDataAccessException e) {
      return Collections.emptyList();
    }
  }

  public List<SykefraværForEttKvartal> hentKvartalsvisSykefraværprosentVirksomhet(
      Virksomhet virksomhet) {
    try {
      return namedParameterJdbcTemplate.query(
          "SELECT sum(tapte_dagsverk) as tapte_dagsverk,"
              + "sum(mulige_dagsverk) as mulige_dagsverk,"
              + "sum(antall_personer) as antall_personer,"
              + "arstall, kvartal "
              + "FROM sykefravar_statistikk_virksomhet "
              + "where orgnr = :orgnr "
              + "GROUP BY arstall, kvartal "
              + "ORDER BY arstall, kvartal ",
          new MapSqlParameterSource().addValue("orgnr", virksomhet.getOrgnr().getVerdi()),
          (rs, rowNum) -> mapTilKvartalsvisSykefraværprosent(rs));
    } catch (EmptyResultDataAccessException e) {
      return Collections.emptyList();
    }
  }

  private SykefraværForEttKvartal mapTilKvartalsvisSykefraværprosent(ResultSet rs)
      throws SQLException {
    return new SykefraværForEttKvartal(
        new ÅrstallOgKvartal(rs.getInt("arstall"), rs.getInt("kvartal")),
        rs.getBigDecimal("tapte_dagsverk"),
        rs.getBigDecimal("mulige_dagsverk"),
        rs.getInt("antall_personer"));
  }
}
