package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.Statistikkilde;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæringMedVarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhetMedGradering;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.StatistikkRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartalMedVarighet;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.*;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.parametreForStatistikk;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAllStatistikkFraDatabase;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.StatistikkRepository.INSERT_BATCH_STØRRELSE;
import static org.assertj.core.api.Java6Assertions.assertThat;

@ActiveProfiles("db-test")
@RunWith(SpringRunner.class)
@DataJdbcTest
public class StatistikkRepositoryJdbcTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private StatistikkRepository statistikkRepository;

    @Before
    public void setUp() {
        statistikkRepository = new StatistikkRepository(jdbcTemplate);
        slettAllStatistikkFraDatabase(jdbcTemplate);
    }

    @After
    public void tearDown() {
        slettAllStatistikkFraDatabase(jdbcTemplate);
    }


    @Test
    public void hentSisteÅrstallOgKvartalForSykefraværsstatistikk__skal_returnere_siste_ÅrstallOgKvartal_for_import() {
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametreForStatistikk(2019, 2, 10, 4, 100)
        );
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametreForStatistikk(2019, 1, 10, 5, 100)
        );

        ÅrstallOgKvartal årstallOgKvartal = statistikkRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(Statistikkilde.LAND);
        assertThat(årstallOgKvartal).isEqualTo(new ÅrstallOgKvartal(2019, 2));
    }


    @Test
    public void batchOpprettSykefraværsstatistikkNæringMedVarighet__skal_lagre_data_i_tabellen() {
        List<SykefraværsstatistikkNæringMedVarighet> list = new ArrayList<>();
        SykefraværsstatistikkNæringMedVarighet statistikkMedVarighet = new SykefraværsstatistikkNæringMedVarighet(
                2019,
                1,
                "03123",
                "A",
                14,
                new BigDecimal("55.123"),
                new BigDecimal("856.891")
        );

        list.add(statistikkMedVarighet);

        statistikkRepository.batchOpprettSykefraværsstatistikkNæringMedVarighet(list, INSERT_BATCH_STØRRELSE);

        List<UmaskertSykefraværForEttKvartalMedVarighet> resultList = hentSykefraværprosentNæringMedVarighet();
        Assertions.assertThat(resultList.size()).isEqualTo(1);
        Assertions.assertThat(resultList.get(0)).isEqualTo(
                new UmaskertSykefraværForEttKvartalMedVarighet(
                        new ÅrstallOgKvartal(2019, 1),
                        new BigDecimal("55.123"),
                        new BigDecimal("856.891"),
                        14,
                        Varighetskategori._1_DAG_TIL_7_DAGER
                )
        );
    }

    @Test
    public void batchOpprettSykefraværsstatistikkVirksomhetMedGradering__skal_lagre_data_i_tabellen() {
        List<SykefraværsstatistikkVirksomhetMedGradering> list = new ArrayList<>();
        SykefraværsstatistikkVirksomhetMedGradering gradertSykemelding = new SykefraværsstatistikkVirksomhetMedGradering(
                2020,
                3,
                ORGNR_VIRKSOMHET_1,
                NÆRINGSKODE_2SIFFER,
                NÆRINGSKODE_5SIFFER,
                1,
                new BigDecimal(3).setScale(6),
                3,
                13,
                new BigDecimal(16).setScale(6),
                new BigDecimal(100).setScale(6)
        );

        list.add(gradertSykemelding);

        statistikkRepository.batchOpprettSykefraværsstatistikkVirksomhetMedGradering(list, INSERT_BATCH_STØRRELSE);

        List<UmaskertSykefraværForEttKvartal> resultList = hentSykefraværprosentMedGradering();
        Assertions.assertThat(resultList.size()).isEqualTo(1);
        Assertions.assertThat(resultList.get(0)).isEqualTo(
                new UmaskertSykefraværForEttKvartal(
                        new ÅrstallOgKvartal(2020, 3),
                        new BigDecimal("3"),
                        new BigDecimal("100"),
                        13
                )
        );
    }

    @Test
    public void slettSykefraværsstatistikkNæringMedVarighet__skal_slette_data_i_tabellen() {
        lagreSykefraværprosentNæringMedVarighet("01", "A", 2018, 3);
        lagreSykefraværprosentNæringMedVarighet("02", "A", 2018, 3);
        lagreSykefraværprosentNæringMedVarighet("01", "A", 2018, 4);
        lagreSykefraværprosentNæringMedVarighet("02", "A", 2018, 4);
        lagreSykefraværprosentNæringMedVarighet("01", "A", 2019, 1);
        lagreSykefraværprosentNæringMedVarighet("02", "A", 2019, 1);

        int antallSlettet = statistikkRepository.slettSykefraværsstatistikkNæringMedVarighet(
                new ÅrstallOgKvartal(2019, 1)
        );
        List<UmaskertSykefraværForEttKvartalMedVarighet> list = hentSykefraværprosentNæringMedVarighet();
        Assertions.assertThat(list.size()).isEqualTo(4);
        Assertions.assertThat(antallSlettet).isEqualTo(2);

    }

    private void lagreSykefraværprosentNæringMedVarighet(
            String næringkode,
            String varighet,
            int årstall,
            int kvartal
    ) {
        jdbcTemplate.update(
                String.format(
                        "insert into sykefravar_statistikk_naring_med_varighet " +
                                "(arstall, kvartal, naring_kode, varighet, antall_personer, tapte_dagsverk, mulige_dagsverk) " +
                                "values (%d, %d, '%s', '%s', 15, 30, 300)",
                        årstall,
                        kvartal,
                        næringkode,
                        varighet
                ),
                new MapSqlParameterSource()
        );
    }

    private List<UmaskertSykefraværForEttKvartalMedVarighet> hentSykefraværprosentNæringMedVarighet() {
        return jdbcTemplate.query(
                "select * from sykefravar_statistikk_naring_med_varighet",
                new MapSqlParameterSource(),
                (rs, rowNum) -> new UmaskertSykefraværForEttKvartalMedVarighet(
                        new ÅrstallOgKvartal(rs.getInt("arstall"), rs.getInt("kvartal")),
                        rs.getBigDecimal("tapte_dagsverk"),
                        rs.getBigDecimal("mulige_dagsverk"),
                        rs.getInt("antall_personer"),
                        Varighetskategori.fraKode(rs.getString("varighet"))
                )
        );
    }

    private List<UmaskertSykefraværForEttKvartal> hentSykefraværprosentMedGradering() {
        return jdbcTemplate.query(
                "select * from sykefravar_statistikk_virksomhet_med_gradering",
                new MapSqlParameterSource(),
                (rs, rowNum) -> new UmaskertSykefraværForEttKvartal(
                        new ÅrstallOgKvartal(rs.getInt("arstall"), rs.getInt("kvartal")),
                        rs.getBigDecimal("tapte_dagsverk_gradert_sykemelding"),
                        rs.getBigDecimal("mulige_dagsverk"),
                        rs.getInt("antall_personer")
                )
        );
    }


}
