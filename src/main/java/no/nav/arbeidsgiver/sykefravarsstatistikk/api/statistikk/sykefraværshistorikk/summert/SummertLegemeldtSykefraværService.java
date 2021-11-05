package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjeprogram;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.LegemeldtSykefraværsprosent;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SummertSykefravær;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class SummertLegemeldtSykefraværService {
    private final SykefraværRepository sykefraværprosentRepository;
    private final Bransjeprogram bransjeprogram;

    public SummertLegemeldtSykefraværService(
            SykefraværRepository sykefraværprosentRepository,
            Bransjeprogram bransjeprogram
    ) {
        this.sykefraværprosentRepository = sykefraværprosentRepository;
        this.bransjeprogram = bransjeprogram;
    }

    static int antallKvartalerSomSkalSummeres = 4;

    public LegemeldtSykefraværsprosent hentLegemeldtSykefraværsprosent(
            Underenhet underenhet,
            ÅrstallOgKvartal sistePubliserteÅrstallOgKvartal
    ) {

        ÅrstallOgKvartal elsdteÅrstallOgKvartal =
                sistePubliserteÅrstallOgKvartal.minusKvartaler(antallKvartalerSomSkalSummeres - 1);


        List<UmaskertSykefraværForEttKvartal> sykefraværForEttKvartalListe =
                sykefraværprosentRepository.hentUmaskertSykefraværForEttKvartalListe(
                        underenhet,
                        elsdteÅrstallOgKvartal
                );

        SummertSykefravær summertSykefravær =
                SummertSykefravær.getSummertSykefravær(sykefraværForEttKvartalListe);

        // TODO: hva skjer hvis bedriften ikke har data i vår DB?
        if (!summertSykefravær.isErMaskert()) {
            return new LegemeldtSykefraværsprosent(
                    Statistikkategori.VIRKSOMHET,
                    underenhet.getNavn(),
                    summertSykefravær.getProsent()
            );
        }
        // TODO sjekk refaktoreringsmuligheter med hentSummertSykefraværshistorikkForBransjeEllerNæring
        Optional<Bransje> bransje = bransjeprogram.finnBransje(underenhet);
        if (bransje.isPresent()) {
            sykefraværForEttKvartalListe =
                    sykefraværprosentRepository.hentUmaskertSykefraværForEttKvartalListe(
                            bransje.get(), elsdteÅrstallOgKvartal
                    );
            summertSykefravær =
                    SummertSykefravær.getSummertSykefravær(
                            sykefraværForEttKvartalListe
                    );
            return new LegemeldtSykefraværsprosent(
                    Statistikkategori.BRANSJE,
                    bransje.get().getNavn(),
                    summertSykefravær.getProsent()
            );
        }

        return null;
    }
}
