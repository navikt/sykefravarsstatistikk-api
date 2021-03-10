package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartalMedOrgNr;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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
                            "arstall, kvartal, orgnr " +
                            "FROM sykefravar_statistikk_virksomhet " +
                            "where arstall = :arstall and " +
                            "kvartal = :kvartal " +
                            "GROUP BY orgnr ",
                    new MapSqlParameterSource()
                            .addValue("arstall", årstallOgKvartal.getÅrstall())
                            .addValue("kvartal", årstallOgKvartal.getKvartal()),
                    (rs, rowNum) -> mapTilSykefraværprosentForEttKvartal(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
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
                rs.getInt("antall_personer")
        );
    }

}
