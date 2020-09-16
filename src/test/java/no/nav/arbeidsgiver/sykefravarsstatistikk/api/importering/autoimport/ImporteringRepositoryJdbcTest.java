package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.Statistikkilde;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.parametreForStatistikk;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAllStatistikkFraDatabase;
import static org.assertj.core.api.Java6Assertions.assertThat;

@ActiveProfiles("db-test")
@RunWith(SpringRunner.class)
@DataJdbcTest
public class ImporteringRepositoryJdbcTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private ImporteringRepository importeringRepository;

    @Before
    public void setUp() {
        importeringRepository = new ImporteringRepository(jdbcTemplate);
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

        ÅrstallOgKvartal årstallOgKvartal = importeringRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(Statistikkilde.LAND);
        assertThat(årstallOgKvartal).isEqualTo(new ÅrstallOgKvartal(2019, 2));
    }

}
