package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SummertSykefravær;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartalMedVarighet;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SummertSykefraværService {

    private final VarighetRepository varighetRepository;
    private final GraderingRepository graderingRepository;
    private final BransjeEllerNæringService bransjeEllerNæringService;

    public SummertSykefraværService(
            VarighetRepository varighetRepository,
            GraderingRepository graderingRepository,
            BransjeEllerNæringService bransjeEllerNæringService) {
        this.varighetRepository = varighetRepository;
        this.graderingRepository = graderingRepository;
        this.bransjeEllerNæringService = bransjeEllerNæringService;
    }

    public SummertSykefraværshistorikk hentSummertSykefraværshistorikkForBransjeEllerNæring(
            Underenhet underenhet,
            ÅrstallOgKvartal sistePubliserteÅrstallOgKvartal,
            int antallKvartalerSomSkalSummeres
    ) {
        if (antallKvartalerSomSkalSummeres < 1) {
            throw new IllegalArgumentException("Kan ikke summere færre enn ett kvartal");
        }

        List<UmaskertSykefraværForEttKvartalMedVarighet> sykefraværVarighet;
        Statistikkategori type;
        String label;
        List<UmaskertSykefraværForEttKvartal> sykefraværGradering;

        BransjeEllerNæring bransjeEllerNæring =
                bransjeEllerNæringService.skalHenteDataPåBransjeEllerNæringsnivå(underenhet.getNæringskode());

        if (!bransjeEllerNæring.isBransje()) {
            type = Statistikkategori.NÆRING;
            Næring næring = bransjeEllerNæring.getNæring();
            label = næring.getNavn();
            sykefraværVarighet = varighetRepository.hentSykefraværForEttKvartalMedVarighet(næring);
            sykefraværGradering = graderingRepository.hentSykefraværForEttKvartalMedGradering(næring);
        } else {
            type = Statistikkategori.BRANSJE;
            Bransje bransje = bransjeEllerNæring.getBransje();
            label = bransjeEllerNæring.getBransje().getNavn();
            sykefraværVarighet = varighetRepository.hentSykefraværForEttKvartalMedVarighet(bransje);
            sykefraværGradering = graderingRepository.hentSykefraværForEttKvartalMedGradering(bransje);
        }

        SummertKorttidsOgLangtidsfravær summertKorttidsOgLangtidsfravær =
                SummertKorttidsOgLangtidsfravær.getSummertKorttidsOgLangtidsfravær(
                        sistePubliserteÅrstallOgKvartal,
                        antallKvartalerSomSkalSummeres,
                        sykefraværVarighet
                );

        SummertSykefravær summertSykefraværGradering = getSummerSykefraværGradering(
                sistePubliserteÅrstallOgKvartal,
                antallKvartalerSomSkalSummeres,
                sykefraværGradering
        );

        return SummertSykefraværshistorikk.builder()
                .type(type)
                .label(label)
                .summertKorttidsOgLangtidsfravær(summertKorttidsOgLangtidsfravær)
                .summertGradertFravær(summertSykefraværGradering)
                .build();
    }

    public SummertSykefraværshistorikk hentSummertSykefraværshistorikk(
            Underenhet underenhet,
            ÅrstallOgKvartal sistePubliserteÅrstallOgKvartal,
            int antallKvartalerSomSkalSummeres
    ) {
        if (antallKvartalerSomSkalSummeres < 1) {
            throw new IllegalArgumentException("Kan ikke summere færre enn ett kvartal");
        }

        List<UmaskertSykefraværForEttKvartalMedVarighet> sykefraværVarighet =
                varighetRepository.hentSykefraværForEttKvartalMedVarighet(underenhet);
        List<UmaskertSykefraværForEttKvartal> sykefraværGradering =
                graderingRepository.hentSykefraværForEttKvartalMedGradering(underenhet);

        SummertKorttidsOgLangtidsfravær summertKorttidsOgLangtidsfravær =
                SummertKorttidsOgLangtidsfravær.getSummertKorttidsOgLangtidsfravær(
                        sistePubliserteÅrstallOgKvartal,
                        antallKvartalerSomSkalSummeres,
                        sykefraværVarighet
                );

        SummertSykefravær summertSykefraværGradering = getSummerSykefraværGradering(
                sistePubliserteÅrstallOgKvartal,
                antallKvartalerSomSkalSummeres,
                sykefraværGradering
        );

        return SummertSykefraværshistorikk.builder()
                .type(Statistikkategori.VIRKSOMHET)
                .label(underenhet.getNavn())
                .summertKorttidsOgLangtidsfravær(summertKorttidsOgLangtidsfravær)
                .summertGradertFravær(summertSykefraværGradering)
                .build();
    }


    protected SummertSykefravær getSummerSykefraværGradering(
            ÅrstallOgKvartal sistePubliserteÅrstallOgKvartal,
            int antallKvartalerSomSkalSummeres,
            List<UmaskertSykefraværForEttKvartal> sykefraværGradering
    ) {
        List<ÅrstallOgKvartal> kvartalerSomSkalSummeres = ÅrstallOgKvartal.range(
                sistePubliserteÅrstallOgKvartal.minusKvartaler(antallKvartalerSomSkalSummeres - 1),
                sistePubliserteÅrstallOgKvartal
        );

        List<UmaskertSykefraværForEttKvartal> gradertSykefraværForDeKvartaleneSomSkalSummeres =
                sykefraværGradering.stream()
                        .filter(v -> kvartalerSomSkalSummeres.contains(v.getÅrstallOgKvartal()))
                        .sorted(UmaskertSykefraværForEttKvartal::compareTo)
                        .collect(Collectors.toList());

        return SummertSykefravær.getSummertSykefravær(gradertSykefraværForDeKvartaleneSomSkalSummeres);
    }
}
