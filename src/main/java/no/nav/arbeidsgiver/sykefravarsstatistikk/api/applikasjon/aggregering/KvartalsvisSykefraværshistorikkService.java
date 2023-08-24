package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregering;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.Bransjeprogram;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.KlassifikasjonerRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.KvartalsvisSykefraværRepository;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;

@Component
@Slf4j
public class KvartalsvisSykefraværshistorikkService {
  public static final int TIMEOUT_UTHENTING_FRA_DB_I_SEKUNDER = 3;
  public static final String SYKEFRAVÆRPROSENT_LAND_LABEL = "Norge";

  private final KvartalsvisSykefraværRepository kvartalsvisSykefraværprosentRepository;
  private final KlassifikasjonerRepository klassifikasjonerRepository;

  public KvartalsvisSykefraværshistorikkService(
      KvartalsvisSykefraværRepository kvartalsvisSykefraværprosentRepository,
      KlassifikasjonerRepository klassifikasjonerRepository) {
    this.kvartalsvisSykefraværprosentRepository = kvartalsvisSykefraværprosentRepository;
    this.klassifikasjonerRepository = klassifikasjonerRepository;
  }

  public List<KvartalsvisSykefraværshistorikk> hentSykefraværshistorikk(
          Virksomhet underenhet, Sektor sektor) {
    Optional<Bransje> bransje = Bransjeprogram.finnBransje(underenhet);
    boolean skalHenteDataPåNæring = bransje.isEmpty() || bransje.get().erDefinertPåTosiffernivå();

    return
        Stream.of(
                uthentingMedFeilhåndteringOgTimeout(
                        this::hentSykefraværshistorikkLand,
                    Statistikkategori.LAND,
                    SYKEFRAVÆRPROSENT_LAND_LABEL),
                uthentingMedFeilhåndteringOgTimeout(
                    () -> hentSykefraværshistorikkSektor(sektor),
                    Statistikkategori.SEKTOR,
                        sektor.getDisplaystring()),
                skalHenteDataPåNæring
                    ? uthentingAvSykefraværshistorikkNæring(underenhet)
                    : uthentingMedFeilhåndteringOgTimeout(
                        () -> hentSykefraværshistorikkBransje(bransje.get()),
                        Statistikkategori.BRANSJE,
                        bransje.get().getNavn()),
                uthentingMedFeilhåndteringOgTimeout(
                    () ->
                        hentSykefraværshistorikkVirksomhet(
                            underenhet, Statistikkategori.VIRKSOMHET),
                    Statistikkategori.VIRKSOMHET,
                    underenhet.getNavn()))
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
  }

  public List<KvartalsvisSykefraværshistorikk> hentSykefraværshistorikk(
          Virksomhet underenhet, OverordnetEnhet overordnetEnhet) {

    KvartalsvisSykefraværshistorikk historikkForOverordnetEnhet =
        uthentingMedFeilhåndteringOgTimeout(
                () ->
                    hentSykefraværshistorikkVirksomhet(
                        overordnetEnhet, Statistikkategori.OVERORDNET_ENHET),
                Statistikkategori.OVERORDNET_ENHET,
                underenhet.getNavn())
            .join();

    List<KvartalsvisSykefraværshistorikk> kvartalsvisSykefraværshistorikkListe =
        hentSykefraværshistorikk(underenhet, overordnetEnhet.getSektor());
    kvartalsvisSykefraværshistorikkListe.add(historikkForOverordnetEnhet);

    return kvartalsvisSykefraværshistorikkListe;
  }

  protected KvartalsvisSykefraværshistorikk hentSykefraværshistorikkLand() {
    return new KvartalsvisSykefraværshistorikk(
        Statistikkategori.LAND,
        SYKEFRAVÆRPROSENT_LAND_LABEL,
        kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentLand());
  }

  private KvartalsvisSykefraværshistorikk hentSykefraværshistorikkSektor(Sektor ssbSektor) {
    return new KvartalsvisSykefraværshistorikk(
        Statistikkategori.SEKTOR,
            ssbSektor.getDisplaystring(),
        kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentSektor(ssbSektor));
  }

  private KvartalsvisSykefraværshistorikk hentSykefraværshistorikkNæring(Næring næring) {
    return new KvartalsvisSykefraværshistorikk(
        Statistikkategori.NÆRING,
        næring.getNavn(),
        kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentNæring(næring));
  }

  protected KvartalsvisSykefraværshistorikk hentSykefraværshistorikkBransje(Bransje bransje) {
    return new KvartalsvisSykefraværshistorikk(
        Statistikkategori.BRANSJE,
        bransje.getNavn(),
        kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentBransje(bransje));
  }

  private KvartalsvisSykefraværshistorikk hentSykefraværshistorikkVirksomhet(
      Virksomhet virksomhet, Statistikkategori type) {
    return new KvartalsvisSykefraværshistorikk(
        type,
        virksomhet.getNavn(),
        kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentVirksomhet(
            virksomhet));
  }

  protected CompletableFuture<KvartalsvisSykefraværshistorikk>
      uthentingAvSykefraværshistorikkNæring(Virksomhet underenhet) {
    BedreNæringskode næring5siffer = underenhet.getNæringskode();
    return uthentingMedTimeout(
            () -> klassifikasjonerRepository.hentNæring(næring5siffer.getNæring().getTosifferIdentifikator()))
        .thenCompose(
            næring ->
                uthentingMedFeilhåndteringOgTimeout(
                    () -> hentSykefraværshistorikkNæring(næring),
                    Statistikkategori.NÆRING,
                    næring.getNavn()))
        .handle(
            (result, throwable) -> {
              if (throwable == null) {
                return result;
              } else {
                log.warn(
                    format(
                        "Fikk '%s' ved uthenting av næring '%s'. " + "Returnerer en tom liste",
                        throwable.getMessage(), næring5siffer.getNæring().getTosifferIdentifikator()),
                    throwable);
                return new KvartalsvisSykefraværshistorikk(
                    Statistikkategori.NÆRING, null, List.of());
              }
            });
  }

  private static CompletableFuture<Næring> uthentingMedTimeout(
      Supplier<Næring> hentNæringSupplier) {
    return CompletableFuture.supplyAsync(hentNæringSupplier)
        .orTimeout(TIMEOUT_UTHENTING_FRA_DB_I_SEKUNDER, TimeUnit.SECONDS);
  }

  protected static CompletableFuture<KvartalsvisSykefraværshistorikk>
      uthentingMedFeilhåndteringOgTimeout(
          Supplier<KvartalsvisSykefraværshistorikk> sykefraværshistorikkSupplier,
          Statistikkategori statistikkategori,
          String sykefraværshistorikkLabel) {
    return CompletableFuture.supplyAsync(sykefraværshistorikkSupplier)
        .orTimeout(TIMEOUT_UTHENTING_FRA_DB_I_SEKUNDER, TimeUnit.SECONDS)
        .exceptionally(
            e -> {
              log.warn(
                  format(
                      "Fikk '%s' ved uthenting av sykefravarsstatistikk '%s'. "
                          + "Returnerer en tom liste",
                      e.getMessage(), statistikkategori),
                  e);
              return new KvartalsvisSykefraværshistorikk(
                  statistikkategori, sykefraværshistorikkLabel, Collections.emptyList());
            });
  }
}
