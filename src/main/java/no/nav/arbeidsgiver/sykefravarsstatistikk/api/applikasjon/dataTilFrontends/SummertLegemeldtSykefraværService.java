package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.dataTilFrontends;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.Virksomhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.BransjeEllerNæringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.LegemeldtSykefraværsprosent;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SummertSykefravær;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværRepository;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SummertLegemeldtSykefraværService {

  private final SykefraværRepository sykefraværprosentRepository;
  private final BransjeEllerNæringService bransjeEllerNæringService;

  public SummertLegemeldtSykefraværService(
      SykefraværRepository sykefraværprosentRepository,
      BransjeEllerNæringService bransjeEllerNæringService) {
    this.sykefraværprosentRepository = sykefraværprosentRepository;
    this.bransjeEllerNæringService = bransjeEllerNæringService;
  }

  public LegemeldtSykefraværsprosent hentLegemeldtSykefraværsprosent(
          Virksomhet underenhet, ÅrstallOgKvartal sistePubliserteÅrstallOgKvartal) {
    ÅrstallOgKvartal eldsteÅrstallOgKvartal = sistePubliserteÅrstallOgKvartal.minusKvartaler(3);

    List<UmaskertSykefraværForEttKvartal> sykefraværForEttKvartalListe =
        sykefraværprosentRepository.hentUmaskertSykefravær(underenhet, eldsteÅrstallOgKvartal);

    SummertSykefravær summertSykefravær =
        SummertSykefravær.getSummertSykefravær(sykefraværForEttKvartalListe);

    boolean erMaskert = summertSykefravær.isErMaskert();
    boolean harData =
        !(summertSykefravær.getKvartaler() == null || summertSykefravær.getKvartaler().isEmpty());

    if (harData && !erMaskert) {
      return new LegemeldtSykefraværsprosent(
          Statistikkategori.VIRKSOMHET, underenhet.getNavn(), summertSykefravær.getProsent());
    } else {
      return hentLegemeldtSykefraværsprosentUtenStatistikkForVirksomhet(underenhet,
          sistePubliserteÅrstallOgKvartal);
    }
  }

  public LegemeldtSykefraværsprosent hentLegemeldtSykefraværsprosentUtenStatistikkForVirksomhet(
          Virksomhet underenhet, ÅrstallOgKvartal sistePubliserteÅrstallOgKvartal) {
    ÅrstallOgKvartal eldsteÅrstallOgKvartal = sistePubliserteÅrstallOgKvartal.minusKvartaler(3);

    BransjeEllerNæring bransjeEllerNæring =
        bransjeEllerNæringService.bestemFraNæringskode(underenhet.getNæringskode());

    if (bransjeEllerNæring.isBransje()) {
      Bransje bransje = bransjeEllerNæring.getBransje();
      List<UmaskertSykefraværForEttKvartal> listeAvSykefraværForEttKvartalForBransje =
          sykefraværprosentRepository.hentUmaskertSykefravær(bransje, eldsteÅrstallOgKvartal);
      SummertSykefravær summertSykefraværBransje =
          SummertSykefravær.getSummertSykefravær(listeAvSykefraværForEttKvartalForBransje);

      return new LegemeldtSykefraværsprosent(
          bransjeEllerNæring.getStatistikkategori(),
          bransje.getNavn(),
          summertSykefraværBransje.getProsent());
    } else {
      Næring næring = bransjeEllerNæring.getNæring();
      List<UmaskertSykefraværForEttKvartal> listeAvSykefraværForEttKvartalForNæring =
          sykefraværprosentRepository.hentUmaskertSykefravær(næring, eldsteÅrstallOgKvartal);
      SummertSykefravær summertSykefraværNæring =
          SummertSykefravær.getSummertSykefravær(listeAvSykefraværForEttKvartalForNæring);

      return new LegemeldtSykefraværsprosent(
          bransjeEllerNæring.getStatistikkategori(),
          næring.getNavn(),
          summertSykefraværNæring.getProsent());
    }
  }
}
