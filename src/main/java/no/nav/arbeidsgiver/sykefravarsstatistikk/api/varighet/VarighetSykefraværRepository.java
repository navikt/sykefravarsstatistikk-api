package no.nav.arbeidsgiver.sykefravarsstatistikk.api.varighet;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret.Virksomhet;
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
public class VarighetSykefraværRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public VarighetSykefraværRepository(@Qualifier("sykefravarsstatistikkJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public List<KvartalsvisSykefraværVarighet> hentSykefraværprosentMedVarighet(Virksomhet virksomhet) {
        try {
            return namedParameterJdbcTemplate.query(
                    "select tapte_dagsverk, mulige_dagsverk, antall_personer, varighet, arstall, kvartal " +
                            " from sykefravar_statistikk_virksomhet " +
                            " where " +
                            " orgnr = :orgnr " +
                            " and varighet in ('A', 'B', 'C', 'D', 'E', 'F', 'X')" +
                            " order by arstall, kvartal, varighet",
                    new MapSqlParameterSource()
                            .addValue("orgnr", virksomhet.getOrgnr().getVerdi()),
                    (rs, rowNum) -> mapTilKvartalsvisSykefraværprosentVarighet(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    private KvartalsvisSykefraværVarighet mapTilKvartalsvisSykefraværprosentVarighet(ResultSet rs) throws SQLException {
        return new KvartalsvisSykefraværVarighet(
                new ÅrstallOgKvartal(
                        rs.getInt("arstall"),
                        rs.getInt("kvartal")
                ),
                rs.getBigDecimal("tapte_dagsverk"),
                rs.getBigDecimal("mulige_dagsverk"),
                rs.getInt("antall_personer"),
                rs.getString("varighet"));
    }

}
