package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SykefraværsstatistikkTilEksporteringRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public SykefraværsstatistikkTilEksporteringRepository(@Qualifier("sykefravarsstatistikkJdbcTemplate")
                                                                  NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }


    public SykefraværsstatistikkLand hentSykefraværprosentLand(
            ÅrstallOgKvartal årstallOgKvartal) {
        try {
            List<SykefraværsstatistikkLand> resultat = namedParameterJdbcTemplate.query(
                    "select arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk " +
                            "from sykefravar_statistikk_land " +
                            "where arstall = :arstall and kvartal = :kvartal order by arstall, kvartal",
                    new MapSqlParameterSource()
                            .addValue("arstall", årstallOgKvartal.getÅrstall())
                            .addValue("kvartal", årstallOgKvartal.getKvartal()),
                    (rs, rowNum) -> mapTilSykefraværsstatistikkLand(rs)
            );

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
                    "select sektor_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk " +
                            "from sykefravar_statistikk_sektor " +
                            "where arstall = :arstall and kvartal = :kvartal " +
                            "order by arstall, kvartal, sektor_kode",
                    new MapSqlParameterSource()
                            .addValue("arstall", årstallOgKvartal.getÅrstall())
                            .addValue("kvartal", årstallOgKvartal.getKvartal()),
                    (rs, rowNum) -> mapTilSykefraværsstatistikkSektor(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    /* Sykefraværsprosent Næring */

    public List<SykefraværsstatistikkNæring> hentSykefraværprosentAlleNæringer(
            ÅrstallOgKvartal sisteÅrstallOgKvartal,
            int antallKvartaler
    ) {
        if (antallKvartaler < 2) {
            return hentSykefraværprosentAlleNæringer(sisteÅrstallOgKvartal);
        }

        return hentSykefraværprosentAlleNæringer(
                sisteÅrstallOgKvartal,
                sisteÅrstallOgKvartal.minusKvartaler(antallKvartaler - 1)
        );
    }

    public List<SykefraværsstatistikkNæring> hentSykefraværprosentAlleNæringer(
            ÅrstallOgKvartal årstallOgKvartal
    ) {
        return hentSykefraværprosentAlleNæringer(årstallOgKvartal, årstallOgKvartal);
    }

    public List<SykefraværsstatistikkNæring> hentSykefraværprosentAlleNæringer(
            ÅrstallOgKvartal fraÅrstallOgKvartal,
            ÅrstallOgKvartal tilÅrstallOgKvartal
    ) {

        List<ÅrstallOgKvartal> årstallOgKvartalListe = ÅrstallOgKvartal.range(fraÅrstallOgKvartal, tilÅrstallOgKvartal);
        String whereClause = årstallOgKvartalListe
                .stream()
                .map(årstallOgKvartal -> String.format(
                        "(arstall = %d and kvartal = %d) ",
                        årstallOgKvartal.getÅrstall(),
                        årstallOgKvartal.getKvartal()
                ))
                .collect(Collectors.joining("or "));

        try {
            return namedParameterJdbcTemplate.query(
                    "select naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk " +
                            "from sykefravar_statistikk_naring " +
                            " where " + whereClause +
                            "order by (arstall, kvartal) desc, naring_kode",
                    (rs, rowNum) -> mapTilSykefraværsstatistikkNæring(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }


    public List<SykefraværsstatistikkNæring5Siffer> hentSykefraværprosentAlleNæringer5Siffer(ÅrstallOgKvartal årstallOgKvartal){
        return hentSykefraværprosentAlleNæringer5Siffer(årstallOgKvartal,årstallOgKvartal);
    }
    public List<SykefraværsstatistikkNæring5Siffer> hentSykefraværprosentAlleNæringer5Siffer(
            ÅrstallOgKvartal fraÅrstallOgKvartal, ÅrstallOgKvartal tilÅrstallOgKvartal) {
        List<ÅrstallOgKvartal> årstallOgKvartalListe = ÅrstallOgKvartal.range(fraÅrstallOgKvartal, tilÅrstallOgKvartal);
        String whereClause = årstallOgKvartalListe
              .stream()
              .map(årstallOgKvartal -> String.format(
                    "(arstall = %d and kvartal = %d) ",
                    årstallOgKvartal.getÅrstall(),
                    årstallOgKvartal.getKvartal()
              ))
              .collect(Collectors.joining("or "));

        try {
            return namedParameterJdbcTemplate.query(
                    "select naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk " +
                            "from sykefravar_statistikk_naring5siffer " +
                            " where " + whereClause +
                            "order by (arstall, kvartal) desc, naring_kode",
                    new MapSqlParameterSource()
                            .addValue("arstall", fraÅrstallOgKvartal.getÅrstall())
                            .addValue("kvartal", fraÅrstallOgKvartal.getKvartal()),
                    (rs, rowNum) -> mapTilSykefraværsstatistikkNæring5Siffer(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    public List<SykefraværsstatistikkVirksomhetUtenVarighet> hentSykefraværprosentAlleVirksomheter(
            ÅrstallOgKvartal fraÅrstallOgKvartal) {
        try {
            return namedParameterJdbcTemplate.query(
                    "select arstall, kvartal, orgnr, " +
                            "sum(tapte_dagsverk) as tapte_dagsverk, " +
                            "sum(mulige_dagsverk) as mulige_dagsverk, " +
                            "sum(antall_personer) as antall_personer " +
                            "from sykefravar_statistikk_virksomhet " +
                            " where " +
                            " (arstall = :arstall and kvartal >= :kvartal) " +
                            "  or " +
                            " (arstall > :arstall) " +
                            "group by arstall, kvartal, orgnr",
                    new MapSqlParameterSource()
                            .addValue("arstall", fraÅrstallOgKvartal.getÅrstall())
                            .addValue("kvartal", fraÅrstallOgKvartal.getKvartal()),
                    (rs, rowNum) -> mapTilSykefraværsstatistikkVirksomhet(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }


    // Utilities
    private SykefraværsstatistikkLand mapTilSykefraværsstatistikkLand(ResultSet rs) throws SQLException {
        return new SykefraværsstatistikkLand(
                rs.getInt("arstall"),
                rs.getInt("kvartal"),
                rs.getInt("antall_personer"),
                rs.getBigDecimal("tapte_dagsverk"),
                rs.getBigDecimal("mulige_dagsverk")
        );
    }

    private SykefraværsstatistikkSektor mapTilSykefraværsstatistikkSektor(ResultSet rs) throws SQLException {
        return new SykefraværsstatistikkSektor(
                rs.getInt("arstall"),
                rs.getInt("kvartal"),
                rs.getString("sektor_kode"),
                rs.getInt("antall_personer"),
                rs.getBigDecimal("tapte_dagsverk"),
                rs.getBigDecimal("mulige_dagsverk")
        );
    }

    private SykefraværsstatistikkNæring mapTilSykefraværsstatistikkNæring(ResultSet rs) throws SQLException {
        return new SykefraværsstatistikkNæring(
                rs.getInt("arstall"),
                rs.getInt("kvartal"),
                rs.getString("naring_kode"),
                rs.getInt("antall_personer"),
                rs.getBigDecimal("tapte_dagsverk"),
                rs.getBigDecimal("mulige_dagsverk")
        );
    }

    private SykefraværsstatistikkNæring5Siffer mapTilSykefraværsstatistikkNæring5Siffer(
            ResultSet rs
    ) throws SQLException {
        return new SykefraværsstatistikkNæring5Siffer(
                rs.getInt("arstall"),
                rs.getInt("kvartal"),
                rs.getString("naring_kode"),
                rs.getInt("antall_personer"),
                rs.getBigDecimal("tapte_dagsverk"),
                rs.getBigDecimal("mulige_dagsverk")
        );
    }

    private SykefraværsstatistikkVirksomhetUtenVarighet mapTilSykefraværsstatistikkVirksomhet(
            ResultSet rs
    ) throws SQLException {
        return new SykefraværsstatistikkVirksomhetUtenVarighet(
                rs.getInt("arstall"),
                rs.getInt("kvartal"),
                rs.getString("orgnr"),
                rs.getInt("antall_personer"),
                rs.getBigDecimal("tapte_dagsverk"),
                rs.getBigDecimal("mulige_dagsverk")
        );
    }
}
