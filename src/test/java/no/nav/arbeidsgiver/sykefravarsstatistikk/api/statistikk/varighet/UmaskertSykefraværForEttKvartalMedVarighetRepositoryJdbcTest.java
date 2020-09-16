package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.varighet;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.VarighetRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartalMedVarighet;
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

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAllStatistikkFraDatabase;
import static org.assertj.core.api.Java6Assertions.assertThat;

@ActiveProfiles("db-test")
@RunWith(SpringRunner.class)
@DataJdbcTest
public class UmaskertSykefraværForEttKvartalMedVarighetRepositoryJdbcTest {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private VarighetRepository varighetRepository;

    @Before
    public void setUp() {
        varighetRepository = new VarighetRepository(jdbcTemplate);
        slettAllStatistikkFraDatabase(jdbcTemplate);
    }

    @After
    public void tearDown() {
        slettAllStatistikkFraDatabase(jdbcTemplate);
    }

    @Test
    public void hentSykefraværprosentMedVarighet__skal_returnere_riktig_sykefravær() {
        Underenhet barnehage = Underenhet.builder().orgnr(new Orgnr("999999999"))
                .navn("test Barnehage")
                .næringskode(new Næringskode5Siffer("88911", "Barnehage"))
                .antallAnsatte(10)
                .overordnetEnhetOrgnr(new Orgnr("1111111111")).build();
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_virksomhet (arstall, kvartal, orgnr, varighet, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:arstall, :kvartal, :orgnr, :varighet, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(2019, 2, barnehage.getOrgnr().getVerdi(), "A", 0, 4, 0)
        );
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_virksomhet (arstall, kvartal, orgnr, varighet, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:arstall, :kvartal, :orgnr, :varighet, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(2019, 2, barnehage.getOrgnr().getVerdi(), "X", 6, 0, 100)
        );

        List<UmaskertSykefraværForEttKvartalMedVarighet> resultat = varighetRepository.hentKvartalsvisSykefraværMedVarighet(barnehage);

        assertThat(resultat.size()).isEqualTo(2);
        assertThat(resultat.get(0)).isEqualTo(new UmaskertSykefraværForEttKvartalMedVarighet(
                new ÅrstallOgKvartal(2019, 2),
                new BigDecimal(4),
                new BigDecimal(0),
                0,
                Varighetskategori._1_DAG_TIL_7_DAGER
        ));

        assertThat(resultat.get(1)).isEqualTo(new UmaskertSykefraværForEttKvartalMedVarighet(
                new ÅrstallOgKvartal(2019, 2),
                new BigDecimal(0),
                new BigDecimal(100),
                6,
                Varighetskategori.TOTAL
        ));
    }


    private MapSqlParameterSource parametre(int årstall, int kvartal, String orgnr, String varighet, int antallPersoner, int tapteDagsverk, int muligeDagsverk) {
        return new MapSqlParameterSource()
                .addValue("arstall", årstall)
                .addValue("kvartal", kvartal)
                .addValue("orgnr", orgnr)
                .addValue("varighet", varighet)
                .addValue("antall_personer", antallPersoner)
                .addValue("tapte_dagsverk", tapteDagsverk)
                .addValue("mulige_dagsverk", muligeDagsverk);
    }
}
