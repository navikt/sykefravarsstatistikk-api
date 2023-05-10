package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis.KvartalsvisSykefraværshistorikkService.SYKEFRAVÆRPROSENT_LAND_LABEL;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis.KvartalsvisSykefraværshistorikkService.uthentingMedFeilhåndteringOgTimeout;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Virksomhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjeprogram;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OffentligKvartalsvisSykefraværshistorikkService {

  private final KvartalsvisSykefraværshistorikkService kvartalsvisSykefraværshistorikkService;

  public OffentligKvartalsvisSykefraværshistorikkService(
      KvartalsvisSykefraværshistorikkService kvartalsvisSykefraværshistorikkService) {
    this.kvartalsvisSykefraværshistorikkService = kvartalsvisSykefraværshistorikkService;
  }

  public List<KvartalsvisSykefraværshistorikk> hentSykefraværshistorikkV1Offentlig(
          Virksomhet underenhet) {
    Optional<Bransje> bransje = Bransjeprogram.finnBransje(underenhet);
    boolean skalHenteDataPåNæring = bransje.isEmpty() || bransje.get().erDefinertPåTosiffernivå();

    return Stream.of(
            hentUtForNorge(),
            skalHenteDataPåNæring ? hentUtForNæring(underenhet) : hentUtForBransje(bransje.get()))
        .map(CompletableFuture::join)
        .collect(Collectors.toList());
  }

  private CompletableFuture<KvartalsvisSykefraværshistorikk> hentUtForNæring(
      Virksomhet underenhet) {
    return kvartalsvisSykefraværshistorikkService.uthentingAvSykefraværshistorikkNæring(underenhet);
  }

  private CompletableFuture<KvartalsvisSykefraværshistorikk> hentUtForBransje(Bransje bransje) {
    return uthentingMedFeilhåndteringOgTimeout(
        () -> kvartalsvisSykefraværshistorikkService.hentSykefraværshistorikkBransje(bransje),
        Statistikkategori.BRANSJE,
        bransje.getNavn());
  }

  private CompletableFuture<KvartalsvisSykefraværshistorikk> hentUtForNorge() {
    return uthentingMedFeilhåndteringOgTimeout(
        kvartalsvisSykefraværshistorikkService::hentSykefraværshistorikkLand,
        Statistikkategori.LAND,
        SYKEFRAVÆRPROSENT_LAND_LABEL);
  }
}
