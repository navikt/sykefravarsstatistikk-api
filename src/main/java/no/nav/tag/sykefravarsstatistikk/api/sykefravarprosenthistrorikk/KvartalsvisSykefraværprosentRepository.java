package no.nav.tag.sykefravarsstatistikk.api.sykefravarprosenthistrorikk;

import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Component
public class KvartalsvisSykefraværprosentRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public KvartalsvisSykefraværprosentRepository(@Qualifier("sykefravarsstatistikkJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;

    }

    public List<KvartalsvisSykefraværprosent> hentKvartalsvisSykefraværprosentLand() {
        try {
            return namedParameterJdbcTemplate.query(
                    "SELECT tapte_dagsverk, mulige_dagsverk, antall_personer, arstall, kvartal " +
                            "FROM SYKEFRAVAR_STATISTIKK_LAND " +
                            "ORDER BY arstall, kvartal DESC ",
                    new HashMap<>(),
                    (rs, rowNum) -> mapTilKvartalsvisSykefraværprosent(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }


    private KvartalsvisSykefraværprosent mapTilKvartalsvisSykefraværprosent(ResultSet rs) throws SQLException {
        return new KvartalsvisSykefraværprosent(
                new ÅrstallOgKvartal(
                        rs.getInt("arstall"),
                        rs.getInt("kvartal")
                ),
                new Sykefraværprosent(
                        "Norge",
                        rs.getBigDecimal("tapte_dagsverk"),
                        rs.getBigDecimal("mulige_dagsverk"),
                        rs.getInt("antall_personer")
                ));
    }

}
