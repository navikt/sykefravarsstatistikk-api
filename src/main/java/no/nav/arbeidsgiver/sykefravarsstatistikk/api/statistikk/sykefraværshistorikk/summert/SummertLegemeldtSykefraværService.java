package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.LegemeldtSykefraværsprosent;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SummertSykefravær;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;

import java.util.List;

public class SummertLegemeldtSykefraværService {
    private final SykefraværRepository sykefraværprosentRepository;

    public SummertLegemeldtSykefraværService(SykefraværRepository sykefraværprosentRepository) {
        this.sykefraværprosentRepository = sykefraværprosentRepository;
    }

    public LegemeldtSykefraværsprosent hentLegemeldtSykefraværsprosent(
            Underenhet underenhet,
            ÅrstallOgKvartal sistePubliserteÅrstallOgKvartal
    ) {
        int antallKvartalerSomSkalSummeres = 4;

        List<ÅrstallOgKvartal> kvartalerSomSkalSummeres = ÅrstallOgKvartal.range(
                sistePubliserteÅrstallOgKvartal.minusKvartaler(antallKvartalerSomSkalSummeres - 1),
                sistePubliserteÅrstallOgKvartal
        );

        ÅrstallOgKvartal elsdteÅrstallOgKvartal = kvartalerSomSkalSummeres.get(0);


        List<UmaskertSykefraværForEttKvartal> sykefraværForEttKvartalListe =
                sykefraværprosentRepository.hentUmaskertSykefraværForEttKvartalListe(
                        underenhet,
                        elsdteÅrstallOgKvartal
                );

        SummertSykefravær summertSykefravær = SummertSykefravær.getSummertSykefravær(sykefraværForEttKvartalListe);

        return new LegemeldtSykefraværsprosent(
                Statistikkategori.VIRKSOMHET,
                underenhet.getNavn(),
                summertSykefravær.getProsent()
        );
    }
}
