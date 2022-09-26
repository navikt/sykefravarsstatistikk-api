package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.AssertUtils.assertBigDecimalIsEqual;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjeprogram;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.api.PubliseringsdatoerService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.KlassifikasjonerRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SummertSykefravær;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartalMedVarighet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SummertSykefraværServiceTest {


  @Mock
  private VarighetRepository varighetRepository;
  @Mock
  private GraderingRepository graderingRepository;
  @Mock
  private KlassifikasjonerRepository klassifikasjonerRepository;
  @Mock
  private PubliseringsdatoerService publiseringsdatoerService;


  private SummertSykefraværService summertSykefraværService;

  private BransjeEllerNæringService bransjeEllerNæringService =
      new BransjeEllerNæringService(
          new Bransjeprogram(),
          klassifikasjonerRepository
      );

  private Underenhet barnehage;
  private static final ÅrstallOgKvartal _2020_3 = new ÅrstallOgKvartal(2020, 3);
  private static final ÅrstallOgKvartal _2020_2 = new ÅrstallOgKvartal(2020, 2);
  private static final ÅrstallOgKvartal _2020_1 = new ÅrstallOgKvartal(2020, 1);
  private static final ÅrstallOgKvartal _2019_4 = new ÅrstallOgKvartal(2019, 4);
  private static final ÅrstallOgKvartal _2019_3 = new ÅrstallOgKvartal(2019, 3);
  private static final ÅrstallOgKvartal _2019_2 = new ÅrstallOgKvartal(2019, 2);

  @BeforeEach
  public void setUp() {
    summertSykefraværService = new SummertSykefraværService(
        varighetRepository,
        graderingRepository,
        bransjeEllerNæringService,
        publiseringsdatoerService);
    barnehage = Underenhet.builder().orgnr(new Orgnr("999999999"))
        .navn("test Barnehage")
        .næringskode(new Næringskode5Siffer("88911", "Barnehage"))
        .antallAnsatte(10)
        .overordnetEnhetOrgnr(new Orgnr("1111111111")).build();
    when(publiseringsdatoerService.hentSistePubliserteKvartal())
        .thenReturn(new ÅrstallOgKvartal(2020, 1));
  }

  @Test
  void getSummerSykefraværGradering() {
    List<UmaskertSykefraværForEttKvartal> listeAvGraderteSykemeldinger = new ArrayList<>();

    listeAvGraderteSykemeldinger.add(
        getGradertSykefravær(_2020_3, new BigDecimal(15.455), new BigDecimal(100), 5));
    listeAvGraderteSykemeldinger.add(
        getGradertSykefravær(_2020_2, new BigDecimal(22.500), new BigDecimal(200), 5));
    listeAvGraderteSykemeldinger.add(
        getGradertSykefravær(_2020_1, new BigDecimal(11), new BigDecimal(150), 5));
    listeAvGraderteSykemeldinger.add(
        getGradertSykefravær(_2019_4, new BigDecimal(18.200), new BigDecimal(150), 5));
    listeAvGraderteSykemeldinger.add(
        getGradertSykefravær(_2019_3, new BigDecimal(0), new BigDecimal(50), 5));

    SummertSykefravær summerSykefraværGradering = summertSykefraværService.getSummerSykefraværGradering(
        new ÅrstallOgKvartal(2020, 4),
        4,
        listeAvGraderteSykemeldinger
    );

    assertThat(summerSykefraværGradering).isNotNull();
    List<ÅrstallOgKvartal> expectedListeAvKvartaler = new ArrayList<>();
    expectedListeAvKvartaler.add(_2020_3);
    expectedListeAvKvartaler.add(_2020_2);
    expectedListeAvKvartaler.add(_2020_1);
    expectedListeAvKvartaler.sort(ÅrstallOgKvartal::compareTo);
    assertThat(summerSykefraværGradering.getKvartaler()).isEqualTo(expectedListeAvKvartaler);
    assertBigDecimalIsEqual(summerSykefraværGradering.getProsent(), 10.9f);
    assertBigDecimalIsEqual(summerSykefraværGradering.getMuligeDagsverk(), 450f);
    assertBigDecimalIsEqual(summerSykefraværGradering.getTapteDagsverk(), 49f);
    assertThat(summerSykefraværGradering.isErMaskert()).isFalse();
  }

  @Test
  public void hentSummertKorttidsOgLangtidsfraværForBransjeEllerNæring__() {
    new UmaskertSykefraværForEttKvartalMedVarighet(
        new ÅrstallOgKvartal(2020, 1),
        BigDecimal.valueOf(5),
        BigDecimal.valueOf(0),
        0,
        Varighetskategori._1_DAG_TIL_7_DAGER
    );
    List<UmaskertSykefraværForEttKvartalMedVarighet> sykefraværMed1Kvartal = Arrays.asList(
        new UmaskertSykefraværForEttKvartalMedVarighet(
            new ÅrstallOgKvartal(2020, 1),
            BigDecimal.valueOf(5),
            BigDecimal.valueOf(0),
            0,
            Varighetskategori._1_DAG_TIL_7_DAGER
        ),
        new UmaskertSykefraværForEttKvartalMedVarighet(
            new ÅrstallOgKvartal(2020, 1),
            BigDecimal.valueOf(0),
            BigDecimal.valueOf(10),
            2,
            Varighetskategori.TOTAL
        )
    );
    when(varighetRepository.hentSykefraværForEttKvartalMedVarighet(any(Bransje.class))).thenReturn(
        sykefraværMed1Kvartal);

    SummertSykefraværshistorikk summertSykefraværshistorikk =
        summertSykefraværService.hentSummertSykefraværshistorikkForBransjeEllerNæring(
            barnehage,
            4
        );

    assertThat(summertSykefraværshistorikk.getType()).isEqualTo(Statistikkategori.BRANSJE);
    assertThat(summertSykefraværshistorikk.getLabel()).isEqualTo("Barnehager");
    assertThat(summertSykefraværshistorikk.getSummertKorttidsOgLangtidsfravær()
        .getSummertKorttidsfravær()).isNotNull();
    assertThat(summertSykefraværshistorikk.getSummertKorttidsOgLangtidsfravær()
        .getSummertLangtidsfravær()).isNotNull();
  }


  private UmaskertSykefraværForEttKvartal getGradertSykefravær(
      ÅrstallOgKvartal årstallOgKvartal,
      BigDecimal tapteDagsverkGradertSykemelding,
      BigDecimal muligeDagsverk,
      int antallPersoner
  ) {
    return
        new UmaskertSykefraværForEttKvartal(
            årstallOgKvartal,
            tapteDagsverkGradertSykemelding,
            muligeDagsverk,
            antallPersoner
        );
  }
}
