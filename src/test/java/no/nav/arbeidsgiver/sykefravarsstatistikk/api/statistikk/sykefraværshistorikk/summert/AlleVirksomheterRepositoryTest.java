package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartalMedVarighet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAllStatistikkFraDatabase;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
@ActiveProfiles("db-test")
@DataJdbcTest
class AlleVirksomheterRepositoryTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private AlleVirksomheterRepository alleVirksomheterRepository;

    @BeforeEach
    void setUp() {
        alleVirksomheterRepository= new AlleVirksomheterRepository(jdbcTemplate);
        slettAllStatistikkFraDatabase(jdbcTemplate);
    }

    @AfterEach
    void tearDown() {
        slettAllStatistikkFraDatabase(jdbcTemplate);
    }

    @Test
    void hentSykefraværprosentAlleVirksomheterForEttKvartal() {
     /*   Underenhet barnehage = Underenhet.builder().orgnr(new Orgnr("999999999"))
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

        List<UmaskertSykefraværForEttKvartalMedVarighet> resultat = varighetRepository.hentSykefraværForEttKvartalMedVarighet(barnehage);

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
        ));*/
    }
}
