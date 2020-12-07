package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Virksomhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartalMedGradering;
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
public class GraderingRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public GraderingRepository(@Qualifier("sykefravarsstatistikkJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }



    public List<UmaskertSykefraværForEttKvartalMedGradering> hentSykefraværForEttKvartalMedGradering(Virksomhet virksomhet) {
        try {
            return namedParameterJdbcTemplate.query(
                    "select arstall, kvartal," +
                            " sum(antall_graderte_sykemeldinger) as sum_antall_graderte_sykemeldinger," +
                            " sum(tapte_dagsverk_gradert_sykemelding) as sum_tapte_dagsverk_gradert_sykemelding, " +
                            " sum(antall_sykemeldinger) as sum_antall_sykemeldinger," +
                            " sum(tapte_dagsverk) as sum_tapte_dagsverk," +
                            " sum(mulige_dagsverk) as sum_mulige_dagsverk, " +
                            " sum(antall_personer) as sum_antall_personer " +
                            " from sykefravar_statistikk_virksomhet_med_gradering " +
                            " where " +
                            " orgnr = :orgnr " +
                            " group by arstall, kvartal" +
                            " order by arstall, kvartal",
                    new MapSqlParameterSource()
                            .addValue("orgnr", virksomhet.getOrgnr().getVerdi()),
                    (rs, rowNum) -> mapTilKvartalsvisSykefraværMedGradering(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    private UmaskertSykefraværForEttKvartalMedGradering mapTilKvartalsvisSykefraværMedGradering(ResultSet rs) throws SQLException {
        return new UmaskertSykefraværForEttKvartalMedGradering(
                new ÅrstallOgKvartal(
                        rs.getInt("arstall"),
                        rs.getInt("kvartal")
                ),rs.getInt("sum_antall_graderte_sykemeldinger"),
                rs.getBigDecimal("sum_tapte_dagsverk_gradert_sykemelding"),
                rs.getInt("sum_antall_sykemeldinger"),
                rs.getBigDecimal("sum_tapte_dagsverk"),
                rs.getBigDecimal("sum_mulige_dagsverk"),
                rs.getInt("sum_antall_personer")
                );
    }

}
