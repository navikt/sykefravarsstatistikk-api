package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAllStatistikkFraDatabase;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.VarighetTestUtils.leggTilStatisitkkNæringMedVarighetForTotalVarighetskategori;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.VarighetTestUtils.leggTilVirksomhetsstatistikkMedVarighet;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.ArbeidsmiljøportalenBransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartalMedVarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.VarighetRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfigForJdbcTesterConfig.class})
@DataJdbcTest(excludeAutoConfiguration = {TestDatabaseAutoConfiguration.class})
public class VarighetRepositoryJdbcTest {

  @Autowired private NamedParameterJdbcTemplate jdbcTemplate;

  private VarighetRepository varighetRepository;

  @BeforeEach
  public void setUp() {
    varighetRepository = new VarighetRepository(jdbcTemplate);
    slettAllStatistikkFraDatabase(jdbcTemplate);
  }

  @AfterEach
  public void tearDown() {
    slettAllStatistikkFraDatabase(jdbcTemplate);
  }

  @Test
  public void hentSykefraværForEttKvartalMedVarighet__skal_returnere_riktig_sykefravær() {
    Underenhet barnehage =
        new Underenhet(
            new Orgnr("999999999"),
            new Orgnr("1111111111"),
            "test Barnehage",
            new Næringskode5Siffer("88911", "Barnehage"),
            10);
    leggTilVirksomhetsstatistikkMedVarighet(
        jdbcTemplate,
        barnehage.getOrgnr().getVerdi(),
        new ÅrstallOgKvartal(2019, 2),
        Varighetskategori._1_DAG_TIL_7_DAGER,
        0,
        4,
        0);
    leggTilVirksomhetsstatistikkMedVarighet(
        jdbcTemplate,
        barnehage.getOrgnr().getVerdi(),
        new ÅrstallOgKvartal(2019, 2),
        Varighetskategori.TOTAL,
        6,
        0,
        100);

    List<UmaskertSykefraværForEttKvartalMedVarighet> resultat =
        varighetRepository.hentSykefraværForEttKvartalMedVarighet(barnehage);

    assertThat(resultat.size()).isEqualTo(2);
    assertThat(resultat.get(0))
        .isEqualTo(
            new UmaskertSykefraværForEttKvartalMedVarighet(
                new ÅrstallOgKvartal(2019, 2),
                new BigDecimal(4),
                new BigDecimal(0),
                0,
                Varighetskategori._1_DAG_TIL_7_DAGER));

    assertThat(resultat.get(1))
        .isEqualTo(
            new UmaskertSykefraværForEttKvartalMedVarighet(
                new ÅrstallOgKvartal(2019, 2),
                new BigDecimal(0),
                new BigDecimal(100),
                6,
                Varighetskategori.TOTAL));
  }

  @Test
  public void
      hentSykefraværForEttKvartalMedVarighet_for_næring__skal_returnere_riktig_sykefravær() {
    Næringskode5Siffer barnehager = new Næringskode5Siffer("88911", "Barnehager");

    leggTilStatisitkkNæringMedVarighetForTotalVarighetskategori(
        jdbcTemplate, barnehager, new ÅrstallOgKvartal(2019, 2), 1, 10);
    VarighetTestUtils.leggTilStatisitkkNæringMedVarighet(
        jdbcTemplate,
        barnehager,
        new ÅrstallOgKvartal(2019, 2),
        Varighetskategori._1_DAG_TIL_7_DAGER,
        4);

    List<UmaskertSykefraværForEttKvartalMedVarighet> resultat =
        varighetRepository.hentSykefraværForEttKvartalMedVarighet(
            new Næring(barnehager.getKode(), ""));

    assertThat(resultat.size()).isEqualTo(2);
    assertThat(resultat.get(0))
        .isEqualTo(
            new UmaskertSykefraværForEttKvartalMedVarighet(
                new ÅrstallOgKvartal(2019, 2),
                new BigDecimal(4),
                new BigDecimal(0),
                0,
                Varighetskategori._1_DAG_TIL_7_DAGER));

    assertThat(resultat.get(1))
        .isEqualTo(
            new UmaskertSykefraværForEttKvartalMedVarighet(
                new ÅrstallOgKvartal(2019, 2),
                new BigDecimal(0),
                new BigDecimal(10),
                1,
                Varighetskategori.TOTAL));
  }

  @Test
  public void
      hentSykefraværForEttKvartalMedVarighet_for_bransje__skal_returnere_riktig_sykefravær() {
    Næringskode5Siffer sykehus = new Næringskode5Siffer("86101", "Alminnelige somatiske sykehus");
    Næringskode5Siffer legetjeneste = new Næringskode5Siffer("86211", "Allmenn legetjeneste");

    leggTilStatisitkkNæringMedVarighetForTotalVarighetskategori(
        jdbcTemplate, sykehus, new ÅrstallOgKvartal(2019, 2), 1, 10);
    VarighetTestUtils.leggTilStatisitkkNæringMedVarighet(
        jdbcTemplate,
        sykehus,
        new ÅrstallOgKvartal(2019, 2),
        Varighetskategori._1_DAG_TIL_7_DAGER,
        4);
    leggTilStatisitkkNæringMedVarighetForTotalVarighetskategori(
        jdbcTemplate, legetjeneste, new ÅrstallOgKvartal(2019, 2), 5, 50);
    VarighetTestUtils.leggTilStatisitkkNæringMedVarighet(
        jdbcTemplate,
        legetjeneste,
        new ÅrstallOgKvartal(2019, 2),
        Varighetskategori._1_DAG_TIL_7_DAGER,
        8);

    List<UmaskertSykefraværForEttKvartalMedVarighet> resultat =
        varighetRepository.hentSykefraværForEttKvartalMedVarighet(
            new Bransje(
                ArbeidsmiljøportalenBransje.SYKEHUS,
                "Sykehus",
                "86101",
                "86102",
                "86104",
                "86105",
                "86106",
                "86107"));

    assertThat(resultat.size()).isEqualTo(2);
    assertThat(resultat.get(0))
        .isEqualTo(
            new UmaskertSykefraværForEttKvartalMedVarighet(
                new ÅrstallOgKvartal(2019, 2),
                new BigDecimal(4),
                new BigDecimal(0),
                0,
                Varighetskategori._1_DAG_TIL_7_DAGER));

    assertThat(resultat.get(1))
        .isEqualTo(
            new UmaskertSykefraværForEttKvartalMedVarighet(
                new ÅrstallOgKvartal(2019, 2),
                new BigDecimal(0),
                new BigDecimal(10),
                1,
                Varighetskategori.TOTAL));
  }
}
