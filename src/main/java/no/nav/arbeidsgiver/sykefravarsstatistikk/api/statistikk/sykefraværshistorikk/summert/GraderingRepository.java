package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.NæringOgNæringskode5siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadataNæringskode5siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Virksomhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET;

@Component
public class GraderingRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public GraderingRepository(@Qualifier("sykefravarsstatistikkJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public List<VirksomhetMetadataNæringskode5siffer> hentVirksomhetMetadataNæringskode5siffer(
            ÅrstallOgKvartal årstallOgKvartal
    ) {
        try {
            return namedParameterJdbcTemplate.query(
                    "select arstall, kvartal, orgnr, naring, naring_kode" +
                            " from sykefravar_statistikk_virksomhet_med_gradering " +
                            " where " +
                            " arstall = :årstall " +
                            " and kvartal = :kvartal " +
                            " group by arstall, kvartal, orgnr, naring, naring_kode" +
                            " order by arstall, kvartal, orgnr, naring, naring_kode",
                    new MapSqlParameterSource()
                            .addValue("årstall", årstallOgKvartal.getÅrstall())
                            .addValue("kvartal", årstallOgKvartal.getKvartal()),
                    (rs, rowNum) -> new VirksomhetMetadataNæringskode5siffer(
                            new Orgnr(rs.getString("orgnr")),
                            new ÅrstallOgKvartal(
                                    rs.getInt("arstall"),
                                    rs.getInt("kvartal")
                            ),
                            new NæringOgNæringskode5siffer(rs.getString("naring"), rs.getString("naring_kode"))
                    )
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    public List<UmaskertSykefraværForEttKvartal> hentSykefraværForEttKvartalMedGradering(Virksomhet virksomhet) {
        try {
            return namedParameterJdbcTemplate.query(
                    "select arstall, kvartal," +
                            " sum(tapte_dagsverk_gradert_sykemelding) as sum_tapte_dagsverk_gradert_sykemelding, " +
                            " sum(mulige_dagsverk) as sum_mulige_dagsverk, " +
                            " sum(antall_personer) as sum_antall_personer " +
                            " from sykefravar_statistikk_virksomhet_med_gradering " +
                            " where " +
                            " orgnr = :orgnr " +
                            " and rectype = :rectype " +
                            " group by arstall, kvartal" +
                            " order by arstall, kvartal",
                    new MapSqlParameterSource()
                            .addValue("orgnr", virksomhet.getOrgnr().getVerdi())
                            .addValue("rectype", RECTYPE_FOR_VIRKSOMHET),
                    (rs, rowNum) -> mapTilUmaskertSykefraværForEttKvartal(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    public List<UmaskertSykefraværForEttKvartal> hentSykefraværForEttKvartalMedGradering(Næring næring) {
        try {
            return namedParameterJdbcTemplate.query(
                    "select arstall, kvartal," +
                            " sum(tapte_dagsverk_gradert_sykemelding) as sum_tapte_dagsverk_gradert_sykemelding, " +
                            " sum(mulige_dagsverk) as sum_mulige_dagsverk, " +
                            " sum(antall_personer) as sum_antall_personer " +
                            " from sykefravar_statistikk_virksomhet_med_gradering " +
                            " where " +
                            " naring = :naring " +
                            " and rectype = :rectype " +
                            " group by arstall, kvartal" +
                            " order by arstall, kvartal",
                    new MapSqlParameterSource()
                            .addValue("naring", næring.getKode())
                            .addValue("rectype", RECTYPE_FOR_VIRKSOMHET),
                    (rs, rowNum) -> mapTilUmaskertSykefraværForEttKvartal(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }

    }

    public List<UmaskertSykefraværForEttKvartal> hentSykefraværForEttKvartalMedGradering(Bransje bransje) {
        try {
            return namedParameterJdbcTemplate.query(
                    "select arstall, kvartal," +
                            " sum(tapte_dagsverk_gradert_sykemelding) as sum_tapte_dagsverk_gradert_sykemelding, " +
                            " sum(mulige_dagsverk) as sum_mulige_dagsverk, " +
                            " sum(antall_personer) as sum_antall_personer " +
                            " from sykefravar_statistikk_virksomhet_med_gradering " +
                            " where " +
                            " naring_kode in (:naringKoder) " +
                            " and rectype = :rectype " +
                            " group by arstall, kvartal" +
                            " order by arstall, kvartal",
                    new MapSqlParameterSource()
                            .addValue("naringKoder", bransje.getKoderSomSpesifisererNæringer())
                            .addValue("rectype", RECTYPE_FOR_VIRKSOMHET),
                    (rs, rowNum) -> mapTilUmaskertSykefraværForEttKvartal(rs)
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }


    private UmaskertSykefraværForEttKvartal mapTilUmaskertSykefraværForEttKvartal(ResultSet rs) throws SQLException {
        return new UmaskertSykefraværForEttKvartal(
                new ÅrstallOgKvartal(
                        rs.getInt("arstall"),
                        rs.getInt("kvartal")
                ),
                rs.getBigDecimal("sum_tapte_dagsverk_gradert_sykemelding"),
                rs.getBigDecimal("sum_mulige_dagsverk"),
                rs.getInt("sum_antall_personer")
        );
    }
}
