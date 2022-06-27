package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjeprogram;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Kvartal;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.AssertUtils.assertBigDecimalIsEqual;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SummertSykefraværServiceTest {


    @Mock
    private VarighetRepository varighetRepository;
    @Mock
    private GraderingRepository graderingRepository;
    @Mock
    private KlassifikasjonerRepository klassifikasjonerRepository;

    private SummertSykefraværService summertSykefraværService;

    private BransjeEllerNæringService bransjeEllerNæringService =
            new BransjeEllerNæringService(
                    new Bransjeprogram(),
                    klassifikasjonerRepository
            );

    private Underenhet barnehage;
    private static final Kvartal _2020_3 = new Kvartal(2020, 3);
    private static final Kvartal _2020_2 = new Kvartal(2020, 2);
    private static final Kvartal _2020_1 = new Kvartal(2020, 1);
    private static final Kvartal _2019_4 = new Kvartal(2019, 4);
    private static final Kvartal _2019_3 = new Kvartal(2019, 3);
    private static final Kvartal _2019_2 = new Kvartal(2019, 2);

    @BeforeEach
    public void setUp() {
        summertSykefraværService = new SummertSykefraværService(
                varighetRepository,
                graderingRepository,
                bransjeEllerNæringService
        );
        barnehage = Underenhet.builder().orgnr(new Orgnr("999999999"))
                .navn("test Barnehage")
                .næringskode(new Næringskode5Siffer("88911", "Barnehage"))
                .antallAnsatte(10)
                .overordnetEnhetOrgnr(new Orgnr("1111111111")).build();
    }

    @Test
    void getSummerSykefraværGradering() {
        List<UmaskertSykefraværForEttKvartal> listeAvGraderteSykemeldinger = new ArrayList<>();

        listeAvGraderteSykemeldinger.add(getGradertSykefravær(_2020_3, new BigDecimal(15.455), new BigDecimal(100), 5));
        listeAvGraderteSykemeldinger.add(getGradertSykefravær(_2020_2, new BigDecimal(22.500), new BigDecimal(200), 5));
        listeAvGraderteSykemeldinger.add(getGradertSykefravær(_2020_1, new BigDecimal(11), new BigDecimal(150), 5));
        listeAvGraderteSykemeldinger.add(getGradertSykefravær(_2019_4, new BigDecimal(18.200), new BigDecimal(150), 5));
        listeAvGraderteSykemeldinger.add(getGradertSykefravær(_2019_3, new BigDecimal(0), new BigDecimal(50), 5));

        SummertSykefravær summerSykefraværGradering = summertSykefraværService.getSummerSykefraværGradering(
                new Kvartal(2020, 4),
                4,
                listeAvGraderteSykemeldinger
        );

        assertThat(summerSykefraværGradering).isNotNull();
        List<Kvartal> expectedListeAvKvartaler = new ArrayList<>();
        expectedListeAvKvartaler.add(_2020_3);
        expectedListeAvKvartaler.add(_2020_2);
        expectedListeAvKvartaler.add(_2020_1);
        expectedListeAvKvartaler.sort(Kvartal::compareTo);
        assertThat(summerSykefraværGradering.getKvartaler()).isEqualTo(expectedListeAvKvartaler);
        assertBigDecimalIsEqual(summerSykefraværGradering.getProsent(), 10.9f);
        assertBigDecimalIsEqual(summerSykefraværGradering.getMuligeDagsverk(), 450f);
        assertBigDecimalIsEqual(summerSykefraværGradering.getTapteDagsverk(), 49f);
        assertThat(summerSykefraværGradering.isErMaskert()).isFalse();
    }

    @Test
    public void hentSummertKorttidsOgLangtidsfraværForBransjeEllerNæring__() {
        new UmaskertSykefraværForEttKvartalMedVarighet(
                new Kvartal(2020, 1),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(0),
                0,
                Varighetskategori._1_DAG_TIL_7_DAGER
        );
        List<UmaskertSykefraværForEttKvartalMedVarighet> sykefraværMed1Kvartal = Arrays.asList(
                new UmaskertSykefraværForEttKvartalMedVarighet(
                        new Kvartal(2020, 1),
                        BigDecimal.valueOf(5),
                        BigDecimal.valueOf(0),
                        0,
                        Varighetskategori._1_DAG_TIL_7_DAGER
                ),
                new UmaskertSykefraværForEttKvartalMedVarighet(
                        new Kvartal(2020, 1),
                        BigDecimal.valueOf(0),
                        BigDecimal.valueOf(10),
                        2,
                        Varighetskategori.TOTAL
                )
        );
        when(varighetRepository.hentSykefraværForEttKvartalMedVarighet(any(Bransje.class))).thenReturn(sykefraværMed1Kvartal);

        SummertSykefraværshistorikk summertSykefraværshistorikk =
                summertSykefraværService.hentSummertSykefraværshistorikkForBransjeEllerNæring(
                        barnehage,
                        new Kvartal(2020, 1),
                        4
                );

        assertThat(summertSykefraværshistorikk.getType()).isEqualTo(Statistikkategori.BRANSJE);
        assertThat(summertSykefraværshistorikk.getLabel()).isEqualTo("Barnehager");
        assertThat(summertSykefraværshistorikk.getSummertKorttidsOgLangtidsfravær().getSummertKorttidsfravær()).isNotNull();
        assertThat(summertSykefraværshistorikk.getSummertKorttidsOgLangtidsfravær().getSummertLangtidsfravær()).isNotNull();
    }


    private UmaskertSykefraværForEttKvartal getGradertSykefravær(
            Kvartal kvartal,
            BigDecimal tapteDagsverkGradertSykemelding,
            BigDecimal muligeDagsverk,
            int antallPersoner
    ) {
        return
                new UmaskertSykefraværForEttKvartal(
                        kvartal,
                        tapteDagsverkGradertSykemelding,
                        muligeDagsverk,
                        antallPersoner
                );
    }
}
