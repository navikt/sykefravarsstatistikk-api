package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.AmendPrimaryKeyForH2Extension;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.Statistikkilde;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæringMedVarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhetMedGradering;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.StatistikkRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartalMedVarighet;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.AssertUtils.assertBigDecimalIsEqual;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.NÆRINGSKODE_2SIFFER;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.NÆRINGSKODE_5SIFFER;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.ORGNR_VIRKSOMHET_1;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.parametreForStatistikk;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAllStatistikkFraDatabase;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.StatistikkRepository.INSERT_BATCH_STØRRELSE;
import static org.assertj.core.api.Java6Assertions.assertThat;

@ActiveProfiles("db-test")
@ExtendWith(MockitoExtension.class)
@DataJdbcTest
@ExtendWith(AmendPrimaryKeyForH2Extension.class)
public class StatistikkRepositoryJdbcTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private StatistikkRepository statistikkRepository;

    @BeforeEach
    public void setUp() {
        statistikkRepository = new StatistikkRepository(jdbcTemplate);
        slettAllStatistikkFraDatabase(jdbcTemplate);
    }

    @AfterEach
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
                RECTYPE_FOR_VIRKSOMHET,
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
    public void batchOpprettSykefraværsstatistikkVirksomhet__skal_lagre_data_i_tabellen_med_rectype() {
        List<SykefraværsstatistikkVirksomhet> list = new ArrayList<>();
        SykefraværsstatistikkVirksomhet sykefraværsstatistikkVirksomhet = new SykefraværsstatistikkVirksomhet(
                2019,
                3,
                ORGNR_VIRKSOMHET_1,
                Varighetskategori._1_DAG_TIL_7_DAGER.kode,
                RECTYPE_FOR_VIRKSOMHET,
                1,
                new BigDecimal(16).setScale(6),
                new BigDecimal(100).setScale(6)
        );

        list.add(sykefraværsstatistikkVirksomhet);

        statistikkRepository.importSykefraværsstatistikkVirksomhet(list, new ÅrstallOgKvartal(2019, 3));

        List<RawDataStatistikkVirksomhet> resultList = hentRawDataStatistikkVirksomhet();
        Assertions.assertThat(resultList.size()).isEqualTo(1);
        assertIsEquals(resultList.get(0),
                new RawDataStatistikkVirksomhet(
                        2019,
                        3,
                        ORGNR_VIRKSOMHET_1,
                        Varighetskategori._1_DAG_TIL_7_DAGER.kode,
                        RECTYPE_FOR_VIRKSOMHET,
                        new BigDecimal("16"),
                        new BigDecimal("100"),
                        1
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


    private List<RawDataStatistikkVirksomhet> hentRawDataStatistikkVirksomhet() {
        return jdbcTemplate.query(
                "select * from sykefravar_statistikk_virksomhet",
                new MapSqlParameterSource(),
                (rs, rowNum) -> new RawDataStatistikkVirksomhet(
                        rs.getInt("arstall"),
                        rs.getInt("kvartal"),
                        rs.getString("orgnr"),
                        rs.getString("varighet"),
                        rs.getString("rectype"),
                        rs.getBigDecimal("tapte_dagsverk"),
                        rs.getBigDecimal("mulige_dagsverk"),
                        rs.getInt("antall_personer")
                )
        );
    }

    private class RawDataStatistikkVirksomhet {
        int årstall;
        int kvartal;
        String orgnr;
        String varighet;
        String rectype;
        BigDecimal tapteDagsverk;
        BigDecimal muligeDagsverk;
        int antallPersoner;

        public RawDataStatistikkVirksomhet(
                int årstall,
                int kvartal,
                String orgnr,
                String varighet,
                String rectype,
                BigDecimal tapteDagsverk,
                BigDecimal muligeDagsverk,
                int antallPersoner
        ) {
            this.årstall = årstall;
            this.kvartal = kvartal;
            this.orgnr = orgnr;
            this.varighet = varighet;
            this.rectype = rectype;
            this.tapteDagsverk = tapteDagsverk;
            this.muligeDagsverk = muligeDagsverk;
            this.antallPersoner = antallPersoner;
        }
    }

    public static void assertIsEquals(RawDataStatistikkVirksomhet actual, RawDataStatistikkVirksomhet expected) {
        assertThat(actual.årstall).isEqualTo(expected.årstall);
        assertThat(actual.kvartal).isEqualTo(expected.kvartal);
        assertThat(actual.antallPersoner).isEqualTo(expected.antallPersoner);
        assertThat(actual.varighet).isEqualTo(expected.varighet);
        assertThat(actual.rectype).isEqualTo(expected.rectype);
        assertBigDecimalIsEqual(actual.muligeDagsverk, expected.muligeDagsverk);
        assertBigDecimalIsEqual(actual.tapteDagsverk, expected.tapteDagsverk);
    }

}
