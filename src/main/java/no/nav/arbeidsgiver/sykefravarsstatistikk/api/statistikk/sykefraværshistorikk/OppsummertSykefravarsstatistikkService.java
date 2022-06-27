package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Kvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.InnloggetBruker;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.math.BigDecimal.ZERO;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Konstanter.SISTE_PUBLISERTE_KVARTAL;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.VIRKSOMHET;

@Service
public class OppsummertSykefravarsstatistikkService {
  private final SykefraværRepository sykefraværprosentRepository;
  private final BransjeEllerNæringService bransjeEllerNæringService;
  private final TilgangskontrollService tilgangskontrollService;
  private final EnhetsregisteretClient enhetsregisteretClient;

  public OppsummertSykefravarsstatistikkService(
      SykefraværRepository sykefraværprosentRepository,
      BransjeEllerNæringService bransjeEllerNæringService,
      TilgangskontrollService tilgangskontrollService,
      EnhetsregisteretClient enhetsregisteretClient) {
    this.sykefraværprosentRepository = sykefraværprosentRepository;
    this.bransjeEllerNæringService = bransjeEllerNæringService;
    this.tilgangskontrollService = tilgangskontrollService;
    this.enhetsregisteretClient = enhetsregisteretClient;
  }

  public List<GenerellStatistikk> hentOppsummertStatistikk(String orgnr) {
    InnloggetBruker innloggetBruker =
        tilgangskontrollService.hentInnloggetBrukerForAlleRettigheter();
    tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
        new Orgnr(orgnr), innloggetBruker, "", "");

    Underenhet underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(new Orgnr(orgnr));

    BransjeEllerNæring bransjeEllerNæring =
        bransjeEllerNæringService.skalHenteDataPåBransjeEllerNæringsnivå(
            underenhet.getNæringskode());
    try {
      InnloggetBruker innloggetBrukerMedIARettigheter =
          tilgangskontrollService.hentInnloggetBruker();
      tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
          new Orgnr(orgnr), innloggetBrukerMedIARettigheter, "", "");

    } catch (TilgangskontrollException tilgangskontrollException) {

    }
    return null;
  }

  Map<Statistikkategori, GenerellStatistikk> hentOgBearbeidStatistikk(Underenhet underenhet) {
    Map<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> sykefraværSisteFemKvartaler =
        hentUmaskertStatistikkForSisteFemKvartaler(underenhet);

    Map<Statistikkategori, GenerellStatistikk> returverdi =
        Map.of(VIRKSOMHET, kalkulerSykefraværSisteFireKvartaler(sykefraværSisteFemKvartaler));
  }

  private Map<Statistikkategori, List<UmaskertSykefraværForEttKvartal>>
      hentUmaskertStatistikkForSisteFemKvartaler(Underenhet bedrift) {
    Kvartal eldsteKvartalViBryrOssOm = SISTE_PUBLISERTE_KVARTAL.minusKvartaler(4);
    return sykefraværprosentRepository.getAllTheThings(bedrift, eldsteKvartalViBryrOssOm);
  }

  private List<UmaskertSykefraværForEttKvartal> ekstraherSisteFireKvartaler(
      List<UmaskertSykefraværForEttKvartal> statistikk) {
    List<Kvartal> sisteFireKvartaler =
        IntStream.range(0, 4)
            .mapToObj(SISTE_PUBLISERTE_KVARTAL::minusKvartaler)
            .collect(Collectors.toList());

    return statistikk.stream()
        .filter(x -> sisteFireKvartaler.contains(x.getKvartal()))
        .collect(Collectors.toList());
  }

  private BigDecimal kalkulerSykefraværSisteFireKvartaler(
      List<UmaskertSykefraværForEttKvartal> statistikk) {

    return ekstraherSisteFireKvartaler(statistikk).stream()
        .map(
            e -> new Sykefravær(e.getMuligeDagsverk(), e.getTapteDagsverk(), e.getAntallPersoner()))
        .reduce(Sykefravær.NULLPUNKT, Sykefravær::plus)
        .kalkulerFraværsprosent();
  }
}

class Sykefravær {
  public BigDecimal mulige;
  public BigDecimal tapte;
  public int antallPersonerIGrunnlaget;

  Sykefravær(BigDecimal mulige, BigDecimal tapte, int antallPersonerIGrunnlaget) {
    this.mulige = mulige;
    this.tapte = tapte;
    this.antallPersonerIGrunnlaget = antallPersonerIGrunnlaget;
  }

  BigDecimal kalkulerFraværsprosent() {
    return tapte.divide(mulige, RoundingMode.HALF_UP);
  }

  Sykefravær plus(Sykefravær other) {
    return new Sykefravær(
        this.mulige.add(other.mulige),
        this.tapte.add(other.tapte),
        this.antallPersonerIGrunnlaget + other.antallPersonerIGrunnlaget);
  }

  static Sykefravær NULLPUNKT = new Sykefravær(ZERO, ZERO, 0);
}
