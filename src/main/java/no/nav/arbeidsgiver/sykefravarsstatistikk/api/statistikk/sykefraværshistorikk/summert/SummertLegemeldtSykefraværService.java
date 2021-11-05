package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjeprogram;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.LegemeldtSykefraværsprosent;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SummertSykefravær;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SummertLegemeldtSykefraværService {
    private final SykefraværRepository sykefraværprosentRepository;
    private final BransjeEllerNæringService bransjeEllerNæringService;

    private final Bransjeprogram bransjeprogram;

    public SummertLegemeldtSykefraværService(
            SykefraværRepository sykefraværprosentRepository,
            Bransjeprogram bransjeprogram,
            BransjeEllerNæringService bransjeEllerNæringService
    ) {
        this.sykefraværprosentRepository = sykefraværprosentRepository;
        this.bransjeprogram = bransjeprogram;
        this.bransjeEllerNæringService = bransjeEllerNæringService;
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
        boolean erMaskert = summertSykefravær.isErMaskert();
        boolean harData = !(summertSykefravær.getKvartaler() == null || summertSykefravær.getKvartaler().isEmpty());

        if (harData && !erMaskert) {
            return new LegemeldtSykefraværsprosent(
                    Statistikkategori.VIRKSOMHET,
                    underenhet.getNavn(),
                    summertSykefravær.getProsent()
            );
        }
        BransjeEllerNæring bransjeEllerNæring =
                bransjeEllerNæringService.getBransjeEllerNæring(underenhet.getNæringskode());

        if (bransjeEllerNæring.isBransje()) {
            Bransje bransje = bransjeEllerNæring.getBransje();
            List<UmaskertSykefraværForEttKvartal> sykefraværForEttKvartalBransje =
                    sykefraværprosentRepository.hentUmaskertSykefraværForEttKvartalListe(
                            bransje, elsdteÅrstallOgKvartal
                    );
            SummertSykefravær summertSykefraværBransje =
                    SummertSykefravær.getSummertSykefravær(sykefraværForEttKvartalBransje);

            return new LegemeldtSykefraværsprosent(
                    Statistikkategori.BRANSJE,
                    bransje.getNavn(),
                    summertSykefraværBransje.getProsent()
            );
        } else {
            Næring næring = bransjeEllerNæring.getNæring();
            List<UmaskertSykefraværForEttKvartal> sykefraværForEttKvartalNæring =
                    sykefraværprosentRepository.hentUmaskertSykefraværForEttKvartalListe(
                            næring, elsdteÅrstallOgKvartal
                    );
            SummertSykefravær summertSykefraværNæring =
                    SummertSykefravær.getSummertSykefravær(sykefraværForEttKvartalNæring);

            return new LegemeldtSykefraværsprosent(
                    Statistikkategori.BRANSJE,
                    næring.getNavn(),
                    summertSykefraværNæring.getProsent()
            );
        }
    }
}
