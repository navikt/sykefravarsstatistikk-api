package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartalMedGradering;
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
import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAllStatistikkFraDatabase;
import static org.assertj.core.api.Java6Assertions.assertThat;

@ActiveProfiles("db-test")
@RunWith(SpringRunner.class)
@DataJdbcTest
public class GraderingRepositoryJdbcTest {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private GraderingRepository graderingRepository;

    @Before
    public void setUp() {
        graderingRepository = new GraderingRepository(jdbcTemplate);
        slettAllStatistikkFraDatabase(jdbcTemplate);
    }

    @After
    public void tearDown() {
        slettAllStatistikkFraDatabase(jdbcTemplate);
    }

    @Test
    public void hentSykefraværForEttKvartalMedGradering__skal_returnere_riktig_sykefravær() {
        Underenhet underenhet = Underenhet.builder()
                .orgnr(new Orgnr("999999999"))
                .navn("bedrift med 2 næringskoder")
                .næringskode(new Næringskode5Siffer("14100", "tilfeldigNæringskode"))
                .antallAnsatte(10)
                .overordnetEnhetOrgnr(new Orgnr("1111111111")).build();
        insertDataMedGradering(
                jdbcTemplate,
                underenhet,
                underenhet.getNæringskode().getKode(),
                2019,
                4,
                5,
                9,
                7,
                new BigDecimal(10),
                new BigDecimal(20),
                new BigDecimal(100)
        );
        insertDataMedGradering(
                jdbcTemplate,
                underenhet,
                "14222",
                2019,
                4,
                2,
                9,
                7,
                new BigDecimal(12),
                new BigDecimal(20),
                new BigDecimal(100)
        );
        insertDataMedGradering(
                jdbcTemplate,
                underenhet,
                "14222",
                2020,
                1,
                19,
                30,
                15,
                new BigDecimal(25),
                new BigDecimal(50),
                new BigDecimal(300)
        );

        List<UmaskertSykefraværForEttKvartalMedGradering> resultat = graderingRepository.hentSykefraværForEttKvartalMedGradering(underenhet);

        assertThat(resultat.size()).isEqualTo(2);
        assertThat(resultat.get(0)).isEqualTo(
                new UmaskertSykefraværForEttKvartalMedGradering(
                        new ÅrstallOgKvartal(2019, 4),
                        7,
                        new BigDecimal(22),
                        18,
                        new BigDecimal(40),
                        new BigDecimal(200),
                        14
                )
        );
        assertThat(resultat.get(1)).isEqualTo(
                new UmaskertSykefraværForEttKvartalMedGradering(
                        new ÅrstallOgKvartal(2020, 1),
                        19,
                        new BigDecimal(25),
                        30,
                        new BigDecimal(50),
                        new BigDecimal(300),
                        15
                )
        );
    }

    @Test
    public void hentSykefraværForEttKvartalMedGradering__skal_returnere_riktig_underenhet_sykefravær() {
        Underenhet underenhet1 = Underenhet.builder()
                .orgnr(new Orgnr("999999999"))
                .navn("bedrift med 2 næringskoder")
                .næringskode(new Næringskode5Siffer("14100", "tilfeldigNæringskode"))
                .antallAnsatte(10)
                .overordnetEnhetOrgnr(new Orgnr("1111111111")).build();
        Underenhet underenhet2 = Underenhet.builder()
                .orgnr(new Orgnr("888888888"))
                .navn("bedrift med 2 næringskoder")
                .næringskode(new Næringskode5Siffer("15100", "andre_næringskode"))
                .antallAnsatte(10)
                .overordnetEnhetOrgnr(new Orgnr("1111111111")).build();
        insertDataMedGradering(
                jdbcTemplate,
                underenhet1,
                underenhet1.getNæringskode().getKode(),
                2019,
                4,
                5,
                9,
                7,
                new BigDecimal(10),
                new BigDecimal(20),
                new BigDecimal(100)
        );
        insertDataMedGradering(
                jdbcTemplate,
                underenhet1,
                "14222",
                2020,
                1,
                2,
                9,
                7,
                new BigDecimal(12),
                new BigDecimal(20),
                new BigDecimal(100)
        );
        insertDataMedGradering(
                jdbcTemplate,
                underenhet2,
                "14222",
                2020,
                1,
                19,
                30,
                15,
                new BigDecimal(25),
                new BigDecimal(50),
                new BigDecimal(300)
        );

        List<UmaskertSykefraværForEttKvartalMedGradering> resultat = graderingRepository.hentSykefraværForEttKvartalMedGradering(underenhet1);

        assertThat(resultat.size()).isEqualTo(2);
        assertThat(resultat.get(0)).isEqualTo(
                new UmaskertSykefraværForEttKvartalMedGradering(
                        new ÅrstallOgKvartal(2019, 4),
                        5,
                        new BigDecimal(10),
                        9,
                        new BigDecimal(20),
                        new BigDecimal(100),
                        7
                )
        );
        assertThat(resultat.get(1)).isEqualTo(
                new UmaskertSykefraværForEttKvartalMedGradering(
                        new ÅrstallOgKvartal(2020, 1),
                        2,
                        new BigDecimal(12),
                        9,
                        new BigDecimal(20),
                        new BigDecimal(100),
                        7
                )
        );
    }


    private void insertDataMedGradering(
            NamedParameterJdbcTemplate jdbcTemplate,
            Underenhet underenhet,
            String næringskode,
            int årstall,
            int kvartal,
            int antallGraderteSykemeldinger,
            int antallSykemeldinger,
            int antallPersoner,
            BigDecimal tapteDagsverkGradertSykemelding,
            BigDecimal tapteDagsverk,
            BigDecimal muligeDagsverk
    ) {
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_virksomhet_med_gradering (" +
                        "orgnr, " +
                        "naring, " +
                        "naring_kode, " +
                        "arstall, " +
                        "kvartal," +
                        "antall_graderte_sykemeldinger, " +
                        "tapte_dagsverk_gradert_sykemelding, " +
                        "antall_sykemeldinger, " +
                        "antall_personer, " +
                        "tapte_dagsverk, " +
                        "mulige_dagsverk) "
                        + "VALUES (" +
                        ":orgnr, " +
                        ":naring, " +
                        ":naring_kode, " +
                        ":arstall, " +
                        ":kvartal, " +
                        ":antall_graderte_sykemeldinger, " +
                        ":tapte_dagsverk_gradert_sykemelding, " +
                        ":antall_sykemeldinger , " +
                        ":antall_personer, " +
                        ":tapte_dagsverk, " +
                        ":mulige_dagsverk)",
                parametre(
                        underenhet.getOrgnr().getVerdi(),
                        "14",
                        næringskode,
                        årstall,
                        kvartal,
                        antallGraderteSykemeldinger,
                        tapteDagsverkGradertSykemelding,
                        antallSykemeldinger,
                        antallPersoner,
                        tapteDagsverk,
                        muligeDagsverk
                )
        );
    }

    private MapSqlParameterSource parametre(
            String orgnr,
            String naring,
            String næringskode,
            int årstall,
            int kvartal,
            int antallGraderteSykemeldinger,
            BigDecimal tapteDagsverkGradertSykemelding,
            int antallSykemeldinger,
            int antallPersoner,
            BigDecimal tapteDagsverk,
            BigDecimal muligeDagsverk
    ) {
        return new MapSqlParameterSource()
                .addValue("orgnr", orgnr)
                .addValue("naring", naring)
                .addValue("naring_kode", næringskode)
                .addValue("arstall", årstall)
                .addValue("kvartal", kvartal)
                .addValue("antall_graderte_sykemeldinger", antallGraderteSykemeldinger)
                .addValue("tapte_dagsverk_gradert_sykemelding", tapteDagsverkGradertSykemelding)
                .addValue("antall_sykemeldinger", antallSykemeldinger)
                .addValue("antall_personer", antallPersoner)
                .addValue("tapte_dagsverk", tapteDagsverk)
                .addValue("mulige_dagsverk", muligeDagsverk);
    }
}
