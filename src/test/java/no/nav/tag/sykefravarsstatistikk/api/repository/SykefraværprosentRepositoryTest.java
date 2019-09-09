package no.nav.tag.sykefravarsstatistikk.api.repository;

import no.nav.tag.sykefravarsstatistikk.api.domain.stats.LandStatistikk;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static org.hamcrest.number.BigDecimalCloseTo.closeTo;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = {"mock.enabled=false"})
public class SykefraværprosentRepositoryTest {


    public static final BigDecimal DELTA_ERROR = new BigDecimal(0.0000001);

    @Autowired
    SykefravarprosentRepository repository;


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
    public void hentLandStatistikk_returnerer_____() {
        repository.hentLandStatistikk(2008, 2);
        fail("Repository skal returnere EmptyResultDataAccessException");
    }

}