package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import static java.util.Collections.emptyList;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.BRANSJE;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.LAND;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.NÆRING;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.VIRKSOMHET;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Virksomhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjeprogram;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.Sykefraværsdata;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

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
            throw (new IllegalArgumentException(
                  "Denne metoden funker bare for 5-siffer næringskoder"));
        }

        try {
            return namedParameterJdbcTemplate.query(
                  "SELECT sum(tapte_dagsverk) as tapte_dagsverk, sum(mulige_dagsverk) as "
                        + "mulige_dagsverk, sum(antall_personer) as antall_personer, arstall, "
                        + "kvartal "
                        +
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
                        .addValue("naringKode", næring.getKode().substring(0, 2))
                        .addValue("arstall", fraÅrstallOgKvartal.getÅrstall())
                        .addValue("kvartal", fraÅrstallOgKvartal.getKvartal()),
                  (rs, rowNum) -> mapTilUmaskertSykefraværForEttKvartal(rs)));
        } catch (EmptyResultDataAccessException e) {
            return emptyList();
        }
    }

    public List<UmaskertSykefraværForEttKvartal> hentUmaskertSykefraværForNorge(
          ÅrstallOgKvartal fra) {
        try {
            try {
                return sorterKronologisk(namedParameterJdbcTemplate.query(
                      "SELECT tapte_dagsverk, mulige_dagsverk, antall_personer, arstall, kvartal "
                            + "FROM sykefravar_statistikk_land "
                            + "WHERE (arstall = :arstall and kvartal >= :kvartal) "
                            + "  or (arstall > :arstall) "
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

    public Sykefraværsdata hentUmaskertSykefraværAlleKategorier(
          Virksomhet virksomhet,
          ÅrstallOgKvartal fraÅrstallOgKvartal) {
        Næring næring = new Næring(virksomhet.getNæringskode().getKode(), "");
        Optional<Bransje> maybeBransje
              = new Bransjeprogram().finnBransje(virksomhet.getNæringskode());

        Map<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> data = new HashMap<>();
        data.put(VIRKSOMHET,
              hentUmaskertSykefravær(virksomhet, fraÅrstallOgKvartal));
        data.put(LAND, hentUmaskertSykefraværForNorge(fraÅrstallOgKvartal));
        data.put(NÆRING, hentUmaskertSykefravær(næring, fraÅrstallOgKvartal));

        if (maybeBransje.isPresent() && maybeBransje.get().erDefinertPåFemsiffernivå()) {
            data.put(BRANSJE, hentUmaskertSykefravær(maybeBransje.get(), fraÅrstallOgKvartal));
        }

        return new Sykefraværsdata(data);
    }


    private List<UmaskertSykefraværForEttKvartal> sorterKronologisk(
          List<UmaskertSykefraværForEttKvartal> statistikk) {
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
