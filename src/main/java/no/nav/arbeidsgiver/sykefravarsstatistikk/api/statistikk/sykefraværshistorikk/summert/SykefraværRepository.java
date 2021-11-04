package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Sektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Virksomhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
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
public class SykefraværRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public SykefraværRepository(@Qualifier("sykefravarsstatistikkJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;

    }

    public List<UmaskertSykefraværForEttKvartal> hentUmaskertSykefraværForEttKvartalListe(
            Virksomhet virksomhet,
            ÅrstallOgKvartal fraÅrstallOgKvartal
    ) {
        try {
            return namedParameterJdbcTemplate.query(
                    "SELECT sum(tapte_dagsverk) as tapte_dagsverk," +
                            "sum(mulige_dagsverk) as mulige_dagsverk," +
                            "sum(antall_personer) as antall_personer," +
                            "arstall, kvartal " +
                            "FROM sykefravar_statistikk_virksomhet " +
                            "where orgnr = :orgnr " +
                            "and (" +
                            "  (arstall = :arstall and kvartal >= :kvartal) " +
                            "  or " +
                            "  (arstall > :arstall)" +
                            ") " +
                            "GROUP BY arstall, kvartal " +
                            "ORDER BY arstall, kvartal ",
                    new MapSqlParameterSource()
                            .addValue("orgnr", virksomhet.getOrgnr().getVerdi())
                            .addValue("arstall", fraÅrstallOgKvartal.getÅrstall())
                            .addValue("kvartal", fraÅrstallOgKvartal.getKvartal()),

                    (rs, rowNum) -> mapTilUmaskertSykefraværForEttKvartal(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    /*
    public List<UmaskertSykefraværForEttKvartal> hentUmaskertSykefraværForEttKvartalListe(Næring næring) {
        try {
            return namedParameterJdbcTemplate.query(
                    "SELECT tapte_dagsverk, mulige_dagsverk, antall_personer, arstall, kvartal " +
                            "FROM sykefravar_statistikk_naring " +
                            "where naring_kode = :naringKode " +
                            "ORDER BY arstall, kvartal ",
                    new MapSqlParameterSource()
                            .addValue("naringKode", næring.getKode()),
                    (rs, rowNum) -> mapTilUmaskertSykefraværForEttKvartal(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    public List<UmaskertSykefraværForEttKvartal> hentUmaskertSykefraværForEttKvartalListe(Bransje bransje) {
        try {
            return namedParameterJdbcTemplate.query(
                    "SELECT sum(tapte_dagsverk) as tapte_dagsverk, sum(mulige_dagsverk) as mulige_dagsverk, sum(antall_personer) as antall_personer, arstall, kvartal " +
                            "FROM sykefravar_statistikk_naring5siffer " +
                            "where naring_kode in (:naringKoder) " +
                            "group by arstall, kvartal " +
                            "ORDER BY arstall, kvartal ",
                    new MapSqlParameterSource()
                            .addValue("naringKoder", bransje.getKoderSomSpesifisererNæringer()),
                    (rs, rowNum) -> mapTilUmaskertSykefraværForEttKvartal(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }*/

    private UmaskertSykefraværForEttKvartal mapTilUmaskertSykefraværForEttKvartal(ResultSet rs) throws SQLException {
        return new UmaskertSykefraværForEttKvartal(
                new ÅrstallOgKvartal(
                        rs.getInt("arstall"),
                        rs.getInt("kvartal")
                ),
                rs.getBigDecimal("sum_tapte_dagsverk_gradert_sykemelding"),
                rs.getBigDecimal("sum_mulige_dagsverk"),
                rs.getInt("sum_antall_personer")
        );
    }
}
