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
public class KvartalsvisTapteDagsverkForKostnadsberegningRepositoryTest {

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

        List<KvartalsvisTapteDagsverk> kvartalsvisTapteDagsverkListe = Arrays.asList(
                testTapteDagsverk(1, 2019, 1, 10),
                testTapteDagsverk(10, 2019, 2, 10),
                testTapteDagsverk(100, 2019, 3, 10),
                testTapteDagsverk(1000, 2018, 4, 10)
        );


        insertTapteDagsverk(orgnr, kvartalsvisTapteDagsverkListe);

        List<KvartalsvisTapteDagsverk> resultat = repository.hentTapteDagsverkFor4Kvartaler(Arrays.asList(
                new ÅrstallOgKvartal(2019, 1),
                new ÅrstallOgKvartal(2019, 2),
                new ÅrstallOgKvartal(2019, 3),
                new ÅrstallOgKvartal(2018, 4)
                ),
                orgnr);

        assertThat(resultat).containsExactlyInAnyOrder(toArray(kvartalsvisTapteDagsverkListe));
    }

    @Test
    public void hentTapteDagsverkFor4Kvartaler__skal_returnere_tom_liste_hvis_ingen_data() {
        List<KvartalsvisTapteDagsverk> resultat = repository.hentTapteDagsverkFor4Kvartaler(Arrays.asList(
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

        List<KvartalsvisTapteDagsverk> kvartalsvisTapteDagsverkListe = Arrays.asList(
                testTapteDagsverk(1, 2019, 1, 10),
                testTapteDagsverk(10, 2019, 2, 10),
                testTapteDagsverk(1000, 2018, 4, 10)
        );

        insertTapteDagsverk(orgnr, kvartalsvisTapteDagsverkListe);

        List<KvartalsvisTapteDagsverk> resultat = repository.hentTapteDagsverkFor4Kvartaler(Arrays.asList(
                new ÅrstallOgKvartal(2019, 1),
                new ÅrstallOgKvartal(2019, 2),
                new ÅrstallOgKvartal(2019, 3),
                new ÅrstallOgKvartal(2018, 4)
                ),
                etOrgnr());

        assertThat(resultat).containsExactlyInAnyOrder(toArray(kvartalsvisTapteDagsverkListe));
    }

    @Test
    public void hentTapteDagsverkFor4Kvartaler__skal_bare_returnere_tapte_dagsverk_for_eget_orgnr() {
        Orgnr orgnr1 = new Orgnr("111111111");
        Orgnr orgnr2 = new Orgnr("222222222");

        List<KvartalsvisTapteDagsverk> kvartalsvisTapteDagsverkOrgnr1 = Arrays.asList(
                testTapteDagsverk(1, 2018, 1, 10),
                testTapteDagsverk(1, 2018, 2, 10),
                testTapteDagsverk(1, 2018, 3, 10),
                testTapteDagsverk(1, 2018, 4, 10)
        );

        List<KvartalsvisTapteDagsverk> kvartalsvisTapteDagsverkOrgnr2 = Arrays.asList(
                testTapteDagsverk(2, 2018, 1, 7),
                testTapteDagsverk(2, 2018, 2, 7),
                testTapteDagsverk(2, 2018, 3, 7),
                testTapteDagsverk(2, 2018, 4, 7)
        );

        insertTapteDagsverk(orgnr1, kvartalsvisTapteDagsverkOrgnr1);
        insertTapteDagsverk(orgnr2, kvartalsvisTapteDagsverkOrgnr2);

        List<KvartalsvisTapteDagsverk> resultat = repository.hentTapteDagsverkFor4Kvartaler(Arrays.asList(
                new ÅrstallOgKvartal(2018, 1),
                new ÅrstallOgKvartal(2018, 2),
                new ÅrstallOgKvartal(2018, 3),
                new ÅrstallOgKvartal(2018, 4)
                ),
                orgnr1);

        assertThat(resultat).containsExactlyInAnyOrder(toArray(kvartalsvisTapteDagsverkOrgnr1));
    }

    @Test
    public void hentTapteDagsverkFor4Kvartaler__skal_bare_returnere_tapte_dagsverk_for_innsendte_kvartaler() {
        Orgnr orgnr = new Orgnr("111111111");

        List<KvartalsvisTapteDagsverk> riktigeKvartalsvisTapteDagsverk = Arrays.asList(
                testTapteDagsverk(100, 2018, 1, 10),
                testTapteDagsverk(100, 2018, 2, 10),
                testTapteDagsverk(100, 2018, 3, 10),
                testTapteDagsverk(100, 2018, 4, 10)
        );

        List<KvartalsvisTapteDagsverk> feilKvartalsvisTapteDagsverk = Arrays.asList(
                testTapteDagsverk(200, 2019, 1, 7),
                testTapteDagsverk(200, 2019, 2, 7),
                testTapteDagsverk(200, 2019, 3, 7),
                testTapteDagsverk(200, 2019, 4, 7)
        );

        insertTapteDagsverk(orgnr, riktigeKvartalsvisTapteDagsverk);
        insertTapteDagsverk(orgnr, feilKvartalsvisTapteDagsverk);

        List<KvartalsvisTapteDagsverk> resultat = repository.hentTapteDagsverkFor4Kvartaler(Arrays.asList(
                new ÅrstallOgKvartal(2018, 1),
                new ÅrstallOgKvartal(2018, 2),
                new ÅrstallOgKvartal(2018, 3),
                new ÅrstallOgKvartal(2018, 4)
                ),
                orgnr);

        assertThat(resultat).containsExactlyInAnyOrder(toArray(riktigeKvartalsvisTapteDagsverk));
    }

    private KvartalsvisTapteDagsverk[] toArray(List<KvartalsvisTapteDagsverk> kvartalsvisTapteDagsverk) {
        return kvartalsvisTapteDagsverk.toArray(new KvartalsvisTapteDagsverk[0]);
    }

    private void insertTapteDagsverk(Orgnr orgnr, List<KvartalsvisTapteDagsverk> kvartalsvisTapteDagsverkListe) {
        kvartalsvisTapteDagsverkListe.forEach(tapteDagsverk -> insertTapteDagsverk(orgnr, tapteDagsverk));
    }

    private void insertTapteDagsverk(Orgnr orgnr, KvartalsvisTapteDagsverk kvartalsvisTapteDagsverk) {
        insertStatistikkForVirksomhet(
                jdbcTemplate,
                orgnr,
                kvartalsvisTapteDagsverk.getÅrstall(),
                kvartalsvisTapteDagsverk.getKvartal(),
                10,
                kvartalsvisTapteDagsverk.getTapteDagsverk(),
                1000
        );
    }
}
