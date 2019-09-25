package no.nav.tag.sykefravarsstatistikk.api.sammenligning;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = {"mock.enabled=false"})
public class SykefraværprosentRepositoryTest {


    public static final BigDecimal DELTA_ERROR = new BigDecimal(0.0000001);

    @Autowired
    SammenligningRepository repository;

/*
    @Test
    public void hentLandStatistikk_returnerer_LandStatistikk_basert_på_ARSTALL_og_KVARTAL() {
        LandStatistikk landStatistikk = repository.hentLandStatistikk(2019, 1);

        assertTrue(landStatistikk.getArstall() == 2019);
        assertTrue(landStatistikk.getKvartal() == 1);
        assertThat(
                landStatistikk.getTapteDagsverk(),
                closeTo(
                        new BigDecimal(7605099.154119),
                        DELTA_ERROR
                )
        );

        assertThat(
                landStatistikk.getMuligeDagsverk(),
                closeTo(
                        new BigDecimal(138978910.743955),
                        DELTA_ERROR
                )
        );
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void hentLandStatistikk_returnerer_exception_dersom_ingen_data_er_funnet() {
        repository.hentLandStatistikk(2008, 2);
        fail("Repository skal returnere EmptyResultDataAccessException");
    }

 */

}