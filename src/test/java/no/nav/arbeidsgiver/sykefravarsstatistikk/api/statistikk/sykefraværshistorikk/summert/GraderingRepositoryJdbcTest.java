package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjetype;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAllStatistikkFraDatabase;
import static org.assertj.core.api.Java6Assertions.assertThat;

@ActiveProfiles("db-test")
@DataJdbcTest
public class GraderingRepositoryJdbcTest {

    private static final Næring PRODUKSJON_AV_KLÆR = new Næring("14", "Produksjon av klær");
    private static final Næring PRODUKSJON_AV_LÆR_OG_LÆRVARER = new Næring("15", "Produksjon av lær og lærvarer");
    private static final Næring HELSETJENESTER = new Næring("86", "Helsetjenester");
    private static Underenhet UNDERENHET_1_NÆRING_14 = Underenhet.builder()
            .orgnr(new Orgnr("999999999"))
            .næringskode(new Næringskode5Siffer("14120", "Produksjon av arbeidstøy"))
            .build();
    private static Underenhet UNDERENHET_2_NÆRING_15 = Underenhet.builder()
            .orgnr(new Orgnr("888888888"))
            .næringskode(new Næringskode5Siffer("15100", "andre_næringskode"))
            .build();
    private static Underenhet UNDERENHET_3_NÆRING_14 = Underenhet.builder()
            .orgnr(new Orgnr("777777777"))
            .næringskode(new Næringskode5Siffer("14120", "Produksjon av arbeidstøy"))
            .build();
    private static ÅrstallOgKvartal _2020_1 = new ÅrstallOgKvartal(2020, 1);
    private static ÅrstallOgKvartal _2019_4 = new ÅrstallOgKvartal(2019, 4);

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private GraderingRepository graderingRepository;

    @BeforeEach
    public void setUp() {
        graderingRepository = new GraderingRepository(jdbcTemplate);
        slettAllStatistikkFraDatabase(jdbcTemplate);
    }

    @AfterEach
    public void tearDown() {
        slettAllStatistikkFraDatabase(jdbcTemplate);
    }

    @Test
    public void hentSykefraværForEttKvartalMedGradering__skal_returnere_riktig_sykefravær() {
        insertDataMedGradering(
                jdbcTemplate,
                UNDERENHET_1_NÆRING_14.getOrgnr().getVerdi(),
                PRODUKSJON_AV_KLÆR.getKode(), "14100",
                _2019_4,
                5,
                9,
                7,
                new BigDecimal(10),
                new BigDecimal(20),
                new BigDecimal(100)
        );
        insertDataMedGradering(
                jdbcTemplate,
                UNDERENHET_1_NÆRING_14.getOrgnr().getVerdi(),
                PRODUKSJON_AV_KLÆR.getKode(), "14222",
                _2019_4,
                2,
                9,
                7,
                new BigDecimal(12),
                new BigDecimal(20),
                new BigDecimal(100)
        );
        insertDataMedGradering(
                jdbcTemplate,
                UNDERENHET_1_NÆRING_14.getOrgnr().getVerdi(),
                PRODUKSJON_AV_KLÆR.getKode(), "14222",
                _2020_1,
                19,
                30,
                15,
                new BigDecimal(25),
                new BigDecimal(50),
                new BigDecimal(300)
        );

        List<UmaskertSykefraværForEttKvartal> resultat =
                graderingRepository.hentSykefraværForEttKvartalMedGradering(UNDERENHET_1_NÆRING_14);

        assertThat(resultat.size()).isEqualTo(2);
        assertThat(resultat.get(0)).isEqualTo(
                new UmaskertSykefraværForEttKvartal(
                        new ÅrstallOgKvartal(2019, 4),
                        new BigDecimal(22),
                        new BigDecimal(200),
                        14
                )
        );
        assertThat(resultat.get(1)).isEqualTo(
                new UmaskertSykefraværForEttKvartal(
                        new ÅrstallOgKvartal(2020, 1),
                        new BigDecimal(25),
                        new BigDecimal(300),
                        15
                )
        );
    }

    @Test
    public void hentSykefraværForEttKvartalMedGradering__skal_returnere_riktig_underenhet_sykefravær() {
        insertDataMedGradering(
                jdbcTemplate,
                UNDERENHET_1_NÆRING_14.getOrgnr().getVerdi(),
                PRODUKSJON_AV_KLÆR.getKode(),
                UNDERENHET_1_NÆRING_14.getNæringskode().getKode(),
                _2019_4,
                5,
                9,
                7,
                new BigDecimal(10),
                new BigDecimal(20),
                new BigDecimal(100)
        );
        insertDataMedGradering(
                jdbcTemplate,
                UNDERENHET_1_NÆRING_14.getOrgnr().getVerdi(),
                PRODUKSJON_AV_KLÆR.getKode(), "14222",
                _2020_1,
                2,
                9,
                7,
                new BigDecimal(12),
                new BigDecimal(20),
                new BigDecimal(100)
        );
        insertDataMedGradering(
                jdbcTemplate,
                UNDERENHET_3_NÆRING_14.getOrgnr().getVerdi(),
                PRODUKSJON_AV_KLÆR.getKode(), "14222",
                _2020_1,
                19,
                30,
                15,
                new BigDecimal(25),
                new BigDecimal(50),
                new BigDecimal(300)
        );

        List<UmaskertSykefraværForEttKvartal> resultat = graderingRepository.hentSykefraværForEttKvartalMedGradering(UNDERENHET_1_NÆRING_14);

        assertThat(resultat.size()).isEqualTo(2);
        assertThat(resultat.get(0)).isEqualTo(
                new UmaskertSykefraværForEttKvartal(
                        new ÅrstallOgKvartal(2019, 4),
                        new BigDecimal(10),
                        new BigDecimal(100),
                        7
                )
        );
        assertThat(resultat.get(1)).isEqualTo(
                new UmaskertSykefraværForEttKvartal(
                        new ÅrstallOgKvartal(2020, 1),
                        new BigDecimal(12),
                        new BigDecimal(100),
                        7
                )
        );
    }


    @Test
    public void hentSykefraværForEttKvartalMedGradering__skal_returnere_riktig_sykefravær_for_næring() {

        insertDataMedGradering(
                jdbcTemplate,
                UNDERENHET_1_NÆRING_14.getOrgnr().getVerdi(),
                PRODUKSJON_AV_KLÆR.getKode(),
                UNDERENHET_1_NÆRING_14.getNæringskode().getKode(),
                _2019_4,
                5,
                9,
                7,
                new BigDecimal(10),
                new BigDecimal(20),
                new BigDecimal(100)
        );
        insertDataMedGradering(
                jdbcTemplate,
                UNDERENHET_3_NÆRING_14.getOrgnr().getVerdi(),
                PRODUKSJON_AV_KLÆR.getKode(),
                "14222",
                _2020_1,
                2,
                9,
                7,
                new BigDecimal(12),
                new BigDecimal(20),
                new BigDecimal(100)
        );
        insertDataMedGradering(
                jdbcTemplate,
                UNDERENHET_2_NÆRING_15.getOrgnr().getVerdi(),
                PRODUKSJON_AV_LÆR_OG_LÆRVARER.getKode(),
                "15333",
                _2020_1,
                19,
                30,
                15,
                new BigDecimal(25),
                new BigDecimal(50),
                new BigDecimal(300)
        );

        List<UmaskertSykefraværForEttKvartal> resultat = graderingRepository.hentSykefraværForEttKvartalMedGradering(PRODUKSJON_AV_KLÆR);

        assertThat(resultat.size()).isEqualTo(2);
        assertThat(resultat.get(0)).isEqualTo(
                new UmaskertSykefraværForEttKvartal(
                        new ÅrstallOgKvartal(2019, 4),
                        new BigDecimal(10),
                        new BigDecimal(100),
                        7
                )
        );
        assertThat(resultat.get(1)).isEqualTo(
                new UmaskertSykefraværForEttKvartal(
                        new ÅrstallOgKvartal(2020, 1),
                        new BigDecimal(12),
                        new BigDecimal(100),
                        7
                )
        );
    }

    @Test
    public void hentSykefraværForEttKvartalMedGradering__skal_returnere_riktig_sykefravær_for_bransje() {
        Næringskode5Siffer sykehus = new Næringskode5Siffer("86101", "Alminnelige somatiske sykehus");
        Næringskode5Siffer legetjeneste = new Næringskode5Siffer("86211", "Allmenn legetjeneste");
        Næringskode5Siffer næringskodeIkkeErFraBransje = new Næringskode5Siffer("86902", "Fysioterapitjeneste");

        insertDataMedGradering(
                jdbcTemplate,
                UNDERENHET_1_NÆRING_14.getOrgnr().getVerdi(),
                HELSETJENESTER.getKode(),
                sykehus.getKode(),
                _2019_4,
                5,
                9,
                7,
                new BigDecimal(10),
                new BigDecimal(20),
                new BigDecimal(100)
        );
        insertDataMedGradering(
                jdbcTemplate,
                UNDERENHET_3_NÆRING_14.getOrgnr().getVerdi(),
                HELSETJENESTER.getKode(),
                sykehus.getKode(),
                _2020_1,
                2,
                9,
                7,
                new BigDecimal(12),
                new BigDecimal(20),
                new BigDecimal(100)
        );
        insertDataMedGradering(
                jdbcTemplate,
                UNDERENHET_2_NÆRING_15.getOrgnr().getVerdi(),
                HELSETJENESTER.getKode(),
                legetjeneste.getKode(),
                _2020_1,
                19,
                30,
                15,
                new BigDecimal(25),
                new BigDecimal(50),
                new BigDecimal(300)
        );
        insertDataMedGradering(
                jdbcTemplate,
                UNDERENHET_2_NÆRING_15.getOrgnr().getVerdi(),
                HELSETJENESTER.getKode(),
                næringskodeIkkeErFraBransje.getKode(),
                _2020_1,
                35,
                1,
                4,
                new BigDecimal(55),
                new BigDecimal(66),
                new BigDecimal(3000)
        );

        List<UmaskertSykefraværForEttKvartal> resultat = graderingRepository.hentSykefraværForEttKvartalMedGradering(
                new Bransje(Bransjetype.SYKEHUS, "sykehus", "86101", "86211"));

        assertThat(resultat.size()).isEqualTo(2);
        assertThat(resultat.get(0)).isEqualTo(
                new UmaskertSykefraværForEttKvartal(
                        new ÅrstallOgKvartal(2019, 4),
                        new BigDecimal(10),
                        new BigDecimal(100),
                        7
                )
        );
        assertThat(resultat.get(1)).isEqualTo(
                new UmaskertSykefraværForEttKvartal(
                        new ÅrstallOgKvartal(2020, 1),
                        new BigDecimal(37),
                        new BigDecimal(400),
                        22
                )
        );
    }

    private void insertDataMedGradering(
            NamedParameterJdbcTemplate jdbcTemplate,
            String orgnr,
            String næring,
            String næringskode,
            ÅrstallOgKvartal årstallOgKvartal,
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
                        orgnr,
                        næring,
                        næringskode,
                        årstallOgKvartal.getÅrstall(),
                        årstallOgKvartal.getKvartal(),
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
