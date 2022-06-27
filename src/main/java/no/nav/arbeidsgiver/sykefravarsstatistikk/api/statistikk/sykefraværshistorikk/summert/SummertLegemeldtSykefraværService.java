package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Kvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.LegemeldtSykefraværsprosent;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SummertSykefravær;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import org.springframework.stereotype.Component;

import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Konstanter.antallKvartalerSomSkalSummeres;

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


    public LegemeldtSykefraværsprosent hentLegemeldtSykefraværsprosent(
            Underenhet underenhet,
            Kvartal sistePubliserteKvartal
    ) {
        Kvartal eldsteKvartal =
                sistePubliserteKvartal.minusKvartaler(antallKvartalerSomSkalSummeres - 1);

        List<UmaskertSykefraværForEttKvartal> sykefraværForEttKvartalListe =
                sykefraværprosentRepository.hentUmaskertSykefraværForEttKvartalListe(
                        underenhet,
                        eldsteKvartal
                );

        SummertSykefravær summertSykefravær =
                SummertSykefravær.getSummertSykefravær(sykefraværForEttKvartalListe);

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
                bransjeEllerNæringService.skalHenteDataPåBransjeEllerNæringsnivå(underenhet.getNæringskode());

        if (bransjeEllerNæring.isBransje()) {
            Bransje bransje = bransjeEllerNæring.getBransje();
            List<UmaskertSykefraværForEttKvartal> listeAvSykefraværForEttKvartalForBransje =
                    sykefraværprosentRepository.hentUmaskertSykefraværForEttKvartalListe(
                            bransje, eldsteKvartal
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
                            eldsteKvartal
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
