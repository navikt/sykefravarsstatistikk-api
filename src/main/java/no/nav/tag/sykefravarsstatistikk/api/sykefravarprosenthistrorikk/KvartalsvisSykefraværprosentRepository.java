package no.nav.tag.sykefravarsstatistikk.api.sykefravarprosenthistrorikk;

import no.nav.tag.sykefravarsstatistikk.api.domene.bransjeprogram.Bransje;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
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
public class KvartalsvisSykefraværprosentRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public KvartalsvisSykefraværprosentRepository(@Qualifier("sykefravarsstatistikkJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;

    }

    public List<KvartalsvisSykefraværprosent> hentKvartalsvisSykefraværprosentLand(String label) {
        try {
            List<KvartalsvisSykefraværprosent> resultat = namedParameterJdbcTemplate.query(
                    "SELECT tapte_dagsverk, mulige_dagsverk, antall_personer, arstall, kvartal " +
                            "FROM SYKEFRAVAR_STATISTIKK_LAND " +
                            "ORDER BY arstall, kvartal DESC ",
                    new HashMap<>(),
                    (rs, rowNum) -> mapTilKvartalsvisSykefraværprosent(rs, label)
            );
            Collections.sort(resultat);
            return resultat;
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    public List<KvartalsvisSykefraværprosent> hentKvartalsvisSykefraværprosentSektor(Sektor sektor) {
        try {
            List<KvartalsvisSykefraværprosent> resultat = namedParameterJdbcTemplate.query(
                    "SELECT tapte_dagsverk, mulige_dagsverk, antall_personer, arstall, kvartal " +
                            "FROM sykefravar_statistikk_sektor " +
                            "where sektor_kode = :sektorKode " +
                            "ORDER BY arstall, kvartal DESC ",
                    new MapSqlParameterSource()
                            .addValue("sektorKode", sektor.getKode()),
                    (rs, rowNum) -> mapTilKvartalsvisSykefraværprosent(rs, sektor.getNavn())
            );
            Collections.sort(resultat);
            return resultat;
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    public List<KvartalsvisSykefraværprosent> hentKvartalsvisSykefraværprosentNæring(Næring næring) {
        try {
            List<KvartalsvisSykefraværprosent> resultat = namedParameterJdbcTemplate.query(
                    "SELECT tapte_dagsverk, mulige_dagsverk, antall_personer, arstall, kvartal " +
                            "FROM sykefravar_statistikk_naring " +
                            "where naring_kode = :naringKode " +
                            "ORDER BY arstall, kvartal DESC ",
                    new MapSqlParameterSource()
                            .addValue("naringKode", næring.getKode()),
                    (rs, rowNum) -> mapTilKvartalsvisSykefraværprosent(rs, næring.getNavn())
            );
            Collections.sort(resultat);
            return resultat;
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    public List<KvartalsvisSykefraværprosent> hentKvartalsvisSykefraværprosentBransje(Bransje bransje) {
        try {
            List<KvartalsvisSykefraværprosent> resultat = namedParameterJdbcTemplate.query(
                    "SELECT sum(tapte_dagsverk) as tapte_dagsverk, sum(mulige_dagsverk) as mulige_dagsverk, sum(antall_personer) as antall_personer, arstall, kvartal " +
                            "FROM sykefravar_statistikk_naring5siffer " +
                            "where naring_kode in (:naringKoder) " +
                            "group by arstall, kvartal " +
                            "ORDER BY (arstall, kvartal) DESC ",
                    new MapSqlParameterSource()
                            .addValue("naringKoder", bransje.getKoderSomSpesifisererNæringer()),
                    (rs, rowNum) -> mapTilKvartalsvisSykefraværprosent(rs, bransje.getNavn())
            );
            Collections.sort(resultat);
            return resultat;
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }


    private KvartalsvisSykefraværprosent mapTilKvartalsvisSykefraværprosent(ResultSet rs, String label) throws SQLException {
        return new KvartalsvisSykefraværprosent(
                new ÅrstallOgKvartal(
                        rs.getInt("arstall"),
                        rs.getInt("kvartal")
                ),
                new Sykefraværprosent(
                        label,
                        rs.getBigDecimal("tapte_dagsverk"),
                        rs.getBigDecimal("mulige_dagsverk"),
                        rs.getInt("antall_personer")
                ));
    }
}
