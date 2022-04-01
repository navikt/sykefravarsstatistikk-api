package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Virksomhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Component
@DependsOn({"sykefravarsstatistikkJdbcTemplate"})
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

    public List<UmaskertSykefraværForEttKvartal> hentUmaskertSykefraværForEttKvartalListe(
            Bransje bransje,
            ÅrstallOgKvartal fraÅrstallOgKvartal
    ) {
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

    public List<UmaskertSykefraværForEttKvartal> hentUmaskertSykefraværForEttKvartalListe(
            Næring næring,
            ÅrstallOgKvartal fraÅrstallOgKvartal
    ) {
        try {
            return namedParameterJdbcTemplate.query(
                    "SELECT tapte_dagsverk, mulige_dagsverk, antall_personer, arstall, kvartal " +
                            "FROM sykefravar_statistikk_naring " +
                            "where naring_kode = :naringKode " +
                            "and (" +
                            "  (arstall = :arstall and kvartal >= :kvartal) " +
                            "  or " +
                            "  (arstall > :arstall)" +
                            ") " +
                            "ORDER BY arstall, kvartal ",
                    new MapSqlParameterSource()
                            .addValue("naringKode", næring.getKode())
                            .addValue("arstall", fraÅrstallOgKvartal.getÅrstall())
                            .addValue("kvartal", fraÅrstallOgKvartal.getKvartal()),
                    (rs, rowNum) -> mapTilUmaskertSykefraværForEttKvartal(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }


    private UmaskertSykefraværForEttKvartal mapTilUmaskertSykefraværForEttKvartal(ResultSet rs) throws SQLException {
        return new UmaskertSykefraværForEttKvartal(
                new ÅrstallOgKvartal(
                        rs.getInt("arstall"),
                        rs.getInt("kvartal")
                ),
                rs.getBigDecimal("tapte_dagsverk"),
                rs.getBigDecimal("mulige_dagsverk"),
                rs.getInt("antall_personer")
        );
    }
}
