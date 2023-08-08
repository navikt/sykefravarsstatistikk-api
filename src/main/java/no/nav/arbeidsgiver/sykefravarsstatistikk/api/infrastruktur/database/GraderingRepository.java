package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Statistikkategori.BRANSJE;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Statistikkategori.NÆRING;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.NæringOgNæringskode5siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.VirksomhetMetadataNæringskode5siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Virksomhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.Bransjeprogram;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Sykefraværsdata;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class GraderingRepository {

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public GraderingRepository(
      @Qualifier("sykefravarsstatistikkJdbcTemplate")
          NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }

  public List<VirksomhetMetadataNæringskode5siffer> hentVirksomhetMetadataNæringskode5siffer(
      ÅrstallOgKvartal årstallOgKvartal) {
    try {
      return namedParameterJdbcTemplate.query(
          "select arstall, kvartal, orgnr, naring, naring_kode"
              + " from sykefravar_statistikk_virksomhet_med_gradering "
              + " where "
              + " arstall = :årstall "
              + " and kvartal = :kvartal "
              + " group by arstall, kvartal, orgnr, naring, naring_kode"
              + " order by arstall, kvartal, orgnr, naring, naring_kode",
          new MapSqlParameterSource()
              .addValue("årstall", årstallOgKvartal.getÅrstall())
              .addValue("kvartal", årstallOgKvartal.getKvartal()),
          (rs, rowNum) ->
              new VirksomhetMetadataNæringskode5siffer(
                  new Orgnr(rs.getString("orgnr")),
                  new ÅrstallOgKvartal(rs.getInt("arstall"), rs.getInt("kvartal")),
                  new NæringOgNæringskode5siffer(
                      rs.getString("naring"), rs.getString("naring_kode"))));
    } catch (EmptyResultDataAccessException e) {
      return Collections.emptyList();
    }
  }

  public List<UmaskertSykefraværForEttKvartal> hentSykefraværMedGradering(Virksomhet virksomhet) {
    try {
      return namedParameterJdbcTemplate.query(
          "select arstall, kvartal,"
              + " sum(tapte_dagsverk_gradert_sykemelding) as "
              + "sum_tapte_dagsverk_gradert_sykemelding, "
              + " sum(tapte_dagsverk) as sum_tapte_dagsverk, "
              + " sum(antall_personer) as sum_antall_personer "
              + " from sykefravar_statistikk_virksomhet_med_gradering "
              + " where "
              + " orgnr = :orgnr "
              + " and rectype = :rectype "
              + " group by arstall, kvartal"
              + " order by arstall, kvartal",
          new MapSqlParameterSource()
              .addValue("orgnr", virksomhet.getOrgnr().getVerdi())
              .addValue("rectype", RECTYPE_FOR_VIRKSOMHET),
          (rs, rowNum) -> mapTilUmaskertSykefraværForEttKvartal(rs));
    } catch (EmptyResultDataAccessException e) {
      return Collections.emptyList();
    }
  }

  public Sykefraværsdata hentGradertSykefraværAlleKategorier(@NotNull Virksomhet virksomhet) {

    Næring næring = new Næring(virksomhet.getNæringskode().getKode(), "");
    Optional<Bransje> maybeBransje = Bransjeprogram.finnBransje(virksomhet.getNæringskode());

    Map<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> data = new HashMap<>();

    data.put(Statistikkategori.VIRKSOMHET, hentSykefraværMedGradering(virksomhet));

    if (maybeBransje.isEmpty()) {
      data.put(NÆRING, hentSykefraværMedGradering(næring));
    } else if (maybeBransje.get().erDefinertPåFemsiffernivå()) {
      data.put(BRANSJE, hentSykefraværMedGradering(maybeBransje.get()));
    } else {
      data.put(BRANSJE, hentSykefraværMedGradering(næring));
    }
    return new Sykefraværsdata(data);
  }

  public List<UmaskertSykefraværForEttKvartal> hentSykefraværMedGradering(Næring næring) {
    try {
      return namedParameterJdbcTemplate.query(
          "select arstall, kvartal,"
              + " sum(tapte_dagsverk_gradert_sykemelding) as "
              + "sum_tapte_dagsverk_gradert_sykemelding, "
              + " sum(tapte_dagsverk) as sum_tapte_dagsverk, "
              + " sum(antall_personer) as sum_antall_personer "
              + " from sykefravar_statistikk_virksomhet_med_gradering "
              + " where "
              + " naring = :naring "
              + " and rectype = :rectype "
              + " group by arstall, kvartal"
              + " order by arstall, kvartal",
          new MapSqlParameterSource()
              .addValue("naring", næring.getKode().substring(0, 2))
              .addValue("rectype", RECTYPE_FOR_VIRKSOMHET),
          (rs, rowNum) -> mapTilUmaskertSykefraværForEttKvartal(rs));
    } catch (EmptyResultDataAccessException e) {
      return Collections.emptyList();
    }
  }

  public List<UmaskertSykefraværForEttKvartal> hentSykefraværMedGradering(Bransje bransje) {
    try {
      return namedParameterJdbcTemplate.query(
          "select arstall, kvartal,"
              + " sum(tapte_dagsverk_gradert_sykemelding) as "
              + "sum_tapte_dagsverk_gradert_sykemelding, "
              + " sum(tapte_dagsverk) as sum_tapte_dagsverk, "
              + " sum(antall_personer) as sum_antall_personer "
              + " from sykefravar_statistikk_virksomhet_med_gradering "
              + " where "
              + " naring_kode in (:naringKoder) "
              + " and rectype = :rectype "
              + " group by arstall, kvartal"
              + " order by arstall, kvartal",
          new MapSqlParameterSource()
              .addValue("naringKoder", bransje.getKoderSomSpesifisererNæringer())
              .addValue("rectype", RECTYPE_FOR_VIRKSOMHET),
          (rs, rowNum) -> mapTilUmaskertSykefraværForEttKvartal(rs));
    } catch (EmptyResultDataAccessException e) {
      return Collections.emptyList();
    }
  }

  private UmaskertSykefraværForEttKvartal mapTilUmaskertSykefraværForEttKvartal(ResultSet rs)
      throws SQLException {
    return new UmaskertSykefraværForEttKvartal(
        new ÅrstallOgKvartal(rs.getInt("arstall"), rs.getInt("kvartal")),
        rs.getBigDecimal("sum_tapte_dagsverk_gradert_sykemelding"),
        rs.getBigDecimal("sum_tapte_dagsverk"),
        rs.getInt("sum_antall_personer"));
  }
}
