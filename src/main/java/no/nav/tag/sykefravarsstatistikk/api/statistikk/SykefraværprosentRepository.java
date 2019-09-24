package no.nav.tag.sykefravarsstatistikk.api.statistikk;

import lombok.SneakyThrows;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.LandStatistikk;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.Sykefraværprosent;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class SykefraværprosentRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public SykefraværprosentRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @SneakyThrows
    public LandStatistikk hentLandStatistikk(int arstall, int kvartal) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("arstall", arstall)
                .addValue("kvartal", kvartal);

        LandStatistikk landStatistikk = namedParameterJdbcTemplate.queryForObject(
                "SELECT * FROM SYKEFRAVAR_STATISTIKK_LAND where arstall = :arstall and kvartal = :kvartal",
                namedParameters,
                new LandStatistikkRowMapper());
        return landStatistikk;
    }


    // TODO Generaliser disse metodene?
    public Sykefraværprosent hentSykefraværprosentLand(int årstall, int kvartal) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("arstall", årstall)
                .addValue("kvartal", kvartal);

        return namedParameterJdbcTemplate.queryForObject(
                "SELECT * FROM SYKEFRAVAR_STATISTIKK_LAND where arstall = :arstall and kvartal = :kvartal",
                namedParameters,
                (rs, rowNum) -> mapTilSykefraværprosent("Norge", rs, rowNum)
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
                (rs, rowNum) -> mapTilSykefraværprosent("Offentlig næringsvirksomhet", rs, rowNum)
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
                (rs, rowNum) -> mapTilSykefraværprosent("Tjenester tilknyttet informasjonsteknologi", rs, rowNum)
        );
    }

    public Sykefraværprosent hentSykefraværprosentVirksomhet(int årstall, int kvartal, String orgnr) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("arstall", årstall)
                .addValue("kvartal", kvartal)
                .addValue("orgnr", orgnr);

        return namedParameterJdbcTemplate.queryForObject(
                "SELECT * FROM SYKEFRAVAR_STATISTIKK_VIRKSOMHET WHERE arstall = :arstall AND kvartal = :kvartal AND orgnr = :orgnr",
                namedParameters,
                (rs, rowNum) -> mapTilSykefraværprosent("Fisk og Fulg AS", rs, rowNum)
        );
    }

    private Sykefraværprosent mapTilSykefraværprosent(String label, ResultSet rs, int rowNum) throws SQLException {
        return new Sykefraværprosent(
                label,
                rs.getBigDecimal("tapte_dagsverk"),
                rs.getBigDecimal("mulige_dagsverk")
        );
    }
}
