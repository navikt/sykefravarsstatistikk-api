package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.AppConfigForJdbcTesterConfig;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Sektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.Sykefraværsstatistikk;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkLand;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkSektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhetUtenVarighet;
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
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.AssertUtils.assertBigDecimalIsEqual;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAllStatistikkFraDatabase;
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

    private Næringskode5Siffer produksjonAvKlær = new Næringskode5Siffer("14190", "Produksjon av klær");
    private Næringskode5Siffer undervisning = new Næringskode5Siffer("86907", "Undervisning");
    private Næring utdanning = new Næring("86", "Utdanning");
    private Næring produksjon = new Næring("14", "Produksjon");
    private Sektor kommunalForvaltning = new Sektor("1", "Kommunal forvaltning");
    private Sektor næringsvirksomhet = new Sektor("3", "Privat og offentlig næringsvirksomhet");
    private String VIRKSOMHET_1 = "999999999";
    private String VIRKSOMHET_2 = "999999998";

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
        assertSykefraværsstatistikkForSektorIsEqual(resultat, 2019, 2, 3, kommunalForvaltning, 1, 60);
        assertSykefraværsstatistikkForSektorIsEqual(resultat, 2019, 2, 4, næringsvirksomhet, 9, 100);

        List<SykefraværsstatistikkSektor> resultat_2019_1 =
                repository.hentSykefraværprosentAlleSektorer(new ÅrstallOgKvartal(2019, 1));

        assertThat(resultat_2019_1.size()).isEqualTo(2);
        assertSykefraværsstatistikkForSektorIsEqual(resultat_2019_1, 2019, 1, 40, kommunalForvaltning, 20, 115);
        assertSykefraværsstatistikkForSektorIsEqual(resultat_2019_1, 2019, 1, 7, næringsvirksomhet, 12, 100);
    }

    @Test
    void hentSykefraværprosentAlleNæringer__skal_hente_alle_næringer_for_ett_kvartal() {
        opprettStatistikkNæringTestData();

        List<SykefraværsstatistikkNæring> resultat =
                repository.hentSykefraværprosentAlleNæringer(new ÅrstallOgKvartal(2019, 2));

        assertThat(resultat.size()).isEqualTo(2);
        assertSykefraværsstatistikkForNæringIsEqual(resultat, 2019, 2, 10, produksjon, 2, 100);
        assertSykefraværsstatistikkForNæringIsEqual(resultat, 2019, 2, 10, utdanning, 5, 100);

        List<SykefraværsstatistikkNæring> resultat_2019_1 =
                repository.hentSykefraværprosentAlleNæringer(new ÅrstallOgKvartal(2019, 1));

        assertThat(resultat_2019_1.size()).isEqualTo(2);
        assertSykefraværsstatistikkForNæringIsEqual(resultat_2019_1, 2019, 1, 10, produksjon, 3, 100);
        assertSykefraværsstatistikkForNæringIsEqual(resultat_2019_1, 2019, 1, 10, utdanning, 8, 100);
    }

    @Test
    void  hentSykefraværprosentAlleNæringer5SifferForEttKvartal__skal_returnere_riktig_data_til_alle_næringer() {
        opprettStatistikkNæring5SifferTestData();

        List<SykefraværsstatistikkNæring5Siffer> resultat =
                repository.hentSykefraværprosentAlleNæringer5Siffer(new ÅrstallOgKvartal(2019, 2));

        assertSykefraværsstatistikkForNæringskode5SifferIsEqual(resultat, 2019, 2, 10,produksjonAvKlær, 2, 100);
        assertSykefraværsstatistikkForNæringskode5SifferIsEqual(resultat, 2019, 2, 10, undervisning, 5, 100);

        List<SykefraværsstatistikkNæring5Siffer> resultat_2019_1 =
                repository.hentSykefraværprosentAlleNæringer5Siffer(new ÅrstallOgKvartal(2019, 1));

        assertThat(resultat_2019_1.size()).isEqualTo(2);
        assertSykefraværsstatistikkForNæringskode5SifferIsEqual(resultat_2019_1, 2019, 1, 10, produksjonAvKlær, 3, 100);
        assertSykefraværsstatistikkForNæringskode5SifferIsEqual(resultat_2019_1, 2019, 1, 10, undervisning, 8, 100);
    }

    @Test
    void hentSykefraværprosentAlleVirksomheter__skal_hente_alle_virksomheter_for_ett_kvartal() {
        opprettStatistikkVirksomhetTestData();

        List<SykefraværsstatistikkVirksomhetUtenVarighet> resultat =
                repository.hentSykefraværprosentAlleVirksomheter(new ÅrstallOgKvartal(2019, 2));

        assertThat(resultat.size()).isEqualTo(2);
        assertSykefraværsstatistikkForVirksomhetIsEqual(resultat, 2019, 2, 3, VIRKSOMHET_1, 1, 60);
        assertSykefraværsstatistikkForVirksomhetIsEqual(resultat, 2019, 2, 4, VIRKSOMHET_2, 9, 100);

        List<SykefraværsstatistikkVirksomhetUtenVarighet> resultat_2019_1 =
                repository.hentSykefraværprosentAlleVirksomheter(new ÅrstallOgKvartal(2019, 1));

        assertThat(resultat_2019_1.size()).isEqualTo(2);
        assertSykefraværsstatistikkForVirksomhetIsEqual(resultat_2019_1, 2019, 1, 40, VIRKSOMHET_1, 20, 115);
        assertSykefraværsstatistikkForVirksomhetIsEqual(resultat_2019_1, 2019, 1, 7, VIRKSOMHET_2, 12, 100);
    }


    // Metoder for assert/verifikasjon
    private void assertSykefraværsstatistikkForLandIsEqual(
            SykefraværsstatistikkLand actual,
            int årstall,
            int kvartal,
            int antallPersoner,
            Sektor sektor,
            int tapteDagsverk,
            int muligeDagsverk
    ) {
        assertSykefraværsstatistikkIsEqual(
                actual,
                årstall,
                kvartal,
                antallPersoner,
                tapteDagsverk,
                muligeDagsverk
        );
    }

    private void assertSykefraværsstatistikkForSektorIsEqual(
            List<SykefraværsstatistikkSektor> actual,
            int årstall,
            int kvartal,
            int antallPersoner,
            Sektor sektor,
            int tapteDagsverk,
            int muligeDagsverk
    ) {
        List<SykefraværsstatistikkSektor> statistikkForSektor = actual.stream()
                .filter(sfSektor ->
                        sfSektor.getSektorkode().equals(sektor.getKode())).collect(Collectors.toList());
        assertThat(statistikkForSektor.size()).isEqualTo(1);
        assertSykefraværsstatistikkIsEqual(
                statistikkForSektor.get(0),
                årstall,
                kvartal,
                antallPersoner,
                tapteDagsverk,
                muligeDagsverk
        );
    }

    private void assertSykefraværsstatistikkForVirksomhetIsEqual(
            List<SykefraværsstatistikkVirksomhetUtenVarighet> actual,
            int årstall,
            int kvartal,
            int antallPersoner,
            String orgnr,
            int tapteDagsverk,
            int muligeDagsverk
    ) {
        List<SykefraværsstatistikkVirksomhetUtenVarighet> statistikkForVirksomhet = actual.stream()
                .filter(sfVirksomhet ->
                        sfVirksomhet.getOrgnr().equals(orgnr)).collect(Collectors.toList());
        assertThat(statistikkForVirksomhet.size()).isEqualTo(1);
        assertSykefraværsstatistikkIsEqual(
                statistikkForVirksomhet.get(0),
                årstall,
                kvartal,
                antallPersoner,
                tapteDagsverk,
                muligeDagsverk
        );
    }

    private void assertSykefraværsstatistikkForNæringIsEqual(
            List<SykefraværsstatistikkNæring> actual,
            int årstall,
            int kvartal,
            int antallPersoner,
            Næring næring,
            int tapteDagsverk,
            int muligeDagsverk
    ) {
        List<SykefraværsstatistikkNæring> statistikkForNæring = actual.stream()
                .filter(sfNæring ->
                        sfNæring.getNæringkode().equals(næring.getKode())).collect(Collectors.toList());
        assertThat(statistikkForNæring.size()).isEqualTo(1);
        assertSykefraværsstatistikkIsEqual(
                statistikkForNæring.get(0),
                årstall,
                kvartal,
                antallPersoner,
                tapteDagsverk,
                muligeDagsverk
        );
    }

    private void assertSykefraværsstatistikkForNæringskode5SifferIsEqual(
            List<SykefraværsstatistikkNæring5Siffer> actual,
            int årstall,
            int kvartal,
            int antallPersoner,
            Næringskode5Siffer næringskode5Siffer,
            int tapteDagsverk,
            int muligeDagsverk
    ) {
        List<SykefraværsstatistikkNæring5Siffer> statistikkForNæring5Siffer = actual.stream()
                .filter(sfNæring ->
                        sfNæring.getNæringkode5siffer().equals(næringskode5Siffer.getKode())).collect(Collectors.toList());
        assertThat(statistikkForNæring5Siffer.size()).isEqualTo(1);
        assertSykefraværsstatistikkIsEqual(
                statistikkForNæring5Siffer.get(0),
                årstall,
                kvartal,
                antallPersoner,
                tapteDagsverk,
                muligeDagsverk
        );
    }

    private void assertSykefraværsstatistikkIsEqual(
            Sykefraværsstatistikk actual,
            int årstall,
            int kvartal,
            int antallPersoner,
            int tapteDagsverk,
            int muligeDagsverk
    ) {
        assertThat(actual.getÅrstall()).isEqualTo(årstall);
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
        createStatistikkSektor(kommunalForvaltning,2019, 2, 3, 1, 60);
        createStatistikkSektor(kommunalForvaltning, 2019, 1, 40, 20, 115);
        createStatistikkSektor(næringsvirksomhet, 2019, 2, 4, 9, 100);
        createStatistikkSektor(næringsvirksomhet, 2019, 1, 7, 12, 100);
    }

    private void opprettStatistikkVirksomhetTestData() {
        createStatistikkVirksomhet(VIRKSOMHET_1, 2019, 2, 3, 1, 60);
        createStatistikkVirksomhet(VIRKSOMHET_1, 2019, 1, 40, 20, 115);
        createStatistikkVirksomhet(VIRKSOMHET_2, 2019, 2, 4, 9, 100);
        createStatistikkVirksomhet(VIRKSOMHET_2, 2019, 1, 7, 12, 100);
    }

    private void opprettStatistikkNæringTestData() {
        createStatistikkNæring(produksjon, 2019, 2, 10, 2, 100);
        createStatistikkNæring(produksjon, 2019, 1, 10, 3, 100);
        createStatistikkNæring(utdanning, 2019, 2, 10, 5, 100);
        createStatistikkNæring(utdanning, 2019, 1, 10, 8, 100);
    }

    private void opprettStatistikkNæring5SifferTestData() {
        createStatistikkNæring5Siffer(produksjonAvKlær, 2019, 2, 10, 2, 100);
        createStatistikkNæring5Siffer(produksjonAvKlær, 2019, 1, 10, 3, 100);
        createStatistikkNæring5Siffer(undervisning, 2019, 2, 10, 5, 100);
        createStatistikkNæring5Siffer(undervisning, 2019, 1, 10, 8, 100);
    }

    private void createStatistikkLand(
            int årstall,
            int kvartal,
            int antallPersoner,
            int tapteDagsverk,
            int muligeDagsverk
    ) {
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_land " +
                        "(arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "values (:arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                leggTilParametreForSykefraværsstatistikk(
                        new MapSqlParameterSource(),
                        new SykefraværsstatistikkLand(
                                årstall,
                                kvartal,
                                antallPersoner,
                                new BigDecimal(tapteDagsverk),
                                new BigDecimal(muligeDagsverk)
                        )
                )
        );
    }

    private void createStatistikkSektor(
            Sektor sektor,
            int årstall,
            int kvartal,
            int antallPersoner,
            int tapteDagsverk,
            int muligeDagsverk
    ) {
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_sektor " +
                        "(sektor_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "values (:sektor_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(
                        new SykefraværsstatistikkSektor(
                                årstall,
                                kvartal,
                                sektor.getKode(),
                                antallPersoner,
                                new BigDecimal(tapteDagsverk),
                                new BigDecimal(muligeDagsverk)
                        )
                )
        );
    }

    private void createStatistikkNæring(
            Næring næring,
            int årstall,
            int kvartal,
            int antallPersoner,
            int tapteDagsverk,
            int muligeDagsverk
    ) {
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
                                new BigDecimal(muligeDagsverk)
                        )
                )
        );
    }

    private void createStatistikkNæring5Siffer(
            Næringskode5Siffer næring5Siffer,
            int årstall,
            int kvartal,
            int antallPersoner,
            int tapteDagsverk,
            int muligeDagsverk
    ) {
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_naring5siffer " +
                        "(naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "values (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(
                        new SykefraværsstatistikkNæring5Siffer(
                                årstall,
                                kvartal,
                                næring5Siffer.getKode(),
                                antallPersoner,
                                new BigDecimal(tapteDagsverk),
                                new BigDecimal(muligeDagsverk)
                        )
                )
        );
    }

    private void createStatistikkVirksomhet(
            String orgnr,
            int årstall,
            int kvartal,
            int antallPersoner,
            int tapteDagsverk,
            int muligeDagsverk
    ) {
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_virksomhet " +
                        "(arstall, kvartal, orgnr, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "values (:arstall, :kvartal, :orgnr, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(
                        new SykefraværsstatistikkVirksomhetUtenVarighet(
                                årstall,
                                kvartal,
                                orgnr,
                                antallPersoner,
                                new BigDecimal(tapteDagsverk),
                                new BigDecimal(muligeDagsverk)
                        )
                )
        );
    }

    private MapSqlParameterSource parametre(SykefraværsstatistikkSektor sykefraværsstatistikkSektor) {
        MapSqlParameterSource parametre = new MapSqlParameterSource()
                .addValue("sektor_kode", sykefraværsstatistikkSektor.getSektorkode());

        return leggTilParametreForSykefraværsstatistikk(parametre, sykefraværsstatistikkSektor);
    }

    private MapSqlParameterSource parametre(SykefraværsstatistikkNæring sykefraværsstatistikkNæring) {
        MapSqlParameterSource parametre = new MapSqlParameterSource()
                .addValue("naring_kode", sykefraværsstatistikkNæring.getNæringkode());

        return leggTilParametreForSykefraværsstatistikk(parametre, sykefraværsstatistikkNæring);
    }

    private MapSqlParameterSource parametre(SykefraværsstatistikkNæring5Siffer sykefraværsstatistikkNæring5Siffer) {
        MapSqlParameterSource parametre = new MapSqlParameterSource()
                .addValue("naring_kode", sykefraværsstatistikkNæring5Siffer.getNæringkode5siffer());

        return leggTilParametreForSykefraværsstatistikk(parametre, sykefraværsstatistikkNæring5Siffer);
    }

    private MapSqlParameterSource parametre(SykefraværsstatistikkVirksomhetUtenVarighet sykefraværsstatistikkVirksomhet) {
        MapSqlParameterSource parametre = new MapSqlParameterSource()
                .addValue("orgnr", sykefraværsstatistikkVirksomhet.getOrgnr());

        return leggTilParametreForSykefraværsstatistikk(parametre, sykefraværsstatistikkVirksomhet);
    }

    private MapSqlParameterSource leggTilParametreForSykefraværsstatistikk(
            MapSqlParameterSource parametre,
            Sykefraværsstatistikk sykefraværsstatistikk
    ) {
        return parametre
                .addValue("arstall", sykefraværsstatistikk.getÅrstall())
                .addValue("kvartal", sykefraværsstatistikk.getKvartal())
                .addValue("antall_personer", sykefraværsstatistikk.getAntallPersoner())
                .addValue("tapte_dagsverk", sykefraværsstatistikk.getTapteDagsverk())
                .addValue("mulige_dagsverk", sykefraværsstatistikk.getMuligeDagsverk());
    }
}
