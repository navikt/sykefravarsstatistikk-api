package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjeprogram;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.KlassifikasjonerRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SummertSykefravær;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartalMedVarighet;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SummertSykefraværService {

    private final VarighetRepository varighetRepository;
    private final GraderingRepository graderingRepository;
    private final Bransjeprogram bransjeprogram;
    private final KlassifikasjonerRepository klassifikasjonerRepository;


    public SummertSykefraværService(
            VarighetRepository varighetRepository,
            GraderingRepository graderingRepository,
            Bransjeprogram bransjeprogram,
            KlassifikasjonerRepository klassifikasjonerRepository
    ) {
        this.varighetRepository = varighetRepository;
        this.graderingRepository = graderingRepository;
        this.bransjeprogram = bransjeprogram;
        this.klassifikasjonerRepository = klassifikasjonerRepository;
    }

    public SummertSykefraværshistorikk hentSummertSykefraværshistorikkForBransjeEllerNæring(
            Underenhet underenhet,
            ÅrstallOgKvartal sistePubliserteÅrstallOgKvartal,
            int antallKvartalerSomSkalSummeres
    ) {
        if (antallKvartalerSomSkalSummeres < 1) {
            throw new IllegalArgumentException("Kan ikke summere færre enn ett kvartal");
        }

        Optional<Bransje> bransje = bransjeprogram.finnBransje(underenhet);
        boolean skalHenteDataPåNæring2Siffer =
                bransje.isEmpty()
                        || bransje.get().lengdePåNæringskoder() == 2;

        List<UmaskertSykefraværForEttKvartalMedVarighet> sykefraværVarighet;
        Statistikkategori type;
        String label;

        if (skalHenteDataPåNæring2Siffer) {
            type = Statistikkategori.NÆRING;
            Næring næring = klassifikasjonerRepository.hentNæring(underenhet.getNæringskode().hentNæringskode2Siffer());
            label = næring.getNavn();
            sykefraværVarighet = varighetRepository.hentSykefraværForEttKvartalMedVarighet(næring);
        } else {
            type = Statistikkategori.BRANSJE;
            label = bransje.get().getNavn();
            sykefraværVarighet = varighetRepository.hentSykefraværForEttKvartalMedVarighet(bransje.get());
        }

        SummertKorttidsOgLangtidsfravær summertKorttidsOgLangtidsfravær =
                SummertKorttidsOgLangtidsfravær.getSummertKorttidsOgLangtidsfravær(
                        sistePubliserteÅrstallOgKvartal,
                        antallKvartalerSomSkalSummeres,
                        sykefraværVarighet
                );

        return SummertSykefraværshistorikk.builder()
                .type(type)
                .label(label)
                .summertKorttidsOgLangtidsfravær(summertKorttidsOgLangtidsfravær)
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
