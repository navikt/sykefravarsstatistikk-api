package no.nav.tag.sykefravarsstatistikk.api.sammenligning;

import no.nav.tag.sykefravarsstatistikk.api.domene.bransjeprogram.Bransje;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static no.nav.tag.sykefravarsstatistikk.api.TestData.*;
import static no.nav.tag.sykefravarsstatistikk.api.TestUtils.insertStatistikkForVirksomhet;
import static no.nav.tag.sykefravarsstatistikk.api.TestUtils.slettAllStatistikkFraDatabase;
import static no.nav.tag.sykefravarsstatistikk.api.sammenligning.SammenligningRepository.NORGE;
import static org.assertj.core.api.Java6Assertions.assertThat;

@ActiveProfiles("db-test")
@RunWith(SpringRunner.class)
@DataJdbcTest
public class SammenligningRepositoryJdbcTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private SammenligningRepository repository;

    @Before
    public void setUp() {
        repository = new SammenligningRepository(jdbcTemplate);
        slettAllStatistikkFraDatabase(jdbcTemplate);
    }

    @After
    public void tearDown() {
        slettAllStatistikkFraDatabase(jdbcTemplate);
    }

    @Test
    public void hentSykefraværprosentLand__skal_returnere_riktig_sykefravær() {
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(2019, 2, 10, 4, 100)
        );

        Sykefraværprosent resultat = repository.hentSykefraværprosentLand(2019, 2);
        assertThat(resultat).isEqualTo(enSykefraværprosent("Norge", 4, 100, 10));
    }

    @Test
    public void hentSykefraværprosentLand__skal_returnere_null_hvis_database_ikke_har_data() {
        assertThat(repository.hentSykefraværprosentLand(2020, 1))
                .isEqualTo(Sykefraværprosent.tomSykefraværprosent(NORGE));
    }

    @Test
    public void hentSykefraværprosentSektor__skal_returnere_riktig_sykefravær() {
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_sektor " +
                        "(sektor_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES ( :sektor_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(2018, 1, 5, 8, 23).addValue("sektor_kode", "1")
        );
        Sektor sektor = enSektor();
        Sykefraværprosent resultat = repository.hentSykefraværprosentSektor(2018, 1, sektor);
        assertThat(resultat).isEqualTo(enSykefraværprosent(sektor.getNavn(), 8, 23, 5));
    }

    @Test
    public void hentSykefraværprosentSektor__skal_returnere_null_hvis_database_ikke_har_data() {
        assertThat(repository.hentSykefraværprosentSektor(2020, 1, enSektor()))
                .isEqualTo(Sykefraværprosent.tomSykefraværprosent(enSektor().getNavn()));
    }

    @Test
    public void hentSykefraværprosentNæring__skal_returnere_riktig_sykefravær() {
        Næring næring = new Næring("74123", "Spesiell næring");

        insertStatistikkForNæring2siffer("74123", 2017, 3, 10, 56, 2051);

        Sykefraværprosent resultat = repository.hentSykefraværprosentNæring(2017, 3, næring);

        assertThat(resultat).isEqualTo(enSykefraværprosent(næring.getNavn(), 56, 2051, 10));
    }

    @Test
    public void hentSykefraværprosentNæring__skal_returnere_null_hvis_database_ikke_har_data() {
        assertThat(repository.hentSykefraværprosentNæring(2020, 1, enNæring()))
                .isEqualTo(Sykefraværprosent.tomSykefraværprosent(enNæring().getNavn()));
    }

    @Test
    public void hentSykefraværprosentVirksomhet__skal_returnere_riktig_sykefravær() {
        Underenhet virksomhet = enUnderenhet();
        insertStatistikkForVirksomhet(
                jdbcTemplate, virksomhet.getOrgnr(), 2016, 4, 10, new BigDecimal(31), 6234
        );

        Sykefraværprosent resultat = repository.hentSykefraværprosentVirksomhet(2016, 4, virksomhet);

        assertThat(resultat).isEqualTo(enSykefraværprosent(virksomhet.getNavn(), 31, 6234, 10));
    }

    @Test
    public void hentSykefraværprosentVirksomhet__skal_returnere_null_hvis_database_ikke_har_data() {
        assertThat(repository.hentSykefraværprosentVirksomhet(2020, 1, enUnderenhet()))
                .isEqualTo(Sykefraværprosent.tomSykefraværprosent(enUnderenhet().getNavn()));
    }

    @Test
    public void hentSykefraværprosentBransje__skal_summere_opp_næringer_på_2siffernivå_hvis_bransjen_spesifiseres_av_2sifferkoder() {
        Bransje bransje = new Bransje("bransje", "01", "02");
        insertStatistikkForNæring2siffer("01", 2017, 3, 10, 10, 1000);
        insertStatistikkForNæring2siffer("02", 2017, 3, 20, 30, 2000);

        Sykefraværprosent resultat = repository.hentSykefraværprosentBransje(2017, 3, bransje);

        assertThat(resultat).isEqualTo(enSykefraværprosent(bransje.getNavn(), 40, 3000, 30));
    }

    @Test
    public void hentSykefraværprosentBransje__skal_summere_opp_næringer_på_5siffernivå_hvis_bransjen_spesifiseres_av_5sifferkoder() {
        Bransje bransje = new Bransje("bransje", "11111", "22222");
        insertStatistikkForNæring5siffer("11111", 2017, 3, 10, 10, 1000);
        insertStatistikkForNæring5siffer("22222", 2017, 3, 20, 30, 2000);

        Sykefraværprosent resultat = repository.hentSykefraværprosentBransje(2017, 3, bransje);

        assertThat(resultat).isEqualTo(enSykefraværprosent(bransje.getNavn(), 40, 3000, 30));
    }

    private void insertStatistikkForNæring2siffer(String næringskode2siffer, int årstall, int kvartal, int antallPersoner, int tapteDagsverk, int muligeDagsverk) {
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_naring " +
                        "(naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(årstall, kvartal, antallPersoner, tapteDagsverk, muligeDagsverk)
                        .addValue("naring_kode", næringskode2siffer)
        );
    }

    private void insertStatistikkForNæring5siffer(String næringskode5siffer, int årstall, int kvartal, int antallPersoner, int tapteDagsverk, int muligeDagsverk) {
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_naring5siffer " +
                        "(naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(årstall, kvartal, antallPersoner, tapteDagsverk, muligeDagsverk)
                        .addValue("naring_kode", næringskode5siffer)
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
}
