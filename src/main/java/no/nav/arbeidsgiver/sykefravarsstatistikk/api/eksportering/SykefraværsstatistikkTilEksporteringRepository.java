package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkLand;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkSektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhetUtenVarighet;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SykefraværsstatistikkTilEksporteringRepository {

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public SykefraværsstatistikkTilEksporteringRepository(
      @Qualifier("sykefravarsstatistikkJdbcTemplate")
          NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }

  public SykefraværsstatistikkLand hentSykefraværprosentLand(ÅrstallOgKvartal årstallOgKvartal) {
    try {
      List<SykefraværsstatistikkLand> resultat =
          namedParameterJdbcTemplate.query(
              "select arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk "
                  + "from sykefravar_statistikk_land "
                  + "where arstall = :arstall and kvartal = :kvartal order by arstall, kvartal",
              new MapSqlParameterSource()
                  .addValue("arstall", årstallOgKvartal.getÅrstall())
                  .addValue("kvartal", årstallOgKvartal.getKvartal()),
              (rs, rowNum) -> mapTilSykefraværsstatistikkLand(rs));

      if (resultat.size() != 1) {
        return null;
      }
      return resultat.get(0);
    } catch (EmptyResultDataAccessException e) {
      return null;
    }
  }

  public List<SykefraværsstatistikkSektor> hentSykefraværprosentAlleSektorer(
      ÅrstallOgKvartal årstallOgKvartal) {
    try {
      return namedParameterJdbcTemplate.query(
          "select sektor_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk "
              + "from sykefravar_statistikk_sektor "
              + "where arstall = :arstall and kvartal = :kvartal "
              + "order by arstall, kvartal, sektor_kode",
          new MapSqlParameterSource()
              .addValue("arstall", årstallOgKvartal.getÅrstall())
              .addValue("kvartal", årstallOgKvartal.getKvartal()),
          (rs, rowNum) -> mapTilSykefraværsstatistikkSektor(rs));
    } catch (EmptyResultDataAccessException e) {
      return Collections.emptyList();
    }
  }

  /* Sykefraværsprosent Næring */

  public List<SykefraværsstatistikkNæring> hentSykefraværprosentAlleNæringer(
      ÅrstallOgKvartal sisteÅrstallOgKvartal, int antallKvartaler) {
    if (antallKvartaler == 2) {
      return hentSykefraværprosentAlleNæringer(sisteÅrstallOgKvartal);
    }

    return hentSykefraværprosentAlleNæringer(
        sisteÅrstallOgKvartal, sisteÅrstallOgKvartal.minusKvartaler(antallKvartaler - 1));
  }

  public List<SykefraværsstatistikkNæring> hentSykefraværprosentAlleNæringer(
      ÅrstallOgKvartal årstallOgKvartal) {
    return hentSykefraværprosentAlleNæringer(årstallOgKvartal, årstallOgKvartal);
  }

  public List<SykefraværsstatistikkNæring> hentSykefraværprosentAlleNæringer(
      ÅrstallOgKvartal fraÅrstallOgKvartal, ÅrstallOgKvartal tilÅrstallOgKvartal) {

    try {
      return namedParameterJdbcTemplate.query(
          "select naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk "
              + "from sykefravar_statistikk_naring "
              + " where "
              + getWhereClause(fraÅrstallOgKvartal, tilÅrstallOgKvartal)
              + "order by (arstall, kvartal) desc, naring_kode",
          (rs, rowNum) -> mapTilSykefraværsstatistikkNæring(rs));
    } catch (EmptyResultDataAccessException e) {
      return Collections.emptyList();
    }
  }

  public List<SykefraværsstatistikkNæring5Siffer> hentSykefraværprosentAlleNæringer5Siffer(
      ÅrstallOgKvartal årstallOgKvartal) {
    return hentSykefraværprosentAlleNæringer5Siffer(årstallOgKvartal, årstallOgKvartal);
  }

  public List<SykefraværsstatistikkNæring5Siffer> hentSykefraværprosentAlleNæringer5Siffer(
      ÅrstallOgKvartal fraÅrstallOgKvartal, ÅrstallOgKvartal tilÅrstallOgKvartal) {

    try {
      return namedParameterJdbcTemplate.query(
          "select naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk "
              + "from sykefravar_statistikk_naring5siffer "
              + " where "
              + getWhereClause(fraÅrstallOgKvartal, tilÅrstallOgKvartal)
              + "order by (arstall, kvartal) desc, naring_kode",
          new MapSqlParameterSource()
              .addValue("arstall", fraÅrstallOgKvartal.getÅrstall())
              .addValue("kvartal", fraÅrstallOgKvartal.getKvartal()),
          (rs, rowNum) -> mapTilSykefraværsstatistikkNæring5Siffer(rs));
    } catch (EmptyResultDataAccessException e) {
      return Collections.emptyList();
    }
  }

  public List<SykefraværsstatistikkVirksomhetUtenVarighet> hentSykefraværAlleVirksomheter(
      ÅrstallOgKvartal fraÅrstallOgKvartal) {
    return hentSykefraværAlleVirksomheter(fraÅrstallOgKvartal, fraÅrstallOgKvartal);
  }

  public List<SykefraværsstatistikkVirksomhetUtenVarighet> hentSykefraværAlleVirksomheter(
      ÅrstallOgKvartal fraÅrstallOgKvartal, ÅrstallOgKvartal tilÅrstallOgKvartal) {
    try {
      return namedParameterJdbcTemplate.query(
          "select arstall, kvartal, orgnr, "
              + "sum(tapte_dagsverk) as tapte_dagsverk, "
              + "sum(mulige_dagsverk) as mulige_dagsverk, "
              + "sum(antall_personer) as antall_personer "
              + "from sykefravar_statistikk_virksomhet "
              + " where "
              + getWhereClause(fraÅrstallOgKvartal, tilÅrstallOgKvartal)
              + " group by arstall, kvartal, orgnr",
          (rs, rowNum) -> mapTilSykefraværsstatistikkVirksomhet(rs));
    } catch (EmptyResultDataAccessException e) {
      return Collections.emptyList();
    }
  }

  // Utilities
  @NotNull
  private static String getWhereClause(
      ÅrstallOgKvartal fraÅrstallOgKvartal, ÅrstallOgKvartal tilÅrstallOgKvartal) {
    // Alltid sjekk input før verdi skal til SQL
    sjekkÅrstallOgKvartal(fraÅrstallOgKvartal, tilÅrstallOgKvartal);

    List<ÅrstallOgKvartal> årstallOgKvartalListe =
        ÅrstallOgKvartal.range(fraÅrstallOgKvartal, tilÅrstallOgKvartal);
    return årstallOgKvartalListe.stream()
        .map(
            årstallOgKvartal ->
                String.format(
                    "(arstall = %d and kvartal = %d) ",
                    årstallOgKvartal.getÅrstall(), årstallOgKvartal.getKvartal()))
        .collect(Collectors.joining("or "));
  }

  private static void sjekkÅrstallOgKvartal(ÅrstallOgKvartal... årstallOgKvartalListe) {
    Arrays.stream(årstallOgKvartalListe)
        .forEach(
            årstallOgKvartal -> {
              if (årstallOgKvartal.getÅrstall() < 2010 || årstallOgKvartal.getÅrstall() > 2100) {
                throw new IllegalArgumentException(
                    String.format(
                        "Årstall skal være mellom 2010 og 2100. Fikk '%d'",
                        årstallOgKvartal.getÅrstall()));
              }

              if (årstallOgKvartal.getKvartal() < 1 || årstallOgKvartal.getKvartal() > 4) {
                throw new IllegalArgumentException(
                    String.format(
                        "Kvartal skal være mellom 1 og 4. Fikk '%d'",
                        årstallOgKvartal.getKvartal()));
              }
            });
  }

  private SykefraværsstatistikkLand mapTilSykefraværsstatistikkLand(ResultSet rs)
      throws SQLException {
    return new SykefraværsstatistikkLand(
        rs.getInt("arstall"),
        rs.getInt("kvartal"),
        rs.getInt("antall_personer"),
        rs.getBigDecimal("tapte_dagsverk"),
        rs.getBigDecimal("mulige_dagsverk"));
  }

  private SykefraværsstatistikkSektor mapTilSykefraværsstatistikkSektor(ResultSet rs)
      throws SQLException {
    return new SykefraværsstatistikkSektor(
        rs.getInt("arstall"),
        rs.getInt("kvartal"),
        rs.getString("sektor_kode"),
        rs.getInt("antall_personer"),
        rs.getBigDecimal("tapte_dagsverk"),
        rs.getBigDecimal("mulige_dagsverk"));
  }

  private SykefraværsstatistikkNæring mapTilSykefraværsstatistikkNæring(ResultSet rs)
      throws SQLException {
    return new SykefraværsstatistikkNæring(
        rs.getInt("arstall"),
        rs.getInt("kvartal"),
        rs.getString("naring_kode"),
        rs.getInt("antall_personer"),
        rs.getBigDecimal("tapte_dagsverk"),
        rs.getBigDecimal("mulige_dagsverk"));
  }

  private SykefraværsstatistikkNæring5Siffer mapTilSykefraværsstatistikkNæring5Siffer(ResultSet rs)
      throws SQLException {
    return new SykefraværsstatistikkNæring5Siffer(
        rs.getInt("arstall"),
        rs.getInt("kvartal"),
        rs.getString("naring_kode"),
        rs.getInt("antall_personer"),
        rs.getBigDecimal("tapte_dagsverk"),
        rs.getBigDecimal("mulige_dagsverk"));
  }

  private SykefraværsstatistikkVirksomhetUtenVarighet mapTilSykefraværsstatistikkVirksomhet(
      ResultSet rs) throws SQLException {
    return new SykefraværsstatistikkVirksomhetUtenVarighet(
        rs.getInt("arstall"),
        rs.getInt("kvartal"),
        rs.getString("orgnr"),
        rs.getInt("antall_personer"),
        rs.getBigDecimal("tapte_dagsverk"),
        rs.getBigDecimal("mulige_dagsverk"));
  }
}
