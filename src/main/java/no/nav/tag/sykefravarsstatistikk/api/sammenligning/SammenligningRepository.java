package no.nav.tag.sykefravarsstatistikk.api.sammenligning;

import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Næringskode5Siffer;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class SammenligningRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public static final String ÅRSTALL = "arstall";
    public static final String KVARTAL = "kvartal";
    public static final String ORGNR = "orgnr";
    public static final String NÆRING = "naring";
    public static final String SEKTOR = "sektor";

    public SammenligningRepository(
            @Qualifier("sykefravarsstatistikkJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate
    )
    {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public Sykefraværprosent hentSykefraværprosentVirksomhet(int årstall, int kvartal, Underenhet underenhet) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue(ÅRSTALL, årstall)
                .addValue(KVARTAL, kvartal)
                .addValue(ORGNR, underenhet.getOrgnr().getVerdi());

        return queryForSykefraværsprosentOgHåndterHvisIngenResultat(
                "SELECT * FROM SYKEFRAVAR_STATISTIKK_VIRKSOMHET WHERE arstall = :arstall AND kvartal = :kvartal AND orgnr = :orgnr",
                namedParameters,
                underenhet.getNavn()
        );
    }

    public Sykefraværprosent hentSykefraværprosentNæring(int årstall, int kvartal, Næring næring) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue(ÅRSTALL, årstall)
                .addValue(KVARTAL, kvartal)
                .addValue(NÆRING, næring.getKode());


        return queryForSykefraværsprosentOgHåndterHvisIngenResultat(
                "SELECT * FROM SYKEFRAVAR_STATISTIKK_NARING WHERE arstall = :arstall AND kvartal = :kvartal AND naring_kode = :naring",
                namedParameters,
                næring.getNavn()
        );
    }

    public Sykefraværprosent hentSykefraværprosentSektor(int årstall, int kvartal, Sektor sektor) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue(ÅRSTALL, årstall)
                .addValue(KVARTAL, kvartal)
                .addValue(SEKTOR, sektor.getKode());

        return queryForSykefraværsprosentOgHåndterHvisIngenResultat(
                "SELECT * FROM SYKEFRAVAR_STATISTIKK_SEKTOR WHERE arstall = :arstall AND kvartal = :kvartal AND sektor_kode = :sektor",
                namedParameters,
                sektor.getNavn()
        );
    }

    public Sykefraværprosent hentSykefraværprosentLand(int årstall, int kvartal) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue(ÅRSTALL, årstall)
                .addValue(KVARTAL, kvartal);

        return queryForSykefraværsprosentOgHåndterHvisIngenResultat(
                "SELECT * FROM SYKEFRAVAR_STATISTIKK_LAND where arstall = :arstall and kvartal = :kvartal",
                namedParameters,
                "Norge"
        );
    }

    private Sykefraværprosent queryForSykefraværsprosentOgHåndterHvisIngenResultat(
            String sql,
            SqlParameterSource namedParameters,
            String sykefraværsprosentLabel
    ) {
        try {
            return namedParameterJdbcTemplate.queryForObject(
                    sql,
                    namedParameters,
                    (rs, rowNum) -> mapTilSykefraværprosent(sykefraværsprosentLabel, rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private Sykefraværprosent mapTilSykefraværprosent(String label, ResultSet rs) throws SQLException {
        return new Sykefraværprosent(
                label,
                rs.getBigDecimal("tapte_dagsverk"),
                rs.getBigDecimal("mulige_dagsverk"),
                rs.getInt("antall_personer")
        );
    }
}
