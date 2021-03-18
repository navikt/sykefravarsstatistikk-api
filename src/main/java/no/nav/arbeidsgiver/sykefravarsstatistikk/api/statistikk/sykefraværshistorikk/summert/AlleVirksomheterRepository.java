package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartalMedOrgNr;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Component
public class AlleVirksomheterRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public AlleVirksomheterRepository(@Qualifier("sykefravarsstatistikkJdbcTemplate")
                                              NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public List<SykefraværForEttKvartalMedOrgNr> hentSykefraværprosentAlleVirksomheterForEttKvartal(
            ÅrstallOgKvartal årstallOgKvartal) {
        try {
            return namedParameterJdbcTemplate.query(
                    "SELECT sum(tapte_dagsverk) as tapte_dagsverk," +
                            "sum(mulige_dagsverk) as mulige_dagsverk," +
                            "sum(antall_personer) as antall_personer," +
                            "arstall, kvartal, orgnr, " +
                            "naring_kode " +
                            "FROM sykefravar_statistikk_virksomhet_med_gradering " +
                            "where arstall = :arstall and " +
                            "kvartal = :kvartal and " +
                            "er_ekportert = false " +
                            "GROUP BY arstall, kvartal, orgnr, naring_kode ",
                    new MapSqlParameterSource()
                            .addValue("arstall", årstallOgKvartal.getÅrstall())
                            .addValue("kvartal", årstallOgKvartal.getKvartal()),
                    (rs, rowNum) -> mapTilSykefraværprosentForEttKvartal(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    public int oppdaterOgSetErEksportertTilTrue(
            String tabell,
            Orgnr orgnr,
            ÅrstallOgKvartal årstallOgKvartal
    ) {
        SqlParameterSource namedParameters =
                new MapSqlParameterSource()
                        .addValue("orgnr", orgnr.getVerdi())
                        .addValue("årstall", årstallOgKvartal.getÅrstall())
                        .addValue("kvartal", årstallOgKvartal.getKvartal());

        return namedParameterJdbcTemplate.update(
                "update " + tabell + " set er_ekportert =true where orgnr = :orgnr and "+
                "arstall = :årstall and kvartal = :kvartal",
                namedParameters
        );
    }

    private SykefraværForEttKvartalMedOrgNr mapTilSykefraværprosentForEttKvartal(ResultSet rs) throws SQLException {
        return new SykefraværForEttKvartalMedOrgNr(
                new ÅrstallOgKvartal(
                        rs.getInt("arstall"),
                        rs.getInt("kvartal")
                ),
                rs.getString("orgnr"),
                rs.getBigDecimal("tapte_dagsverk"),
                rs.getBigDecimal("mulige_dagsverk"),
                rs.getInt("antall_personer"),
                rs.getString("naring_kode"));
    }

}
