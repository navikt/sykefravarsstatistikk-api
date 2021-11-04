package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjetype;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis.KvartalsvisSykefraværRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAllStatistikkFraDatabase;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori._1_DAG_TIL_7_DAGER;
import static org.assertj.core.api.Java6Assertions.assertThat;

@ActiveProfiles("db-test")
@DataJdbcTest
public class SykefraværRepositoryJdbcTest {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private SykefraværRepository sykefraværRepository;

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
    public void hentSykefraværprosentVirksomhet__skal_returnere_riktig_sykefravær() {
        Underenhet barnehage = Underenhet.builder().orgnr(new Orgnr("999999999"))
                .navn("test Barnehage")
                .næringskode(new Næringskode5Siffer("88911", "Barnehage"))
                .antallAnsatte(10)
                .overordnetEnhetOrgnr(new Orgnr("1111111111")).build();
        lagDataset(barnehage);

        List<UmaskertSykefraværForEttKvartal> resultat = sykefraværRepository.hentUmaskertSykefraværForEttKvartalListe(
                barnehage,
                new ÅrstallOgKvartal(2018,3));
        assertThat(resultat.size()).isEqualTo(4);
        assertThat(resultat.get(0)).isEqualTo(new UmaskertSykefraværForEttKvartal(
                new ÅrstallOgKvartal(2018, 3),
                new BigDecimal(6),
                new BigDecimal(100),
                10
        ));
        assertThat(resultat.get(3)).isEqualTo(new UmaskertSykefraværForEttKvartal(
                new ÅrstallOgKvartal(2019, 2),
                new BigDecimal(2),
                new BigDecimal(100),
                10
        ));
    }

    @Test
    public void hentSykefraværprosentVirksomhet__skal_returnere_riktig_sykefravær_for_ønskede_kvartaler() {
        Underenhet barnehage = Underenhet.builder().orgnr(new Orgnr("999999999"))
                .navn("test Barnehage")
                .næringskode(new Næringskode5Siffer("88911", "Barnehage"))
                .antallAnsatte(10)
                .overordnetEnhetOrgnr(new Orgnr("1111111111")).build();
        lagDataset(barnehage);

        List<UmaskertSykefraværForEttKvartal> resultat = sykefraværRepository.hentUmaskertSykefraværForEttKvartalListe(
                barnehage,
                new ÅrstallOgKvartal(2019,1));
        assertThat(resultat.size()).isEqualTo(2);
        assertThat(resultat.get(0)).isEqualTo(new UmaskertSykefraværForEttKvartal(
                new ÅrstallOgKvartal(2019, 1),
                new BigDecimal(3),
                new BigDecimal(100),
                10
        ));
        assertThat(resultat.get(1)).isEqualTo(new UmaskertSykefraværForEttKvartal(
                new ÅrstallOgKvartal(2019, 2),
                new BigDecimal(2),
                new BigDecimal(100),
                10
        ));
    }
    private void lagDataset(Underenhet barnehage) {
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

/*    private MapSqlParameterSource parametre(Næring næring, int årstall, int kvartal, int antallPersoner, int tapteDagsverk, int muligeDagsverk) {
        return parametre(årstall, kvartal, antallPersoner, tapteDagsverk, muligeDagsverk)
                .addValue("naring_kode", næring.getKode());
    }
*/
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
}
