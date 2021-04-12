package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring;
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
import java.util.stream.Collectors;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.AssertUtils.assertBigDecimalIsEqual;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAllStatistikkFraDatabase;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("db-test")
@DataJdbcTest
class SykefraværsstatistikkTilEksporteringRepositoryTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private SykefraværsstatistikkTilEksporteringRepository repository;

    private Næring produksjonAvKlær = new Næring("14190", "Produksjon av klær");
    private Næring undervisning = new Næring("86907", "Undervisning");
    private Næring utdanning = new Næring("86", "Utdanning");
    private Næring _2SifferKode = new Næring("85", "Vilkårlig 2 siffer kode");
    private Næring _5SifferKode = new Næring("08500", "Vilkårlig 5 siffer kode");
    private Næring produksjon = new Næring("14", "Produksjon");

    @BeforeEach
    void setUp() {
        slettAllStatistikkFraDatabase(jdbcTemplate);
        repository = new SykefraværsstatistikkTilEksporteringRepository(jdbcTemplate);
        // TODO: bruk forskjellige objekter til 2- og 5-siffer næring

        createStatistikkNæring5Siffer(produksjonAvKlær, 2019, 2, 10, 2, 100);
        createStatistikkNæring5Siffer(produksjonAvKlær, 2019, 1, 10, 3, 100);
        createStatistikkNæring5Siffer(undervisning, 2019, 2, 10, 5, 100);
        createStatistikkNæring5Siffer(undervisning, 2019, 1, 10, 8, 100);

        createStatistikkNæring(produksjon, 2019, 2, 10, 2, 100);
        createStatistikkNæring(produksjon, 2019, 1, 10, 3, 100);
        createStatistikkNæring(utdanning, 2019, 2, 10, 5, 100);
        createStatistikkNæring(utdanning, 2019, 1, 10, 8, 100);

        createStatistikkVirksomhet("999999999", _2SifferKode, _5SifferKode, 2019, 2, 3, 0, 60);
        createStatistikkVirksomhet("999999998", _2SifferKode, _5SifferKode, 2019, 2, 4, 0, 100);
        createStatistikkVirksomhet("999999999", _2SifferKode, _5SifferKode, 2019, 2, 40, 20, 115);
    }


    @AfterEach
    void tearDown() {
        slettAllStatistikkFraDatabase(jdbcTemplate);
    }


    @Test
    void  hentSykefraværprosentAlleNæringer5SifferForEttKvartal__skal_returnere_riktig_data_til_alle_næringer() {
        List<SykefraværsstatistikkNæring> resultat =
                repository.hentSykefraværprosentAlleNæringer5Siffer(new ÅrstallOgKvartal(2019, 2));

        assertSykefraværsstatistikkNæringIsEqual(resultat, 2019, 2, produksjonAvKlær, 2, 100);
        assertSykefraværsstatistikkNæringIsEqual(resultat, 2019, 2, undervisning, 5, 100);

        List<SykefraværsstatistikkNæring> resultat_2019_1 =
                repository.hentSykefraværprosentAlleNæringer5Siffer(new ÅrstallOgKvartal(2019, 1));

        assertThat(resultat_2019_1.size()).isEqualTo(2);
        assertSykefraværsstatistikkNæringIsEqual(resultat_2019_1, 2019, 1, produksjonAvKlær, 3, 100);
        assertSykefraværsstatistikkNæringIsEqual(resultat_2019_1, 2019, 1, undervisning, 8, 100);
    }

    @Test
    void hentSykefraværprosentAlleNæringer__skal_hente_alle_næringer_for_ett_kvartal() {
        List<SykefraværsstatistikkNæring> resultat =
                repository.hentSykefraværprosentAlleNæringer(new ÅrstallOgKvartal(2019, 2));

        assertThat(resultat.size()).isEqualTo(2);
        assertSykefraværsstatistikkNæringIsEqual(resultat, 2019, 2, produksjon, 2, 100);
        assertSykefraværsstatistikkNæringIsEqual(resultat, 2019, 2, utdanning, 5, 100);

        List<SykefraværsstatistikkNæring> resultat_2019_1 =
                repository.hentSykefraværprosentAlleNæringer(new ÅrstallOgKvartal(2019, 1));

        assertThat(resultat_2019_1.size()).isEqualTo(2);
        assertSykefraværsstatistikkNæringIsEqual(resultat_2019_1, 2019, 1, produksjon, 3, 100);
        assertSykefraværsstatistikkNæringIsEqual(resultat_2019_1, 2019, 1, utdanning, 8, 100);
    }


    // Metoder for assert/verifikasjon

    private void assertSykefraværsstatistikkNæringIsEqual(
            List<SykefraværsstatistikkNæring> resultat,
            int årstall,
            int kvartal,
            Næring næring,
            int tapteDagsverk,
            int muligeDagsverk
    ) {
        List<SykefraværsstatistikkNæring> sfProduksjonAvKlær = resultat.stream()
                .filter(sfNæring ->
                        sfNæring.getNæringkode().equals(næring.getKode())).collect(Collectors.toList());
        assertThat(sfProduksjonAvKlær.size()).isEqualTo(1);
        assertThat(sfProduksjonAvKlær.get(0).getNæringkode()).isEqualTo(næring.getKode());
        assertThat(sfProduksjonAvKlær.get(0).getÅrstall()).isEqualTo(årstall);
        assertThat(sfProduksjonAvKlær.get(0).getKvartal()).isEqualTo(kvartal);
        assertBigDecimalIsEqual(sfProduksjonAvKlær.get(0).getTapteDagsverk(), new BigDecimal(tapteDagsverk));
        assertBigDecimalIsEqual(sfProduksjonAvKlær.get(0).getMuligeDagsverk(), new BigDecimal(muligeDagsverk));
    }

    // Metoder for å opprette testdata

    private void createStatistikkNæring5Siffer(Næring næring5Siffer, int årstall, int kvartal, int antallPersoner, int tapteDagsverk, int muligeDagsverk) {
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_naring5siffer (naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(næring5Siffer, årstall, kvartal, antallPersoner, new BigDecimal(tapteDagsverk), new BigDecimal(muligeDagsverk))
        );
    }

    private void createStatistikkVirksomhet(String orgnr, Næring næring, Næring næring5Siffer, int årstall, int kvartal, int antallPersoner, int tapteDagsverk, int muligeDagsverk) {
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_virksomhet_med_gradering (arstall, kvartal, orgnr, naring, naring_kode, antall_graderte_sykemeldinger, tapte_dagsverk_gradert_sykemelding,antall_sykemeldinger, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:arstall, :kvartal, :orgnr, :naring, :naring_kode, :antall_graderte_sykemeldinger, :tapte_dagsverk_gradert_sykemelding, :antall_sykemeldinger, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(årstall, kvartal, orgnr, næring.getKode(), næring5Siffer.getKode(), 0, 0, 0, antallPersoner, tapteDagsverk, muligeDagsverk)
        );
    }

    private void createStatistikkNæring(Næring næring, int årstall, int kvartal, int antallPersoner, int tapteDagsverk, int muligeDagsverk) {
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_naring (naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(næring, årstall, kvartal, antallPersoner, new BigDecimal(tapteDagsverk), new BigDecimal(muligeDagsverk))
        );
    }


    private MapSqlParameterSource parametre(Næring næring, int årstall, int kvartal, int antallPersoner, BigDecimal tapteDagsverk, BigDecimal muligeDagsverk) {
        return new MapSqlParameterSource()
                .addValue("naring_kode", næring.getKode())
                .addValue("arstall", årstall)
                .addValue("kvartal", kvartal)
                .addValue("antall_personer", antallPersoner)
                .addValue("tapte_dagsverk", tapteDagsverk)
                .addValue("mulige_dagsverk", muligeDagsverk);
    }

    private MapSqlParameterSource parametre(int årstall, int kvartal, String orgnr, String næringskode2Siffer, String næringskode5Siffer, int antall_graderte_sykemeldinger, int tapte_dagsverk_gradert_sykemelding, int antall_sykemeldinger, int antallPersoner, int tapteDagsverk, int muligeDagsverk) {
        return new MapSqlParameterSource()
                .addValue("arstall", årstall)
                .addValue("kvartal", kvartal)
                .addValue("orgnr", orgnr)
                .addValue("naring", næringskode2Siffer)
                .addValue("naring_kode", næringskode5Siffer)
                .addValue("antall_graderte_sykemeldinger", antall_graderte_sykemeldinger)
                .addValue("tapte_dagsverk_gradert_sykemelding", tapte_dagsverk_gradert_sykemelding)
                .addValue("antall_sykemeldinger", antall_sykemeldinger)
                .addValue("antall_personer", antallPersoner)
                .addValue("tapte_dagsverk", tapteDagsverk)
                .addValue("mulige_dagsverk", muligeDagsverk);
    }
}
