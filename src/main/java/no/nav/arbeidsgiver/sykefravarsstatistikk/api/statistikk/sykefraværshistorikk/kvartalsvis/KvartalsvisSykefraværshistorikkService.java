package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis;

import static java.lang.String.format;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.InstitusjonellSektorkode;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.OverordnetEnhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.Sektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.Virksomhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.bransjeprogram.Bransjeprogram;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.KlassifikasjonerRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.SektorMappingService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartal;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KvartalsvisSykefraværshistorikkService {

  public static final int TIMEOUT_UTHENTING_FRA_DB_I_SEKUNDER = 3;
  public static final String SYKEFRAVÆRPROSENT_LAND_LABEL = "Norge";

  private final KvartalsvisSykefraværRepository kvartalsvisSykefraværprosentRepository;
  private final SektorMappingService sektorMappingService;
  private final KlassifikasjonerRepository klassifikasjonerRepository;

  public KvartalsvisSykefraværshistorikkService(
      KvartalsvisSykefraværRepository kvartalsvisSykefraværprosentRepository,
      SektorMappingService sektorMappingService,
      KlassifikasjonerRepository klassifikasjonerRepository) {
    this.kvartalsvisSykefraværprosentRepository = kvartalsvisSykefraværprosentRepository;
    this.sektorMappingService = sektorMappingService;
    this.klassifikasjonerRepository = klassifikasjonerRepository;
  }

  public List<KvartalsvisSykefraværshistorikk> hentSykefraværshistorikk(
          Virksomhet underenhet, InstitusjonellSektorkode institusjonellSektorkode) {
    Optional<Bransje> bransje = Bransjeprogram.finnBransje(underenhet);
    boolean skalHenteDataPåNæring = bransje.isEmpty() || bransje.get().erDefinertPåTosiffernivå();

    Sektor ssbSektor = sektorMappingService.mapTilSSBSektorKode(institusjonellSektorkode);

    List<KvartalsvisSykefraværshistorikk> kvartalsvisSykefraværshistorikkListe =
        Stream.of(
                uthentingMedFeilhåndteringOgTimeout(
                    () -> hentSykefraværshistorikkLand(),
                    Statistikkategori.LAND,
                    SYKEFRAVÆRPROSENT_LAND_LABEL),
                uthentingMedFeilhåndteringOgTimeout(
                    () -> hentSykefraværshistorikkSektor(ssbSektor),
                    Statistikkategori.SEKTOR,
                    ssbSektor.getNavn()),
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

    return kvartalsvisSykefraværshistorikkListe;
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
        hentSykefraværshistorikk(underenhet, overordnetEnhet.getInstitusjonellSektorkode());
    kvartalsvisSykefraværshistorikkListe.add(historikkForOverordnetEnhet);

    return kvartalsvisSykefraværshistorikkListe;
  }

  protected KvartalsvisSykefraværshistorikk hentSykefraværshistorikkLand() {
    return byggSykefraværshistorikk(
        Statistikkategori.LAND,
        SYKEFRAVÆRPROSENT_LAND_LABEL,
        kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentLand());
  }

  private KvartalsvisSykefraværshistorikk hentSykefraværshistorikkSektor(Sektor ssbSektor) {
    return byggSykefraværshistorikk(
        Statistikkategori.SEKTOR,
        ssbSektor.getNavn(),
        kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentSektor(ssbSektor));
  }

  private KvartalsvisSykefraværshistorikk hentSykefraværshistorikkNæring(Næring næring) {
    return byggSykefraværshistorikk(
        Statistikkategori.NÆRING,
        næring.getNavn(),
        kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentNæring(næring));
  }

  protected KvartalsvisSykefraværshistorikk hentSykefraværshistorikkBransje(Bransje bransje) {
    return byggSykefraværshistorikk(
        Statistikkategori.BRANSJE,
        bransje.getNavn(),
        kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentBransje(bransje));
  }

  private KvartalsvisSykefraværshistorikk hentSykefraværshistorikkVirksomhet(
      Virksomhet virksomhet, Statistikkategori type) {
    return byggSykefraværshistorikk(
        type,
        virksomhet.getNavn(),
        kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentVirksomhet(
            virksomhet));
  }

  protected CompletableFuture<KvartalsvisSykefraværshistorikk>
      uthentingAvSykefraværshistorikkNæring(Virksomhet underenhet) {
    Næringskode5Siffer næring5siffer = underenhet.getNæringskode();
    return uthentingMedTimeout(
            () -> klassifikasjonerRepository.hentNæring(næring5siffer.hentNæringskode2Siffer()))
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
                        throwable.getMessage(), næring5siffer.hentNæringskode2Siffer()),
                    throwable);
                return byggSykefraværshistorikk(
                    Statistikkategori.NÆRING, null, Collections.EMPTY_LIST);
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
              return byggSykefraværshistorikk(
                  statistikkategori, sykefraværshistorikkLabel, Collections.emptyList());
            });
  }

  private static KvartalsvisSykefraværshistorikk byggSykefraværshistorikk(
      Statistikkategori statistikkategori,
      String label,
      List<SykefraværForEttKvartal> sykefraværForEttKvartal) {
    KvartalsvisSykefraværshistorikk kvartalsvisSykefraværshistorikk =
        new KvartalsvisSykefraværshistorikk();
    kvartalsvisSykefraværshistorikk.setType(statistikkategori);
    kvartalsvisSykefraværshistorikk.setLabel(label);
    kvartalsvisSykefraværshistorikk.setSykefraværForEttKvartal(sykefraværForEttKvartal);

    return kvartalsvisSykefraværshistorikk;
  }
}
