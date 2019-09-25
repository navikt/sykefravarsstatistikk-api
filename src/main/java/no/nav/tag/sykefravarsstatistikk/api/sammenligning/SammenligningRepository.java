package no.nav.tag.sykefravarsstatistikk.api.sammenligning;

import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class SammenligningRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public SammenligningRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public Sykefraværprosent hentSykefraværprosentVirksomhet(int årstall, int kvartal, String orgnr) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("arstall", årstall)
                .addValue("kvartal", kvartal)
                .addValue("orgnr", orgnr);

        return namedParameterJdbcTemplate.queryForObject(
                "SELECT * FROM SYKEFRAVAR_STATISTIKK_VIRKSOMHET WHERE arstall = :arstall AND kvartal = :kvartal AND orgnr = :orgnr",
                namedParameters,
                (rs, rowNum) -> mapTilSykefraværprosent("Fisk og Fulg AS", rs)
        );
    }

    public Sykefraværprosent hentSykefraværprosentNæring(int årstall, int kvartal, String næring) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("arstall", årstall)
                .addValue("kvartal", kvartal)
                .addValue("naring", næring);

        return namedParameterJdbcTemplate.queryForObject(
                "SELECT * FROM SYKEFRAVAR_STATISTIKK_NARING WHERE arstall = :arstall AND kvartal = :kvartal AND naring_kode = :naring",
                namedParameters,
                (rs, rowNum) -> mapTilSykefraværprosent("Tjenester tilknyttet informasjonsteknologi", rs)
        );
    }

    public Sykefraværprosent hentSykefraværprosentSektor(int årstall, int kvartal, String sektor) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("arstall", årstall)
                .addValue("kvartal", kvartal)
                .addValue("sektor", sektor);

        return namedParameterJdbcTemplate.queryForObject(
                "SELECT * FROM SYKEFRAVAR_STATISTIKK_SEKTOR WHERE arstall = :arstall AND kvartal = :kvartal AND sektor_kode = :sektor",
                namedParameters,
                (rs, rowNum) -> mapTilSykefraværprosent("Offentlig næringsvirksomhet", rs)
        );
    }

    public Sykefraværprosent hentSykefraværprosentLand(int årstall, int kvartal) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("arstall", årstall)
                .addValue("kvartal", kvartal);

        return namedParameterJdbcTemplate.queryForObject(
                "SELECT * FROM SYKEFRAVAR_STATISTIKK_LAND where arstall = :arstall and kvartal = :kvartal",
                namedParameters,
                (rs, rowNum) -> mapTilSykefraværprosent("Norge", rs)
        );
    }

    private Sykefraværprosent mapTilSykefraværprosent(String label, ResultSet rs) throws SQLException {
        return new Sykefraværprosent(
                label,
                rs.getBigDecimal("tapte_dagsverk"),
                rs.getBigDecimal("mulige_dagsverk")
        );
    }
}
