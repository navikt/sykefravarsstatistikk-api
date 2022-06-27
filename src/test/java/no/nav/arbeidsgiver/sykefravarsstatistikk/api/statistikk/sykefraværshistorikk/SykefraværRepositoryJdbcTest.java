package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.AppConfigForJdbcTesterConfig;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Kvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository;
import org.jetbrains.annotations.NotNull;
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

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAllStatistikkFraDatabase;
import static org.assertj.core.api.Java6Assertions.assertThat;

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfigForJdbcTesterConfig.class})
@DataJdbcTest(excludeAutoConfiguration = {TestDatabaseAutoConfiguration.class})
public class SykefraværRepositoryJdbcTest {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private SykefraværRepository sykefraværRepository;

    public static final Underenhet BARNEHAGE = Underenhet.builder().orgnr(new Orgnr("999999999"))
            .navn("test Barnehage")
            .næringskode(new Næringskode5Siffer("88911", "Barnehage"))
            .antallAnsatte(10)
            .overordnetEnhetOrgnr(new Orgnr("1111111111")).build();


    @BeforeEach
    public void setUp() {
        sykefraværRepository = new SykefraværRepository(jdbcTemplate);
        slettAllStatistikkFraDatabase(jdbcTemplate);
    }

    @AfterEach
    public void tearDown() {
        slettAllStatistikkFraDatabase(jdbcTemplate);
    }


    @Test
    public void hentSykefraværprosentVirksomhet__skal_returnerer_empty_list_dersom_ingen_data_funnet_for_årstall_og_kvartal() {
        persisterDatasetIDb(BARNEHAGE);

        List<UmaskertSykefraværForEttKvartal> resultat = sykefraværRepository.hentUmaskertSykefraværForEttKvartalListe(
                BARNEHAGE,
                new Kvartal(2021, 4));

        assertThat(resultat.size()).isEqualTo(0);
    }

    @Test
    public void hentSykefraværprosentVirksomhet__skal_returnere_riktig_sykefravær() {
        persisterDatasetIDb(BARNEHAGE);

        List<UmaskertSykefraværForEttKvartal> resultat = sykefraværRepository.hentUmaskertSykefraværForEttKvartalListe(
                BARNEHAGE,
                new Kvartal(2018, 3));

        assertThat(resultat.size()).isEqualTo(4);
        assertThat(resultat.get(0)).isEqualTo(sykefraværForEtÅrstallOgKvartal(2018, 3, 6));
        assertThat(resultat.get(3)).isEqualTo(sykefraværForEtÅrstallOgKvartal(2019, 2, 2));
    }

    @Test
    public void hentSykefraværprosentVirksomhet__skal_returnere_riktig_sykefravær_for_ønskede_kvartaler() {
        persisterDatasetIDb(BARNEHAGE);

        List<UmaskertSykefraværForEttKvartal> resultat = sykefraværRepository.hentUmaskertSykefraværForEttKvartalListe(
                BARNEHAGE,
                new Kvartal(2019, 1));
        assertThat(resultat.size()).isEqualTo(2);
        assertThat(resultat.get(0)).isEqualTo(sykefraværForEtÅrstallOgKvartal(2019, 1, 3));
        assertThat(resultat.get(1)).isEqualTo(sykefraværForEtÅrstallOgKvartal(2019, 2, 2));
    }


    private void persisterDatasetIDb(Underenhet barnehage) {
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_virksomhet (orgnr, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:orgnr, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(barnehage.getOrgnr(), 2019, 2, 10, 2, 100)
        );
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_virksomhet (orgnr, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:orgnr, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(new Orgnr("987654321"), 2019, 1, 10, 3, 100)
        );
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_virksomhet (orgnr, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:orgnr, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(barnehage.getOrgnr(), 2019, 1, 10, 3, 100)
        );
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_virksomhet (orgnr, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:orgnr, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(barnehage.getOrgnr(), 2018, 4, 10, 5, 100)
        );
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_virksomhet (orgnr, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:orgnr, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(barnehage.getOrgnr(), 2018, 3, 10, 6, 100)
        );
    }

    private MapSqlParameterSource parametre(int årstall, int kvartal, int antallPersoner, int tapteDagsverk, int muligeDagsverk) {
        return new MapSqlParameterSource()
                .addValue("arstall", årstall)
                .addValue("kvartal", kvartal)
                .addValue("antall_personer", antallPersoner)
                .addValue("tapte_dagsverk", tapteDagsverk)
                .addValue("mulige_dagsverk", muligeDagsverk);
    }

    private MapSqlParameterSource parametre(
            Orgnr orgnr,
            int årstall,
            int kvartal,
            int antallPersoner,
            int tapteDagsverk,
            int muligeDagsverk
    ) {
        return parametre(årstall, kvartal, antallPersoner, tapteDagsverk, muligeDagsverk)
                .addValue("orgnr", orgnr.getVerdi());
    }

    @NotNull
    private static UmaskertSykefraværForEttKvartal sykefraværForEtÅrstallOgKvartal(int årstall, int kvartal, int totalTapteDagsverk) {
        return sykefraværForEtÅrstallOgKvartal(årstall, kvartal, totalTapteDagsverk, 100, 10);
    }

    private static UmaskertSykefraværForEttKvartal sykefraværForEtÅrstallOgKvartal(
            int årstall,
            int kvartal,
            int totalTapteDagsverk,
            int totalMuligeDagsverk,
            int totalAntallPersoner
    ) {
        return new UmaskertSykefraværForEttKvartal(
                new Kvartal(årstall, kvartal),
                new BigDecimal(totalTapteDagsverk),
                new BigDecimal(totalMuligeDagsverk),
                totalAntallPersoner
        );
    }


}
