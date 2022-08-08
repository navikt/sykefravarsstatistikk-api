package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import java.util.List;
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
            ÅrstallOgKvartal sistePubliserteÅrstallOgKvartal
    ) {
        ÅrstallOgKvartal eldsteÅrstallOgKvartal =
                sistePubliserteÅrstallOgKvartal.minusKvartaler(3);

        List<UmaskertSykefraværForEttKvartal> sykefraværForEttKvartalListe =
                sykefraværprosentRepository.hentUmaskertSykefravær(
                        underenhet,
                        eldsteÅrstallOgKvartal
                );

        SummertSykefravær summertSykefravær =
                SummertSykefravær.getSummertSykefravær(sykefraværForEttKvartalListe);

        boolean erMaskert = summertSykefravær.isErMaskert();
        boolean harData = !(summertSykefravær.getKvartaler() == null
                || summertSykefravær.getKvartaler().isEmpty());

        if (harData && !erMaskert) {
            return new LegemeldtSykefraværsprosent(
                    Statistikkategori.VIRKSOMHET,
                    underenhet.getNavn(),
                    summertSykefravær.getProsent()
            );
        }
        BransjeEllerNæring bransjeEllerNæring =
                bransjeEllerNæringService.skalHenteDataPåBransjeEllerNæringsnivå(
                        underenhet.getNæringskode());

        if (bransjeEllerNæring.isBransje()) {
            Bransje bransje = bransjeEllerNæring.getBransje();
            List<UmaskertSykefraværForEttKvartal> listeAvSykefraværForEttKvartalForBransje =
                    sykefraværprosentRepository.hentUmaskertSykefravær(
                            bransje, eldsteÅrstallOgKvartal
                    );
            SummertSykefravær summertSykefraværBransje =
                    SummertSykefravær.getSummertSykefravær(
                            listeAvSykefraværForEttKvartalForBransje);

            return new LegemeldtSykefraværsprosent(
                    bransjeEllerNæring.getStatistikkategori(),
                    bransje.getNavn(),
                    summertSykefraværBransje.getProsent()
            );
        } else {
            Næring næring = bransjeEllerNæring.getNæring();
            List<UmaskertSykefraværForEttKvartal> listeAvSykefraværForEttKvartalForNæring =
                    sykefraværprosentRepository.hentUmaskertSykefravær(
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
