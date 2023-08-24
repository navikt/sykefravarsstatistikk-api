package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.AppConfigForJdbcTesterConfig;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværsstatistikkTilEksporteringRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.AssertUtils.assertBigDecimalIsEqual;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfigForJdbcTesterConfig.class})
@DataJdbcTest(excludeAutoConfiguration = {TestDatabaseAutoConfiguration.class})
class SykefraværsstatistikkTilEksporteringRepositoryTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private SykefraværsstatistikkTilEksporteringRepository repository;

    private final Næringskode5Siffer produksjonAvKlær =
            new Næringskode5Siffer("14190", "Produksjon av klær");
    private final Næringskode5Siffer undervisning = new Næringskode5Siffer("86907", "Undervisning");
    private final Næring utdanning = new Næring("86", "Utdanning");
    private final Næring produksjon = new Næring("14", "Produksjon");
    private final String VIRKSOMHET_1 = "999999999";
    private final String VIRKSOMHET_2 = "999999998";

    @BeforeEach
    void setUp() {
        slettAllStatistikkFraDatabase(jdbcTemplate);
        repository = new SykefraværsstatistikkTilEksporteringRepository(jdbcTemplate);
    }

    @AfterEach
    void tearDown() {
        slettAllStatistikkFraDatabase(jdbcTemplate);
    }

    @Test
    void hentSykefraværprosentLand__returnerer_NULL_dersom_ingen_statistikk_er_funnet_for_kvartal() {
        opprettStatistikkLandTestData();
        assertNull(repository.hentSykefraværprosentLand(new ÅrstallOgKvartal(2019, 4)));
    }

    @Test
    void hentSykefraværprosentLand__skal_hente_sykefravær_land_for_ett_kvartal() {
        opprettStatistikkLandTestData();
        assertNull(repository.hentSykefraværprosentLand(new ÅrstallOgKvartal(2019, 4)));

        SykefraværsstatistikkLand resultat =
                repository.hentSykefraværprosentLand(new ÅrstallOgKvartal(2019, 2));
        assertSykefraværsstatistikkIsEqual(resultat, 2019, 2, 2500000, 256800, 60000000);

        SykefraværsstatistikkLand resultat_2019_1 =
                repository.hentSykefraværprosentLand(new ÅrstallOgKvartal(2019, 1));
        assertSykefraværsstatistikkIsEqual(resultat_2019_1, 2019, 1, 2750000, 350000, 71000000);
    }

    @Test
    void hentSykefraværprosentAlleSektorer__skal_hente_alle_sektorer_for_ett_kvartal() {
        opprettStatistikkSektorTestData();

        List<SykefraværsstatistikkSektor> resultat =
                repository.hentSykefraværprosentAlleSektorer(new ÅrstallOgKvartal(2019, 2));

        assertThat(resultat.size()).isEqualTo(2);
        assertSykefraværsstatistikkForSektorIsEqual(resultat, 2019, 2, 3, Sektor.KOMMUNAL, 1, 60);
        assertSykefraværsstatistikkForSektorIsEqual(resultat, 2019, 2, 4, Sektor.PRIVAT, 9, 100);

        List<SykefraværsstatistikkSektor> resultat_2019_1 =
                repository.hentSykefraværprosentAlleSektorer(new ÅrstallOgKvartal(2019, 1));

        assertThat(resultat_2019_1.size()).isEqualTo(2);
        assertSykefraværsstatistikkForSektorIsEqual(
                resultat_2019_1, 2019, 1, 40, Sektor.KOMMUNAL, 20, 115);
        assertSykefraværsstatistikkForSektorIsEqual(
                resultat_2019_1, 2019, 1, 7, Sektor.PRIVAT, 12, 100);
    }

    @Test
    void hentSykefraværprosentAlleNæringer__skal_hente_alle_næringer_for_ett_kvartal() {
        opprettStatistikkNæringTestData(new ÅrstallOgKvartal(2019, 1), new ÅrstallOgKvartal(2019, 2));

        List<SykefraværsstatistikkNæring> resultat =
                repository.hentSykefraværprosentAlleNæringer(new ÅrstallOgKvartal(2019, 2));

        assertThat(resultat.size()).isEqualTo(2);
        assertSykefraværsstatistikkForNæringIsEqual(resultat, 2019, 2, 10, produksjon, 2, 100);
        assertSykefraværsstatistikkForNæringIsEqual(resultat, 2019, 2, 8, utdanning, 5, 100);

        List<SykefraværsstatistikkNæring> resultat_2019_1 =
                repository.hentSykefraværprosentAlleNæringer(new ÅrstallOgKvartal(2019, 1));

        assertThat(resultat_2019_1.size()).isEqualTo(2);
        assertSykefraværsstatistikkForNæringIsEqual(resultat_2019_1, 2019, 1, 10, produksjon, 2, 100);
        assertSykefraværsstatistikkForNæringIsEqual(resultat_2019_1, 2019, 1, 8, utdanning, 5, 100);
    }

    @Test
    void hentSykefraværprosentAlleNæringer_siste4Kvartaler_skal_hente_riktig_data() {
        opprettStatistikkForNæringer(jdbcTemplate);
        List<SykefraværsstatistikkNæring> forventet =
                List.of(
                        new SykefraværsstatistikkNæring(
                                SISTE_PUBLISERTE_KVARTAL.getÅrstall(),
                                SISTE_PUBLISERTE_KVARTAL.getKvartal(),
                                "10",
                                50,
                                new BigDecimal(20000),
                                new BigDecimal(1000000)),
                        new SykefraværsstatistikkNæring(
                                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).getÅrstall(),
                                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).getKvartal(),
                                "10",
                                50,
                                new BigDecimal(30000),
                                new BigDecimal(1000000)),
                        new SykefraværsstatistikkNæring(
                                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).getÅrstall(),
                                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).getKvartal(),
                                "10",
                                50,
                                new BigDecimal(40000),
                                new BigDecimal(1000000)),
                        new SykefraværsstatistikkNæring(
                                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3).getÅrstall(),
                                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3).getKvartal(),
                                "10",
                                50,
                                new BigDecimal(50000),
                                new BigDecimal(1000000)),
                        new SykefraværsstatistikkNæring(
                                SISTE_PUBLISERTE_KVARTAL.getÅrstall(),
                                SISTE_PUBLISERTE_KVARTAL.getKvartal(),
                                "88",
                                50,
                                new BigDecimal(25000),
                                new BigDecimal(1000000)));
        List<SykefraværsstatistikkNæring> resultat =
                repository.hentSykefraværprosentAlleNæringer(
                        SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3), SISTE_PUBLISERTE_KVARTAL);
        assertThat(resultat.size()).isEqualTo(5);
        assertThat(resultat).containsExactlyInAnyOrderElementsOf(forventet);
    }

    @Test
    void
    hentSykefraværprosentAlleNæringer_siste4Kvartaler_kan_likevel_hente_bare_siste_publiserte_kvartal() {
        opprettStatistikkForNæringer(jdbcTemplate);
        List<SykefraværsstatistikkNæring> forventet =
                List.of(
                        new SykefraværsstatistikkNæring(
                                SISTE_PUBLISERTE_KVARTAL.getÅrstall(),
                                SISTE_PUBLISERTE_KVARTAL.getKvartal(),
                                "10",
                                50,
                                new BigDecimal(20000),
                                new BigDecimal(1000000)),
                        new SykefraværsstatistikkNæring(
                                SISTE_PUBLISERTE_KVARTAL.getÅrstall(),
                                SISTE_PUBLISERTE_KVARTAL.getKvartal(),
                                "88",
                                50,
                                new BigDecimal(25000),
                                new BigDecimal(1000000)));

        List<SykefraværsstatistikkNæring> resultat =
                repository.hentSykefraværprosentAlleNæringer(SISTE_PUBLISERTE_KVARTAL, 1);
        assertThat(resultat.size()).isEqualTo(2);
        assertThat(resultat).containsExactlyInAnyOrderElementsOf(forventet);
    }

    @Test
    void hentSykefraværprosentAlleNæringer_siste4Kvartaler_skalIkkeKrasjeVedManglendeData() {
        List<SykefraværsstatistikkNæring> resultat =
                repository.hentSykefraværprosentAlleNæringer(SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3));
        assertThat(resultat.size()).isEqualTo(0);
        assertThat(resultat).containsExactlyInAnyOrderElementsOf(List.of());
    }

    @Test
    void hentSykefraværAlleNæringer_siste4Kvartaler_skal_hente_riktig_data() {
        opprettStatistikkForNæringer(jdbcTemplate);
        List<SykefraværsstatistikkNæring> forventet =
                List.of(
                        new SykefraværsstatistikkNæring(
                                SISTE_PUBLISERTE_KVARTAL.getÅrstall(),
                                SISTE_PUBLISERTE_KVARTAL.getKvartal(),
                                "10",
                                50,
                                new BigDecimal(20000),
                                new BigDecimal(1000000)),
                        new SykefraværsstatistikkNæring(
                                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).getÅrstall(),
                                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).getKvartal(),
                                "10",
                                50,
                                new BigDecimal(30000),
                                new BigDecimal(1000000)),
                        new SykefraværsstatistikkNæring(
                                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).getÅrstall(),
                                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).getKvartal(),
                                "10",
                                50,
                                new BigDecimal(40000),
                                new BigDecimal(1000000)),
                        new SykefraværsstatistikkNæring(
                                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3).getÅrstall(),
                                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3).getKvartal(),
                                "10",
                                50,
                                new BigDecimal(50000),
                                new BigDecimal(1000000)),
                        new SykefraværsstatistikkNæring(
                                SISTE_PUBLISERTE_KVARTAL.getÅrstall(),
                                SISTE_PUBLISERTE_KVARTAL.getKvartal(),
                                "88",
                                50,
                                new BigDecimal(25000),
                                new BigDecimal(1000000)));
        List<SykefraværsstatistikkNæring> resultat =
                repository.hentSykefraværAlleNæringerFraOgMed(SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3));
        assertThat(resultat.size()).isEqualTo(5);
        assertThat(resultat).containsExactlyInAnyOrderElementsOf(forventet);
    }

    @Test
    void
    hentSykefraværAlleNæringer_siste4Kvartaler_kan_likevel_hente_bare_siste_publiserte_kvartal() {
        opprettStatistikkForNæringer(jdbcTemplate);
        List<SykefraværsstatistikkNæring> forventet =
                List.of(
                        new SykefraværsstatistikkNæring(
                                SISTE_PUBLISERTE_KVARTAL.getÅrstall(),
                                SISTE_PUBLISERTE_KVARTAL.getKvartal(),
                                "10",
                                50,
                                new BigDecimal(20000),
                                new BigDecimal(1000000)),
                        new SykefraværsstatistikkNæring(
                                SISTE_PUBLISERTE_KVARTAL.getÅrstall(),
                                SISTE_PUBLISERTE_KVARTAL.getKvartal(),
                                "88",
                                50,
                                new BigDecimal(25000),
                                new BigDecimal(1000000)));

        List<SykefraværsstatistikkNæring> resultat = repository.hentSykefraværAlleNæringerFraOgMed(
                SISTE_PUBLISERTE_KVARTAL);
        assertThat(resultat.size()).isEqualTo(2);
        assertThat(resultat).containsExactlyInAnyOrderElementsOf(forventet);
    }

    @Test
    void hentSykefraværAlleNæringer_siste4Kvartaler_skalIkkeKrasjeVedManglendeData() {
        List<SykefraværsstatistikkNæring> resultat =
                repository.hentSykefraværAlleNæringerFraOgMed(SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3));
        assertThat(resultat.size()).isEqualTo(0);
        assertThat(resultat).containsExactlyInAnyOrderElementsOf(List.of());
    }

    @Test
    void
    hentSykefraværprosentAlleNæringer5SifferForEttKvartal__skal_returnere_riktig_data_til_alle_næringer() {
        opprettStatistikkNæring5SifferTestData(
                new ÅrstallOgKvartal(2019, 2), new ÅrstallOgKvartal(2019, 1));

        List<SykefraværsstatistikkNæring5Siffer> resultat =
                repository.hentSykefraværprosentAlleNæringer5Siffer(new ÅrstallOgKvartal(2019, 2));

        assertSykefraværsstatistikkForNæringskode5SifferContains(
                resultat, 2019, 2, 10, produksjonAvKlær, 3, 100);
        assertSykefraværsstatistikkForNæringskode5SifferContains(
                resultat, 2019, 2, 10, undervisning, 5, 100);

        List<SykefraværsstatistikkNæring5Siffer> resultat_2019_1 =
                repository.hentSykefraværprosentAlleNæringer5Siffer(new ÅrstallOgKvartal(2019, 1));

        assertThat(resultat_2019_1.size()).isEqualTo(2);
        assertSykefraværsstatistikkForNæringskode5SifferContains(
                resultat_2019_1, 2019, 1, 10, produksjonAvKlær, 3, 100);
        assertSykefraværsstatistikkForNæringskode5SifferContains(
                resultat_2019_1, 2019, 1, 10, undervisning, 5, 100);
    }

    @Test
    void
    hentSykefraværprosentAlleNæringer5SifferForSiste4Kvartaler__skal_returnere_riktig_data_til_alle_næringer() {
        opprettStatistikkNæring5SifferTestData(
                new ÅrstallOgKvartal(2019, 2), new ÅrstallOgKvartal(2019, 1));

        List<SykefraværsstatistikkNæring5Siffer> resultat =
                repository.hentSykefraværprosentAlleNæringer5Siffer(new ÅrstallOgKvartal(2019, 2));

        assertSykefraværsstatistikkForNæringskode5SifferContains(
                resultat, 2019, 2, 10, produksjonAvKlær, 3, 100);
        assertSykefraværsstatistikkForNæringskode5SifferContains(
                resultat, 2019, 2, 10, undervisning, 5, 100);

        List<SykefraværsstatistikkNæring5Siffer> resultat_2019_1_til_2019_2 =
                repository.hentSykefraværprosentAlleNæringer5Siffer(
                        new ÅrstallOgKvartal(2019, 1), new ÅrstallOgKvartal(2019, 2));

        assertThat(resultat.size()).isEqualTo(2);
        assertThat(resultat_2019_1_til_2019_2.size()).isEqualTo(4);
        assertSykefraværsstatistikkForNæringskode5SifferContains(
                resultat_2019_1_til_2019_2, 2019, 1, 10, produksjonAvKlær, 3, 100);
        assertSykefraværsstatistikkForNæringskode5SifferContains(
                resultat_2019_1_til_2019_2, 2019, 1, 10, undervisning, 5, 100);
        assertSykefraværsstatistikkForNæringskode5SifferContains(
                resultat_2019_1_til_2019_2, 2019, 2, 10, produksjonAvKlær, 3, 100);
        assertSykefraværsstatistikkForNæringskode5SifferContains(
                resultat_2019_1_til_2019_2, 2019, 2, 10, undervisning, 5, 100);
    }

    @Test
    void
    hentSykefraværprosentAlleVirksomheter__skal_hente_alle_virksomheter_for_ett_eller_flere_kvartaler() {
        opprettStatistikkVirksomhetTestData();

        List<SykefraværsstatistikkVirksomhetUtenVarighet> resultat_2019_2 =
                repository.hentSykefraværAlleVirksomheter(new ÅrstallOgKvartal(2019, 2));

        assertThat(resultat_2019_2.size()).isEqualTo(2);
        assertSykefraværsstatistikkForVirksomhetIsEqual(
                resultat_2019_2, 2019, 2, 3, VIRKSOMHET_1, 1, 60);
        assertSykefraværsstatistikkForVirksomhetIsEqual(
                resultat_2019_2, 2019, 2, 4, VIRKSOMHET_2, 9, 100);

        List<SykefraværsstatistikkVirksomhetUtenVarighet> resultat_2019_1_TIL_2019_2 =
                repository.hentSykefraværAlleVirksomheter(
                        new ÅrstallOgKvartal(2019, 1), new ÅrstallOgKvartal(2019, 2));

        assertThat(resultat_2019_1_TIL_2019_2.size()).isEqualTo(4);
        assertSykefraværsstatistikkForVirksomhetIsEqual(
                resultat_2019_1_TIL_2019_2, 2019, 1, 40, VIRKSOMHET_1, 20, 115);
        assertSykefraværsstatistikkForVirksomhetIsEqual(
                resultat_2019_1_TIL_2019_2, 2019, 1, 7, VIRKSOMHET_2, 12, 100);
        assertSykefraværsstatistikkForVirksomhetIsEqual(
                resultat_2019_1_TIL_2019_2, 2019, 2, 3, VIRKSOMHET_1, 1, 60);
        assertSykefraværsstatistikkForVirksomhetIsEqual(
                resultat_2019_1_TIL_2019_2, 2019, 2, 4, VIRKSOMHET_2, 9, 100);

        List<SykefraværsstatistikkVirksomhetUtenVarighet> resultat_2018_3_TIL_2019_2 =
                repository.hentSykefraværAlleVirksomheter(
                        new ÅrstallOgKvartal(2018, 3), new ÅrstallOgKvartal(2019, 2));

        assertThat(resultat_2018_3_TIL_2019_2.size()).isEqualTo(8);
        assertSykefraværsstatistikkForVirksomhetIsEqual(
                resultat_2018_3_TIL_2019_2, 2018, 4, 40, VIRKSOMHET_1, 20, 115);
        assertSykefraværsstatistikkForVirksomhetIsEqual(
                resultat_2018_3_TIL_2019_2, 2018, 4, 7, VIRKSOMHET_2, 12, 100);
        assertSykefraværsstatistikkForVirksomhetIsEqual(
                resultat_2018_3_TIL_2019_2, 2018, 3, 3, VIRKSOMHET_1, 1, 60);
        assertSykefraværsstatistikkForVirksomhetIsEqual(
                resultat_2018_3_TIL_2019_2, 2018, 3, 4, VIRKSOMHET_2, 9, 100);
        assertSykefraværsstatistikkForVirksomhetIsEqual(
                resultat_2018_3_TIL_2019_2, 2019, 1, 40, VIRKSOMHET_1, 20, 115);
        assertSykefraværsstatistikkForVirksomhetIsEqual(
                resultat_2018_3_TIL_2019_2, 2019, 1, 7, VIRKSOMHET_2, 12, 100);
        assertSykefraværsstatistikkForVirksomhetIsEqual(
                resultat_2018_3_TIL_2019_2, 2019, 2, 3, VIRKSOMHET_1, 1, 60);
        assertSykefraværsstatistikkForVirksomhetIsEqual(
                resultat_2018_3_TIL_2019_2, 2019, 2, 4, VIRKSOMHET_2, 9, 100);
    }

    private void assertSykefraværsstatistikkForSektorIsEqual(
            List<SykefraværsstatistikkSektor> actual,
            int årstall,
            int kvartal,
            int antallPersoner,
            Sektor sektor,
            int tapteDagsverk,
            int muligeDagsverk) {
        List<SykefraværsstatistikkSektor> statistikkForSektor =
                actual.stream()
                        .filter(sfSektor -> sfSektor.getSektorkode().equals(sektor.getSektorkode()))
                        .toList();
        assertThat(statistikkForSektor.size()).isEqualTo(1);
        assertSykefraværsstatistikkIsEqual(
                statistikkForSektor.get(0),
                årstall,
                kvartal,
                antallPersoner,
                tapteDagsverk,
                muligeDagsverk);
    }

    private void assertSykefraværsstatistikkForVirksomhetIsEqual(
            List<SykefraværsstatistikkVirksomhetUtenVarighet> actual,
            int årstall,
            int kvartal,
            int antallPersoner,
            String orgnr,
            int tapteDagsverk,
            int muligeDagsverk) {
        List<SykefraværsstatistikkVirksomhetUtenVarighet> statistikkForVirksomhet =
                actual.stream()
                        .filter(
                                sfVirksomhet ->
                                        sfVirksomhet.getOrgnr().equals(orgnr)
                                                && sfVirksomhet.getårstall() == årstall
                                                && sfVirksomhet.getKvartal() == kvartal)
                        .toList();
        assertThat(statistikkForVirksomhet.size()).isEqualTo(1);
        assertSykefraværsstatistikkIsEqual(
                statistikkForVirksomhet.get(0),
                årstall,
                kvartal,
                antallPersoner,
                tapteDagsverk,
                muligeDagsverk);
    }

    private void assertSykefraværsstatistikkForNæringIsEqual(
            List<SykefraværsstatistikkNæring> actual,
            int årstall,
            int kvartal,
            int antallPersoner,
            Næring næring,
            int tapteDagsverk,
            int muligeDagsverk) {
        List<SykefraværsstatistikkNæring> statistikkForNæring =
                actual.stream()
                        .filter(sfNæring -> sfNæring.getNæringkode().equals(næring.getKode()))
                        .toList();
        assertThat(statistikkForNæring.size()).isEqualTo(1);
        assertSykefraværsstatistikkIsEqual(
                statistikkForNæring.get(0),
                årstall,
                kvartal,
                antallPersoner,
                tapteDagsverk,
                muligeDagsverk);
    }

    private void assertSykefraværsstatistikkForNæringskode5SifferContains(
            List<SykefraværsstatistikkNæring5Siffer> actual,
            int årstall,
            int kvartal,
            int antallPersoner,
            Næringskode5Siffer næringskode5Siffer,
            int tapteDagsverk,
            int muligeDagsverk) {
        List<SykefraværsstatistikkNæring5Siffer> statistikkForNæring5Siffer =
                actual.stream()
                        .filter(
                                sfNæring ->
                                        sfNæring.getNæringkode5siffer().equals(næringskode5Siffer.getKode())
                                                && sfNæring.getårstall() == årstall
                                                && sfNæring.getKvartal() == kvartal)
                        .collect(Collectors.toList());
        assertThat(statistikkForNæring5Siffer.size()).isEqualTo(1);
        assertSykefraværsstatistikkIsEqual(
                statistikkForNæring5Siffer.get(0),
                årstall,
                kvartal,
                antallPersoner,
                tapteDagsverk,
                muligeDagsverk);
    }

    private void assertSykefraværsstatistikkIsEqual(
            Sykefraværsstatistikk actual,
            int årstall,
            int kvartal,
            int antallPersoner,
            int tapteDagsverk,
            int muligeDagsverk) {
        assertThat(actual.getårstall()).isEqualTo(årstall);
        assertThat(actual.getKvartal()).isEqualTo(kvartal);
        assertThat(actual.getAntallPersoner()).isEqualTo(antallPersoner);
        assertBigDecimalIsEqual(actual.getTapteDagsverk(), new BigDecimal(tapteDagsverk));
        assertBigDecimalIsEqual(actual.getMuligeDagsverk(), new BigDecimal(muligeDagsverk));
    }

    // Metoder for å opprette testdata
    private void opprettStatistikkLandTestData() {
        createStatistikkLand(2019, 2, 2500000, 256800, 60000000);
        createStatistikkLand(2019, 1, 2750000, 350000, 71000000);
    }

    private void opprettStatistikkSektorTestData() {
        createStatistikkSektor(Sektor.KOMMUNAL, 2019, 2, 3, 1, 60);
        createStatistikkSektor(Sektor.KOMMUNAL, 2019, 1, 40, 20, 115);
        createStatistikkSektor(Sektor.PRIVAT, 2019, 2, 4, 9, 100);
        createStatistikkSektor(Sektor.PRIVAT, 2019, 1, 7, 12, 100);
    }

    private void opprettStatistikkVirksomhetTestData() {
        createStatistikkVirksomhet(VIRKSOMHET_1, 2018, 2, 3, 1, 60);
        createStatistikkVirksomhet(VIRKSOMHET_2, 2018, 2, 4, 9, 100);
        createStatistikkVirksomhet(VIRKSOMHET_1, 2018, 3, 3, 1, 60);
        createStatistikkVirksomhet(VIRKSOMHET_1, 2018, 4, 40, 20, 115);
        createStatistikkVirksomhet(VIRKSOMHET_2, 2018, 3, 4, 9, 100);
        createStatistikkVirksomhet(VIRKSOMHET_2, 2018, 4, 7, 12, 100);
        createStatistikkVirksomhet(VIRKSOMHET_1, 2019, 2, 3, 1, 60);
        createStatistikkVirksomhet(VIRKSOMHET_1, 2019, 1, 40, 20, 115);
        createStatistikkVirksomhet(VIRKSOMHET_2, 2019, 2, 4, 9, 100);
        createStatistikkVirksomhet(VIRKSOMHET_2, 2019, 1, 7, 12, 100);
    }

    private void opprettStatistikkNæringTestData(ÅrstallOgKvartal... årstallOgKvartal) {
        Arrays.stream(årstallOgKvartal)
                .forEach(
                        item -> {
                            createStatistikkNæring(produksjon, item.getÅrstall(), item.getKvartal(), 10, 2, 100);
                            createStatistikkNæring(utdanning, item.getÅrstall(), item.getKvartal(), 8, 5, 100);
                        });
    }

    private void opprettStatistikkNæring5SifferTestData(ÅrstallOgKvartal... årstallOgKvartals) {
        Arrays.stream(årstallOgKvartals)
                .forEach(
                        item -> {
                            createStatistikkNæring5Siffer(
                                    produksjonAvKlær, item.getÅrstall(), item.getKvartal(), 10, 3, 100);
                            createStatistikkNæring5Siffer(
                                    undervisning, item.getÅrstall(), item.getKvartal(), 10, 5, 100);
                        });
    }

    private void createStatistikkLand(
            int årstall, int kvartal, int antallPersoner, int tapteDagsverk, int muligeDagsverk) {
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_land "
                        + "(arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "values (:arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                leggTilParametreForSykefraværsstatistikk(
                        new MapSqlParameterSource(),
                        new SykefraværsstatistikkLand(
                                årstall,
                                kvartal,
                                antallPersoner,
                                new BigDecimal(tapteDagsverk),
                                new BigDecimal(muligeDagsverk))));
    }

    private void createStatistikkSektor(
            Sektor sektor,
            int årstall,
            int kvartal,
            int antallPersoner,
            int tapteDagsverk,
            int muligeDagsverk) {
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_sektor "
                        + "(sektor_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "values (:sektor_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(
                        new SykefraværsstatistikkSektor(
                                årstall,
                                kvartal,
                                sektor.getSektorkode(),
                                antallPersoner,
                                new BigDecimal(tapteDagsverk),
                                new BigDecimal(muligeDagsverk))));
    }

    private void createStatistikkNæring(
            Næring næring,
            int årstall,
            int kvartal,
            int antallPersoner,
            int tapteDagsverk,
            int muligeDagsverk) {
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_naring (naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "values (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(
                        new SykefraværsstatistikkNæring(
                                årstall,
                                kvartal,
                                næring.getKode(),
                                antallPersoner,
                                new BigDecimal(tapteDagsverk),
                                new BigDecimal(muligeDagsverk))));
    }

    private void createStatistikkNæring5Siffer(
            Næringskode5Siffer næring5Siffer,
            int årstall,
            int kvartal,
            int antallPersoner,
            int tapteDagsverk,
            int muligeDagsverk) {
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_naring5siffer "
                        + "(naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "values (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(
                        new SykefraværsstatistikkNæring5Siffer(
                                årstall,
                                kvartal,
                                næring5Siffer.getKode(),
                                antallPersoner,
                                new BigDecimal(tapteDagsverk),
                                new BigDecimal(muligeDagsverk))));
    }

    private void createStatistikkVirksomhet(
            String orgnr,
            int årstall,
            int kvartal,
            int antallPersoner,
            int tapteDagsverk,
            int muligeDagsverk) {
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_virksomhet "
                        + "(arstall, kvartal, orgnr, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "values (:arstall, :kvartal, :orgnr, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(
                        new SykefraværsstatistikkVirksomhetUtenVarighet(
                                årstall,
                                kvartal,
                                orgnr,
                                antallPersoner,
                                new BigDecimal(tapteDagsverk),
                                new BigDecimal(muligeDagsverk))));
    }

    private MapSqlParameterSource parametre(SykefraværsstatistikkSektor sykefraværsstatistikkSektor) {
        MapSqlParameterSource parametre =
                new MapSqlParameterSource()
                        .addValue("sektor_kode", sykefraværsstatistikkSektor.getSektorkode());

        return leggTilParametreForSykefraværsstatistikk(parametre, sykefraværsstatistikkSektor);
    }

    private MapSqlParameterSource parametre(SykefraværsstatistikkNæring sykefraværsstatistikkNæring) {
        MapSqlParameterSource parametre =
                new MapSqlParameterSource()
                        .addValue("naring_kode", sykefraværsstatistikkNæring.getNæringkode());

        return leggTilParametreForSykefraværsstatistikk(parametre, sykefraværsstatistikkNæring);
    }

    private MapSqlParameterSource parametre(
            SykefraværsstatistikkNæring5Siffer sykefraværsstatistikkNæring5Siffer) {
        MapSqlParameterSource parametre =
                new MapSqlParameterSource()
                        .addValue("naring_kode", sykefraværsstatistikkNæring5Siffer.getNæringkode5siffer());

        return leggTilParametreForSykefraværsstatistikk(parametre, sykefraværsstatistikkNæring5Siffer);
    }

    private MapSqlParameterSource parametre(
            SykefraværsstatistikkVirksomhetUtenVarighet sykefraværsstatistikkVirksomhet) {
        MapSqlParameterSource parametre =
                new MapSqlParameterSource().addValue("orgnr", sykefraværsstatistikkVirksomhet.getOrgnr());

        return leggTilParametreForSykefraværsstatistikk(parametre, sykefraværsstatistikkVirksomhet);
    }

    private MapSqlParameterSource leggTilParametreForSykefraværsstatistikk(
            MapSqlParameterSource parametre, Sykefraværsstatistikk sykefraværsstatistikk) {
        return parametre
                .addValue("arstall", sykefraværsstatistikk.getårstall())
                .addValue("kvartal", sykefraværsstatistikk.getKvartal())
                .addValue("antall_personer", sykefraværsstatistikk.getAntallPersoner())
                .addValue("tapte_dagsverk", sykefraværsstatistikk.getTapteDagsverk())
                .addValue("mulige_dagsverk", sykefraværsstatistikk.getMuligeDagsverk());
    }

    @Test
    void hentSykefraværAlleBransjerFraOgMed() {
    }
}
