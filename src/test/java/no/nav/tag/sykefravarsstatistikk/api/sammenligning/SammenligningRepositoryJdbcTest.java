package no.nav.tag.sykefravarsstatistikk.api.sammenligning;

import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Næringskode5Siffer;
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

import static no.nav.tag.sykefravarsstatistikk.api.TestUtils.*;
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
        cleanUpTestDb(jdbcTemplate);
    }

    @After
    public void tearDown() {
        cleanUpTestDb(jdbcTemplate);
    }

    @Test
    public void hentSykefraværprosentLand__skal_returnere_riktig_sykefravær() {
        jdbcTemplate.update(
                "insert into SYKEFRAVAR_STATISTIKK_LAND (arstall, kvartal, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:arstall, :kvartal, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(2019, 2, 4, 100)
        );

        Sykefraværprosent resultat = repository.hentSykefraværprosentLand(2019, 2);
        assertThat(resultat).isEqualTo(enSykefraværprosent("Norge", 4, 100));
    }

    @Test
    public void hentSykefraværprosentLand__skal_returnere_null_hvis_database_ikke_har_data() {
        assertThat(repository.hentSykefraværprosentLand(2020, 1)).isNull();
    }

    @Test
    public void hentSykefraværprosentSektor__skal_returnere_riktig_sykefravær() {
        jdbcTemplate.update(
                "insert into SYKEFRAVAR_STATISTIKK_SEKTOR (arstall, kvartal, tapte_dagsverk, mulige_dagsverk, sektor_kode) "
                        + "VALUES (:arstall, :kvartal, :tapte_dagsverk, :mulige_dagsverk, :sektor_kode)",
                parametre(2018, 1, 8, 23).addValue("sektor_kode", "1")
        );

        Sykefraværprosent resultat = repository.hentSykefraværprosentSektor(2018, 1, "1");
        assertThat(resultat).isEqualTo(enSykefraværprosent("Offentlig næringsvirksomhet", 8, 23));
    }

    @Test
    public void hentSykefraværprosentSektor__skal_returnere_null_hvis_database_ikke_har_data() {
        assertThat(repository.hentSykefraværprosentSektor(2020, 1, "0")).isNull();
    }

    @Test
    public void hentSykefraværprosentNæring__skal_returnere_riktig_sykefravær() {
        Næringskode5Siffer næringskode = new Næringskode5Siffer("74123", "Spesiell næring");

        insertNæringskode(næringskode);
        jdbcTemplate.update(
                "insert into SYKEFRAVAR_STATISTIKK_NARING (arstall, kvartal, tapte_dagsverk, mulige_dagsverk, naring_kode) "
                        + "VALUES (:arstall, :kvartal, :tapte_dagsverk, :mulige_dagsverk, :naring_kode)",
                parametre(2017, 3, 56, 2051)
                        .addValue("naring_kode", næringskode.hentNæringskode2Siffer())
        );

        Sykefraværprosent resultat = repository.hentSykefraværprosentNæring(2017, 3, næringskode);

        assertThat(resultat).isEqualTo(enSykefraværprosent(næringskode.getBeskrivelse(), 56, 2051));
    }

    @Test
    public void hentSykefraværprosentNæring__skal_returnere_null_hvis_database_ikke_har_data() {
        assertThat(repository.hentSykefraværprosentNæring(2020, 1, enNæringskode5Siffer())).isNull();
    }

    @Test
    public void hentSykefraværprosentVirksomhet__skal_returnere_riktig_sykefravær() {
        Underenhet virksomhet = enUnderenhet();
        jdbcTemplate.update(
                "insert into SYKEFRAVAR_STATISTIKK_VIRKSOMHET (arstall, kvartal, tapte_dagsverk, mulige_dagsverk, orgnr) "
                        + "VALUES (:arstall, :kvartal, :tapte_dagsverk, :mulige_dagsverk, :orgnr)",
                parametre(2016, 4, 31, 6234)
                        .addValue("orgnr", virksomhet.getOrgnr().getVerdi())
        );

        Sykefraværprosent resultat = repository.hentSykefraværprosentVirksomhet(2016, 4, virksomhet);

        assertThat(resultat).isEqualTo(enSykefraværprosent(virksomhet.getNavn(), 31, 6234));
    }

    @Test
    public void hentSykefraværprosentVirksomhet__skal_returnere_null_hvis_database_ikke_har_data() {
        assertThat(repository.hentSykefraværprosentVirksomhet(2020, 1, enUnderenhet())).isNull();
    }

    private void insertNæringskode(Næringskode5Siffer næringskode) {
        jdbcTemplate.update(
                "insert into NARINGSGRUPPE (kode, navn) VALUES ('0000', 'Næringsgruppenavn')",
                new MapSqlParameterSource()
        );

        jdbcTemplate.update(
                "insert into NARING (kode, naringsgruppe_kode, navn) VALUES (:kode, '0000', :navn)",
                new MapSqlParameterSource()
                        .addValue("kode", næringskode.hentNæringskode2Siffer())
                        .addValue("navn", næringskode.getBeskrivelse())
        );
    }

    private MapSqlParameterSource parametre(int årstall, int kvartal, int tapteDagsverk, int muligeDagsverk) {
        return new MapSqlParameterSource()
                .addValue("arstall", årstall)
                .addValue("kvartal", kvartal)
                .addValue("tapte_dagsverk", tapteDagsverk)
                .addValue("mulige_dagsverk", muligeDagsverk);
    }

    private static void cleanUpTestDb(NamedParameterJdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("DELETE FROM SYKEFRAVAR_STATISTIKK_VIRKSOMHET", new MapSqlParameterSource());
        jdbcTemplate.update("DELETE FROM SYKEFRAVAR_STATISTIKK_NARING", new MapSqlParameterSource());
        jdbcTemplate.update("DELETE FROM SYKEFRAVAR_STATISTIKK_SEKTOR", new MapSqlParameterSource());
        jdbcTemplate.update("DELETE FROM SYKEFRAVAR_STATISTIKK_LAND", new MapSqlParameterSource());
        jdbcTemplate.update("DELETE FROM NARING", new MapSqlParameterSource());
        jdbcTemplate.update("DELETE FROM NARINGSGRUPPE", new MapSqlParameterSource());
    }
}
