package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.SlettOgOpprettResultat;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Sykefraværsstatistikk;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.SykefraværsstatistikkVirksomhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.BatchCreateSykefraværsstatistikkFunction;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.DeleteSykefraværsstatistikkFunction;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.StatistikkRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværsstatistikkIntegrasjonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Varighetskategori._1_DAG_TIL_7_DAGER;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
public class StatistikkRepositoryTest {

  @Mock NamedParameterJdbcTemplate jdbcTemplate;

  private StatistikkRepository statistikkRepository;

  @BeforeEach
  public void setUp() {
    statistikkRepository = new StatistikkRepository(jdbcTemplate);
  }

  @Test
  public void
      importStatistikk__skal_ikke_slette_eksisterende_statistikk_når_det_ikke_er_noe_data_å_importere() {

    SlettOgOpprettResultat resultat =
        statistikkRepository.importStatistikk(
            "Test stats",
            Collections.emptyList(),
            new ÅrstallOgKvartal(2019, 3),
            getIntegrasjonUtils());

    assertThat(resultat).isEqualTo(SlettOgOpprettResultat.tomtResultat());
  }

  @Test
  public void batchOpprett__deler_import_i_små_batch() {
    List<SykefraværsstatistikkVirksomhet> list = getSykefraværsstatistikkVirksomhetList(5);

    int resultat = statistikkRepository.batchOpprett(list, dummyUtils(), 2);

    assertThat(resultat).isEqualTo(5);
  }

  @Test
  public void batchOpprett__ikke_deler_dersom_batch_størrelse_er_større_enn_listen() {
    List<SykefraværsstatistikkVirksomhet> list = getSykefraværsstatistikkVirksomhetList(5);

    int resultat = statistikkRepository.batchOpprett(list, dummyUtils(), 1000);

    assertThat(resultat).isEqualTo(5);
  }

  private static List<SykefraværsstatistikkVirksomhet> getSykefraværsstatistikkVirksomhetList(
      int antallStatistikk) {
    List<SykefraværsstatistikkVirksomhet> list = new ArrayList<>();

    IntStream.range(0, antallStatistikk)
        .forEach(i -> list.add(sykefraværsstatistikkVirksomhet((2000 + i), 1)));

    return list;
  }

  private static SykefraværsstatistikkVirksomhet sykefraværsstatistikkVirksomhet(
      int årstall, int kvartal) {
    return new SykefraværsstatistikkVirksomhet(
        årstall,
        kvartal,
        "987654321",
        _1_DAG_TIL_7_DAGER.kode,
        RECTYPE_FOR_VIRKSOMHET,
        10,
        new BigDecimal(15),
        new BigDecimal(450));
  }

  private static SykefraværsstatistikkIntegrasjonUtils getIntegrasjonUtils() {
    return new SykefraværsstatistikkIntegrasjonUtils() {
      @Override
      public DeleteSykefraværsstatistikkFunction getDeleteFunction() {
        return årstallOgKvartal -> {
          throw new IllegalStateException("Skal ikke bruke delete funksjon");
        };
      }

      @Override
      public BatchCreateSykefraværsstatistikkFunction getBatchCreateFunction(
          List<? extends Sykefraværsstatistikk> list) {
        return null;
      }
    };
  }

  private static SykefraværsstatistikkIntegrasjonUtils dummyUtils() {
    return new SykefraværsstatistikkIntegrasjonUtils() {
      @Override
      public DeleteSykefraværsstatistikkFunction getDeleteFunction() {
        return null;
      }

      @Override
      public BatchCreateSykefraværsstatistikkFunction getBatchCreateFunction(
          List<? extends Sykefraværsstatistikk> list) {
        return () -> list.size();
      }
    };
  }
}
