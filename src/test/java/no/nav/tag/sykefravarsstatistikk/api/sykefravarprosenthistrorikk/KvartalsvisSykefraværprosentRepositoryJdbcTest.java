package no.nav.tag.sykefravarsstatistikk.api.sykefravarprosenthistrorikk;

import no.nav.tag.sykefravarsstatistikk.api.TestUtils;
import no.nav.tag.sykefravarsstatistikk.api.domene.bransjeprogram.Bransje;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
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
import java.util.List;

import static no.nav.tag.sykefravarsstatistikk.api.TestUtils.slettAllStatistikkFraDatabase;
import static org.assertj.core.api.Java6Assertions.assertThat;

@ActiveProfiles("db-test")
@RunWith(SpringRunner.class)
@DataJdbcTest
public class KvartalsvisSykefraværprosentRepositoryJdbcTest {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private KvartalsvisSykefraværprosentRepository kvartalsvisSykefraværprosentRepository;

    @Before
    public void setUp() {
        kvartalsvisSykefraværprosentRepository = new KvartalsvisSykefraværprosentRepository(jdbcTemplate);
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
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(2019, 1, 10, 5, 100)
        );
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(2018, 4, 10, 6, 100)
        );

        List<KvartalsvisSykefraværprosent> resultat = kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentLand("Norge");
        assertThat(resultat.size()).isEqualTo(3);
        assertThat(resultat.get(0)).isEqualTo(new KvartalsvisSykefraværprosent(
                        new ÅrstallOgKvartal(2018, 4),
                        new Sykefraværprosent(
                                "Norge",
                                new BigDecimal(6),
                                new BigDecimal(100),
                                10
                        )
                )
        );
    }

    @Test
    public void hentSykefraværprosentSektor__skal_returnere_riktig_sykefravær() {
        Sektor statligForvaltning = new Sektor("1", "Statlig forvaltning");
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_sektor (sektor_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:sektor_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(statligForvaltning, 2019, 2, 10, 2, 100)
        );
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_sektor (sektor_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:sektor_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(statligForvaltning, 2019, 1, 10, 3, 100)
        );
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_sektor (sektor_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:sektor_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(statligForvaltning, 2018, 4, 10, 4, 100)
        );
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_sektor (sektor_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:sektor_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(new Sektor("2", "Kommunal forvaltning"), 2018, 4, 10, 5, 100)
        );

        List<KvartalsvisSykefraværprosent> resultat = kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentSektor
                (statligForvaltning);
        assertThat(resultat.size()).isEqualTo(3);
        assertThat(resultat.get(0)).isEqualTo(new KvartalsvisSykefraværprosent(
                        new ÅrstallOgKvartal(2018, 4),
                        new Sykefraværprosent(
                                "Statlig forvaltning",
                                new BigDecimal(4),
                                new BigDecimal(100),
                                10
                        )
                )
        );
    }

    @Test
    public void hentSykefraværprosentNæring__skal_returnere_riktig_sykefravær() {
        Næring produksjonAvKlær = new Næring("14", "Produksjon av klær");
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_naring (naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(produksjonAvKlær, 2019, 2, 10, 2, 100)
        );
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_naring (naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(produksjonAvKlær, 2019, 1, 10, 3, 100)
        );
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_naring (naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(new Næring("85", "Undervisning"), 2018, 4, 10, 5, 100)
        );

        List<KvartalsvisSykefraværprosent> resultat = kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentNæring(produksjonAvKlær);
        assertThat(resultat.size()).isEqualTo(2);
        assertThat(resultat.get(0)).isEqualTo(new KvartalsvisSykefraværprosent(
                        new ÅrstallOgKvartal(2019, 1),
                        new Sykefraværprosent(
                                produksjonAvKlær.getNavn(),
                                new BigDecimal(3),
                                new BigDecimal(100),
                                10
                        )
                )
        );
    }

    @Test
    public void hentSykefraværprosentBransje__skal_returnere_riktig_sykefravær() {
        Næring produksjonAvKlær = new Næring("14", "Produksjon av klær");
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_naring5siffer " +
                        "(naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(new Næring("87101", "Somatiske spesialsykehjem"), 2019, 2, 10, 5, 100)
        );

        jdbcTemplate.update(
                "insert into sykefravar_statistikk_naring5siffer " +
                        "(naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(new Næring("87101", "Somatiske spesialsykehjem"), 2019, 1, 10, 1, 100)
        );

        jdbcTemplate.update(
                "insert into sykefravar_statistikk_naring5siffer " +
                        "(naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(new Næring("87102", "Somatiske sykehjem"), 2019, 1, 10, 7, 100)
        );

        jdbcTemplate.update(
                "insert into sykefravar_statistikk_naring5siffer " +
                        "(naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(new Næring("87301", "Aldershjem"), 2018, 4, 10, 6, 100)
        );

        Bransje sykehjem = new Bransje("Sykehjem", "87101", "87102");
        List<KvartalsvisSykefraværprosent> resultat = kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentBransje(sykehjem);
        assertThat(resultat.size()).isEqualTo(2);
        assertThat(resultat.get(0)).isEqualTo(new KvartalsvisSykefraværprosent(
                        new ÅrstallOgKvartal(2019, 1),
                        new Sykefraværprosent(
                                sykehjem.getNavn(),
                                new BigDecimal(8),
                                new BigDecimal(200),
                                20
                        )
                )
        );
    }

    @Test
    public void hentSykefraværprosentLand__maskerer_sf_dersom_antall_ansatte_er_for_lav() {
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(2019, 2, 4, 4, 100)
        );
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(2019, 1, 10, 5, 100)
        );

        List<KvartalsvisSykefraværprosent> resultat = kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentLand("Norge");

        assertThat(resultat.size()).isEqualTo(2);

        KvartalsvisSykefraværprosent ikkeMaskertKvartalsvisSykefraværprosent = resultat.get(0);
        assertThat(ikkeMaskertKvartalsvisSykefraværprosent.isErMaskert()).isFalse();
        assertThat(ikkeMaskertKvartalsvisSykefraværprosent.getProsent().setScale(2)).isEqualTo(new BigDecimal(5).setScale(2));

        KvartalsvisSykefraværprosent maskertKvartalsvisSykefraværprosent = resultat.get(1);
        assertThat(maskertKvartalsvisSykefraværprosent.isErMaskert()).isTrue();
        assertThat(maskertKvartalsvisSykefraværprosent.getProsent()).isNull();
    }


    private MapSqlParameterSource parametre(int årstall, int kvartal, int antallPersoner, int tapteDagsverk, int muligeDagsverk) {
        return new MapSqlParameterSource()
                .addValue("arstall", årstall)
                .addValue("kvartal", kvartal)
                .addValue("antall_personer", antallPersoner)
                .addValue("tapte_dagsverk", tapteDagsverk)
                .addValue("mulige_dagsverk", muligeDagsverk);
    }

    private MapSqlParameterSource parametre(Sektor sektor, int årstall, int kvartal, int antallPersoner, int tapteDagsverk, int muligeDagsverk) {
        return new MapSqlParameterSource()
                .addValue("sektor_kode", sektor.getKode())
                .addValue("arstall", årstall)
                .addValue("kvartal", kvartal)
                .addValue("antall_personer", antallPersoner)
                .addValue("tapte_dagsverk", tapteDagsverk)
                .addValue("mulige_dagsverk", muligeDagsverk);
    }

    private MapSqlParameterSource parametre(Næring næring, int årstall, int kvartal, int antallPersoner, int tapteDagsverk, int muligeDagsverk) {
        return new MapSqlParameterSource()
                .addValue("naring_kode", næring.getKode())
                .addValue("arstall", årstall)
                .addValue("kvartal", kvartal)
                .addValue("antall_personer", antallPersoner)
                .addValue("tapte_dagsverk", tapteDagsverk)
                .addValue("mulige_dagsverk", muligeDagsverk);
    }
}
