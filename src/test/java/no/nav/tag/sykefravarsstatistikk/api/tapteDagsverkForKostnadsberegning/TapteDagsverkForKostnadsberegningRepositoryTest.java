package no.nav.tag.sykefravarsstatistikk.api.tapteDagsverkForKostnadsberegning;


import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static no.nav.tag.sykefravarsstatistikk.api.TestData.*;
import static no.nav.tag.sykefravarsstatistikk.api.TestUtils.insertStatistikkForVirksomhet;
import static no.nav.tag.sykefravarsstatistikk.api.TestUtils.slettAllStatistikkFraDatabase;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("db-test")
@RunWith(SpringRunner.class)
@DataJdbcTest
public class TapteDagsverkForKostnadsberegningRepositoryTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private TapteDagsverkForKostnadsberegningRepository repository;

    @Before
    public void setUp() {
        repository = new TapteDagsverkForKostnadsberegningRepository(jdbcTemplate);
        slettAllStatistikkFraDatabase(jdbcTemplate);
    }

    @After
    public void cleanUp() {
        slettAllStatistikkFraDatabase(jdbcTemplate);
    }

    @Test
    public void hentTapteDagsverkFor4Kvartaler__skal_returnere_riktig_data_fra_database() {
        Orgnr orgnr = etOrgnr();

        List<TapteDagsverk> tapteDagsverkListe = Arrays.asList(
                testTapteDagsverk(1, 2019, 1),
                testTapteDagsverk(10, 2019, 2),
                testTapteDagsverk(100, 2019, 3),
                testTapteDagsverk(1000, 2018, 4)
        );


        insertTapteDagsverk(orgnr, tapteDagsverkListe);

        List<TapteDagsverk> resultat = repository.hentTapteDagsverkFor4Kvartaler(Arrays.asList(
                new ÅrstallOgKvartal(2019, 1),
                new ÅrstallOgKvartal(2019, 2),
                new ÅrstallOgKvartal(2019, 3),
                new ÅrstallOgKvartal(2018, 4)
                ),
                orgnr);

        assertThat(resultat).containsExactlyInAnyOrder(toArray(tapteDagsverkListe));
    }

    @Test
    public void hentTapteDagsverkFor4Kvartaler__skal_returnere_tom_liste_hvis_ingen_data() {
        List<TapteDagsverk> resultat = repository.hentTapteDagsverkFor4Kvartaler(Arrays.asList(
                new ÅrstallOgKvartal(2019, 1),
                new ÅrstallOgKvartal(2019, 2),
                new ÅrstallOgKvartal(2019, 3),
                new ÅrstallOgKvartal(2018, 4)
                ),
                etOrgnr());
        assertThat(resultat).isEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void hentTapteDagsverkFor4Kvartaler__skal_feile_hvis_liste_med_tapte_dagsverk_ikke_har_lengde_4() {
        repository.hentTapteDagsverkFor4Kvartaler(Arrays.asList(
                new ÅrstallOgKvartal(2019, 1),
                new ÅrstallOgKvartal(2019, 2),
                new ÅrstallOgKvartal(2019, 3)
                ),
                etOrgnr());
    }

    @Test
    public void hentTapteDagsverkFor4Kvartaler__skal_returnere_færre_enn_4_rader_hvis_alle_kvartalene_ikke_er_tilgjengelige() {
        Underenhet underenhet = enUnderenhet();
        Orgnr orgnr = underenhet.getOrgnr();

        List<TapteDagsverk> tapteDagsverkListe = Arrays.asList(
                testTapteDagsverk(1, 2019, 1),
                testTapteDagsverk(10, 2019, 2),
                testTapteDagsverk(1000, 2018, 4)
        );

        insertTapteDagsverk(orgnr, tapteDagsverkListe);

        List<TapteDagsverk> resultat = repository.hentTapteDagsverkFor4Kvartaler(Arrays.asList(
                new ÅrstallOgKvartal(2019, 1),
                new ÅrstallOgKvartal(2019, 2),
                new ÅrstallOgKvartal(2019, 3),
                new ÅrstallOgKvartal(2018, 4)
                ),
                etOrgnr());

        assertThat(resultat).containsExactlyInAnyOrder(toArray(tapteDagsverkListe));
    }

    @Test
    public void hentTapteDagsverkFor4Kvartaler__skal_bare_returnere_tapte_dagsverk_for_eget_orgnr() {
        Orgnr orgnr1 = new Orgnr("111111111");
        Orgnr orgnr2 = new Orgnr("222222222");

        List<TapteDagsverk> tapteDagsverkOrgnr1 = Arrays.asList(
                testTapteDagsverk(1, 2018, 1),
                testTapteDagsverk(1, 2018, 2),
                testTapteDagsverk(1, 2018, 3),
                testTapteDagsverk(1, 2018, 4)
        );

        List<TapteDagsverk> tapteDagsverkOrgnr2 = Arrays.asList(
                testTapteDagsverk(2, 2018, 1),
                testTapteDagsverk(2, 2018, 2),
                testTapteDagsverk(2, 2018, 3),
                testTapteDagsverk(2, 2018, 4)
        );

        insertTapteDagsverk(orgnr1, tapteDagsverkOrgnr1);
        insertTapteDagsverk(orgnr2, tapteDagsverkOrgnr2);

        List<TapteDagsverk> resultat = repository.hentTapteDagsverkFor4Kvartaler(Arrays.asList(
                new ÅrstallOgKvartal(2018, 1),
                new ÅrstallOgKvartal(2018, 2),
                new ÅrstallOgKvartal(2018, 3),
                new ÅrstallOgKvartal(2018, 4)
                ),
                orgnr1);

        assertThat(resultat).containsExactlyInAnyOrder(toArray(tapteDagsverkOrgnr1));
    }

    @Test
    public void hentTapteDagsverkFor4Kvartaler__skal_bare_returnere_tapte_dagsverk_for_innsendte_kvartaler() {
        Orgnr orgnr = new Orgnr("111111111");

        List<TapteDagsverk> riktigeTapteDagsverk = Arrays.asList(
                testTapteDagsverk(100, 2018, 1),
                testTapteDagsverk(100, 2018, 2),
                testTapteDagsverk(100, 2018, 3),
                testTapteDagsverk(100, 2018, 4)
        );

        List<TapteDagsverk> feilTapteDagsverk = Arrays.asList(
                testTapteDagsverk(200, 2019, 1),
                testTapteDagsverk(200, 2019, 2),
                testTapteDagsverk(200, 2019, 3),
                testTapteDagsverk(200, 2019, 4)
        );

        insertTapteDagsverk(orgnr, riktigeTapteDagsverk);
        insertTapteDagsverk(orgnr, feilTapteDagsverk);

        List<TapteDagsverk> resultat = repository.hentTapteDagsverkFor4Kvartaler(Arrays.asList(
                new ÅrstallOgKvartal(2018, 1),
                new ÅrstallOgKvartal(2018, 2),
                new ÅrstallOgKvartal(2018, 3),
                new ÅrstallOgKvartal(2018, 4)
                ),
                orgnr);

        assertThat(resultat).containsExactlyInAnyOrder(toArray(riktigeTapteDagsverk));
    }

    private TapteDagsverk[] toArray(List<TapteDagsverk> tapteDagsverk) {
        return tapteDagsverk.toArray(new TapteDagsverk[0]);
    }

    private void insertTapteDagsverk(Orgnr orgnr, List<TapteDagsverk> tapteDagsverkListe) {
        tapteDagsverkListe.forEach(tapteDagsverk -> insertTapteDagsverk(orgnr, tapteDagsverk));
    }

    private void insertTapteDagsverk(Orgnr orgnr, TapteDagsverk tapteDagsverk) {
        insertStatistikkForVirksomhet(
                jdbcTemplate,
                orgnr,
                tapteDagsverk.getÅrstall(),
                tapteDagsverk.getKvartal(),
                10,
                tapteDagsverk.getTapteDagsverk(),
                1000
        );
    }
}
