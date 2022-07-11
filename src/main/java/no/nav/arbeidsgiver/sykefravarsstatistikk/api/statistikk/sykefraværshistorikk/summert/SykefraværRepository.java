package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Virksomhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjeprogram;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.Historikkdata;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.*;

@Component
public class SykefraværRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public SykefraværRepository(
          @Qualifier("sykefravarsstatistikkJdbcTemplate")
                NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public List<UmaskertSykefraværForEttKvartal> hentUmaskertSykefravær(
          Virksomhet virksomhet, ÅrstallOgKvartal fraÅrstallOgKvartal) {
        try {
            return sorterKronologisk(namedParameterJdbcTemplate.query(
                  "SELECT sum(tapte_dagsverk) as tapte_dagsverk,"
                        + "sum(mulige_dagsverk) as mulige_dagsverk,"
                        + "sum(antall_personer) as antall_personer,"
                        + "arstall, kvartal "
                        + "FROM sykefravar_statistikk_virksomhet "
                        + "where orgnr = :orgnr "
                        + "and ("
                        + "  (arstall = :arstall and kvartal >= :kvartal) "
                        + "  or "
                        + "  (arstall > :arstall)"
                        + ") "
                        + "GROUP BY arstall, kvartal "
                        + "ORDER BY arstall, kvartal ",
                  new MapSqlParameterSource()
                        .addValue("orgnr", virksomhet.getOrgnr().getVerdi())
                        .addValue("arstall", fraÅrstallOgKvartal.getÅrstall())
                        .addValue("kvartal", fraÅrstallOgKvartal.getKvartal()),
                  (rs, rowNum) -> mapTilUmaskertSykefraværForEttKvartal(rs)));
        } catch (EmptyResultDataAccessException e) {
            return emptyList();
        }
    }

    public List<UmaskertSykefraværForEttKvartal> hentUmaskertSykefravær(
          Bransje bransje, ÅrstallOgKvartal fraÅrstallOgKvartal) {

        if (bransje.erDefinertPåTosiffernivå()) {
            throw (new IllegalArgumentException("Denne metoden funker bare for 5-siffer næringskoder"));
        }

        try {
            return namedParameterJdbcTemplate.query(
                  "SELECT sum(tapte_dagsverk) as tapte_dagsverk, sum(mulige_dagsverk) as mulige_dagsverk, sum(antall_personer) as antall_personer, arstall, kvartal " +
                        "FROM sykefravar_statistikk_naring5siffer " +
                        "where naring_kode in (:naringKoder) " +
                        "and (" +
                        "  (arstall = :arstall and kvartal >= :kvartal) " +
                        "  or " +
                        "  (arstall > :arstall)" +
                        ") " +
                        "group by arstall, kvartal " +
                        "ORDER BY arstall, kvartal ",
                  new MapSqlParameterSource()
                        .addValue("naringKoder", bransje.getKoderSomSpesifisererNæringer())
                        .addValue("arstall", fraÅrstallOgKvartal.getÅrstall())
                        .addValue("kvartal", fraÅrstallOgKvartal.getKvartal()),
                  (rs, rowNum) -> mapTilUmaskertSykefraværForEttKvartal(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    public List<UmaskertSykefraværForEttKvartal> hentUmaskertSykefravær(
          Næring næring, ÅrstallOgKvartal fraÅrstallOgKvartal) {
        try {
            return sorterKronologisk(namedParameterJdbcTemplate.query(
                  "SELECT tapte_dagsverk, mulige_dagsverk, antall_personer, arstall, kvartal "
                        + "FROM sykefravar_statistikk_naring "
                        + "where naring_kode = :naringKode "
                        + "and ("
                        + "  (arstall = :arstall and kvartal >= :kvartal) "
                        + "  or "
                        + "  (arstall > :arstall)"
                        + ") "
                        + "ORDER BY arstall, kvartal ",
                  new MapSqlParameterSource()
                        .addValue("naringKode", næring.getKode())
                        .addValue("arstall", fraÅrstallOgKvartal.getÅrstall())
                        .addValue("kvartal", fraÅrstallOgKvartal.getKvartal()),
                  (rs, rowNum) -> mapTilUmaskertSykefraværForEttKvartal(rs)));
        } catch (EmptyResultDataAccessException e) {
            return emptyList();
        }
    }

    public List<UmaskertSykefraværForEttKvartal> hentUmaskertSykefraværForNorge(ÅrstallOgKvartal fra) {
        try {
            try {
                return sorterKronologisk(namedParameterJdbcTemplate.query(
                      "SELECT tapte_dagsverk, mulige_dagsverk, antall_personer, arstall, kvartal "
                            + "FROM sykefravar_statistikk_land "
                            + "WHERE (arstall = :arstall and kvartal >= :kvartal) "
                            + "  or (arstall > :arstall) +) "
                            + "ORDER BY arstall, kvartal ",
                      new MapSqlParameterSource()
                            .addValue("arstall", fra.getÅrstall())
                            .addValue("kvartal", fra.getKvartal()),
                      (rs, rowNum) -> mapTilUmaskertSykefraværForEttKvartal(rs)));
            } catch (EmptyResultDataAccessException e) {
                return emptyList();
            }
        } catch (EmptyResultDataAccessException e) {
            return emptyList();
        }
    }

    public Historikkdata hentHistorikkAlleKategorier(
          Virksomhet virksomhet, ÅrstallOgKvartal fraÅrstallOgKvartal) {
        Næring næringen = new Næring(virksomhet.getNæringskode().getKode(), "");
        Optional<Bransje> bransjen = new Bransjeprogram().finnBransje(virksomhet.getNæringskode());

        return new Historikkdata(Map.of(
              VIRKSOMHET, hentUmaskertSykefravær(virksomhet, fraÅrstallOgKvartal),
              LAND, hentUmaskertSykefraværForNorge(fraÅrstallOgKvartal),
              NÆRING, hentUmaskertSykefravær(næringen, fraÅrstallOgKvartal),
              BRANSJE,
              bransjen.isPresent() && bransjen.get().erDefinertPåFemsiffernivå()
                    ? hentUmaskertSykefravær(bransjen.get(), fraÅrstallOgKvartal)
                    : emptyList()));
    }


    private List<UmaskertSykefraværForEttKvartal> sorterKronologisk(List<UmaskertSykefraværForEttKvartal> statistikk) {
        return statistikk.stream()
              .sorted(Comparator.comparing(UmaskertSykefraværForEttKvartal::getÅrstallOgKvartal))
              .collect(Collectors.toList());
    }

    private UmaskertSykefraværForEttKvartal mapTilUmaskertSykefraværForEttKvartal(ResultSet rs)
          throws SQLException {
        return new UmaskertSykefraværForEttKvartal(
              new ÅrstallOgKvartal(rs.getInt("arstall"), rs.getInt("kvartal")),
              rs.getBigDecimal("tapte_dagsverk"),
              rs.getBigDecimal("mulige_dagsverk"),
              rs.getInt("antall_personer"));
    }
}
