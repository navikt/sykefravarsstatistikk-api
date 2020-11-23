package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Virksomhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartalMedVarighet;
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
public class VarighetRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public VarighetRepository(@Qualifier("sykefravarsstatistikkJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public List<UmaskertSykefraværForEttKvartalMedVarighet> hentSykefraværForEttKvartalMedVarighet(Næring næring) {
        String næringKode = næring.getKode();
        try {
            return namedParameterJdbcTemplate.query(
                    "select tapte_dagsverk, mulige_dagsverk, antall_personer, varighet, arstall, kvartal " +
                            " from sykefravar_statistikk_naring_med_varighet " +
                            " where " +
                            " naring_kode like :næring2siffer " +
                            " and varighet in ('A', 'B', 'C', 'D', 'E', 'F', 'X')" +
                            " order by arstall, kvartal, varighet",
                    new MapSqlParameterSource()
                            .addValue("næring2siffer", næringKode+"%"),
                    (rs, rowNum) -> mapTilKvartalsvisSykefraværMedVarighet(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    public List<UmaskertSykefraværForEttKvartalMedVarighet> hentSykefraværForEttKvartalMedVarighet(Bransje bransje) {

        try {
            return namedParameterJdbcTemplate.query(
                    "select tapte_dagsverk, mulige_dagsverk, antall_personer, varighet, arstall, kvartal " +
                            " from sykefravar_statistikk_naring_med_varighet " +
                            " where " +
                            " naring_kode in (:naringKoder) " +
                            " and varighet in ('A', 'B', 'C', 'D', 'E', 'F', 'X')" +
                            " order by arstall, kvartal, varighet",
                    new MapSqlParameterSource()
                            .addValue("naringKoder", bransje.getKoderSomSpesifisererNæringer()),
                    (rs, rowNum) -> mapTilKvartalsvisSykefraværMedVarighet(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    public List<UmaskertSykefraværForEttKvartalMedVarighet> hentSykefraværForEttKvartalMedVarighet(Virksomhet virksomhet) {
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
                    (rs, rowNum) -> mapTilKvartalsvisSykefraværMedVarighet(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    private UmaskertSykefraværForEttKvartalMedVarighet mapTilKvartalsvisSykefraværMedVarighet(ResultSet rs) throws SQLException {
        return new UmaskertSykefraværForEttKvartalMedVarighet(
                new ÅrstallOgKvartal(
                        rs.getInt("arstall"),
                        rs.getInt("kvartal")
                ),
                rs.getBigDecimal("tapte_dagsverk"),
                rs.getBigDecimal("mulige_dagsverk"),
                rs.getInt("antall_personer"),
                Varighetskategori.fraKode( rs.getString("varighet")));
    }

}
