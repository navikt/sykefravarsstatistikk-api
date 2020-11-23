package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjeprogram;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.KlassifikasjonerRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartalMedVarighet;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class VarighetService {

    private final VarighetRepository varighetRepository;
    private final Bransjeprogram bransjeprogram;
    private final KlassifikasjonerRepository klassifikasjonerRepository;


    public VarighetService(
            VarighetRepository varighetRepository,
            Bransjeprogram bransjeprogram,
            KlassifikasjonerRepository klassifikasjonerRepository
    ) {
        this.varighetRepository = varighetRepository;
        this.bransjeprogram = bransjeprogram;
        this.klassifikasjonerRepository = klassifikasjonerRepository;
    }

    public SummertSykefraværshistorikk hentSummertKorttidsOgLangtidsfraværForBransjeEllerNæring(
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

    public SummertKorttidsOgLangtidsfravær hentSummertKorttidsOgLangtidsfravær(
            Underenhet underenhet,
            ÅrstallOgKvartal sistePubliserteÅrstallOgKvartal,
            int antallKvartalerSomSkalSummeres
    ) {
        if (antallKvartalerSomSkalSummeres < 1) {
            throw new IllegalArgumentException("Kan ikke summere færre enn ett kvartal");
        }

        List<UmaskertSykefraværForEttKvartalMedVarighet> sykefraværVarighet =
                varighetRepository.hentSykefraværForEttKvartalMedVarighet(underenhet);

        return SummertKorttidsOgLangtidsfravær.getSummertKorttidsOgLangtidsfravær(
                sistePubliserteÅrstallOgKvartal,
                antallKvartalerSomSkalSummeres,
                sykefraværVarighet
        );
    }
}
