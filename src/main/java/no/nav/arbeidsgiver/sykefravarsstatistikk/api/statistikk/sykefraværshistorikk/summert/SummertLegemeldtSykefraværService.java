package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæringService;
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


    public SummertLegemeldtSykefraværService(
            SykefraværRepository sykefraværprosentRepository,
            BransjeEllerNæringService bransjeEllerNæringService
    ) {
        this.sykefraværprosentRepository = sykefraværprosentRepository;
        this.bransjeEllerNæringService = bransjeEllerNæringService;
    }

    static int antallKvartalerSomSkalSummeres = 4;

    public LegemeldtSykefraværsprosent hentLegemeldtSykefraværsprosent(
            Underenhet underenhet,
            ÅrstallOgKvartal sistePubliserteÅrstallOgKvartal,
            boolean harIARettigheter
    ) {
        ÅrstallOgKvartal eldsteÅrstallOgKvartal =
                sistePubliserteÅrstallOgKvartal.minusKvartaler(antallKvartalerSomSkalSummeres - 1);

        List<UmaskertSykefraværForEttKvartal> sykefraværForEttKvartalListe =
                sykefraværprosentRepository.hentUmaskertSykefraværForEttKvartalListe(
                        underenhet,
                        eldsteÅrstallOgKvartal
                );

        SummertSykefravær summertSykefravær =
                SummertSykefravær.getSummertSykefravær(sykefraværForEttKvartalListe);

        boolean erMaskert = summertSykefravær.isErMaskert();
        boolean harData = !(summertSykefravær.getKvartaler() == null || summertSykefravær.getKvartaler().isEmpty());

        if (harData && !erMaskert && harIARettigheter) {
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
            List<UmaskertSykefraværForEttKvartal> listeAvSykefraværForEttKvartalForBransje =
                    sykefraværprosentRepository.hentUmaskertSykefraværForEttKvartalListe(
                            bransje, eldsteÅrstallOgKvartal
                    );
            SummertSykefravær summertSykefraværBransje =
                    SummertSykefravær.getSummertSykefravær(listeAvSykefraværForEttKvartalForBransje);

            return new LegemeldtSykefraværsprosent(
                    bransjeEllerNæring.getStatistikkategori(),
                    bransje.getNavn(),
                    summertSykefraværBransje.getProsent()
            );
        } else {
            Næring næring = bransjeEllerNæring.getNæring();
            List<UmaskertSykefraværForEttKvartal> listeAvSykefraværForEttKvartalForNæring =
                    sykefraværprosentRepository.hentUmaskertSykefraværForEttKvartalListe(
                            næring,
                            eldsteÅrstallOgKvartal
                    );
            SummertSykefravær summertSykefraværNæring =
                    SummertSykefravær.getSummertSykefravær(listeAvSykefraværForEttKvartalForNæring);

            return new LegemeldtSykefraværsprosent(
                    bransjeEllerNæring.getStatistikkategori(),
                    næring.getNavn(),
                    summertSykefraværNæring.getProsent()
            );
        }
    }
}
