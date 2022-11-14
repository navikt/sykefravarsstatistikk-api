package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import static java.lang.String.format;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.EKSPORT_BATCH_STØRRELSE;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.cleanUpEtterBatch;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.getListeAvVirksomhetEksportPerKvartal;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.getSykefraværMedKategoriForLand;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.hentSisteKvartalIBeregningen;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.lagreEksporterteVirksomheterOgNullstillLista;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.leggTilOrgnrIEksporterteVirksomheterListaOglagreIDbNårListaErFull;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.mapToSykefraværsstatistikkLand;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.EksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.SykefraværsstatistikkTilEksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetEksportPerKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhetUtenVarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config.KafkaProperties;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaUtsendingException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.KafkaException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EksporteringPerStatistikkKategoriService {

  private final EksporteringRepository eksporteringRepository;
  private final SykefraværRepository sykefraværRepository;
  private final SykefraværsstatistikkTilEksporteringRepository sykefraværsstatistikkTilEksporteringRepository;
  private final KafkaService kafkaService;
  private final boolean erEksporteringAktivert;

  public EksporteringPerStatistikkKategoriService(
      SykefraværRepository sykefraværRepository,
      SykefraværsstatistikkTilEksporteringRepository sykefraværsstatistikkTilEksporteringRepository,
      EksporteringRepository eksporteringRepository,
      KafkaService kafkaService,
      @Value("${statistikk.eksportering.aktivert}") Boolean erEksporteringAktivert
  ) {
    this.eksporteringRepository = eksporteringRepository;
    this.sykefraværRepository = sykefraværRepository;
    this.sykefraværsstatistikkTilEksporteringRepository = sykefraværsstatistikkTilEksporteringRepository;
    this.kafkaService = kafkaService;
    this.erEksporteringAktivert = erEksporteringAktivert;
  }

  public int eksporterPerStatistikkKategori(
      ÅrstallOgKvartal årstallOgKvartal,
      Statistikkategori statistikkategori,
      EksporteringBegrensning eksporteringBegrensning
  ) {

    if (!erEksporteringAktivert) {
      log.info("Eksportering er ikke aktivert. Avbrytter. ");
      return 0;
    }
    log.info(
        "Starting eksportering av '{}' for årstall '{}' og kvartal '{}'.",
        statistikkategori.name(),
        årstallOgKvartal.getÅrstall(),
        årstallOgKvartal.getKvartal()
    );

    if (Statistikkategori.LAND == statistikkategori) {
      return eksporterSykefraværsstatistikkLand(årstallOgKvartal);
    }

    List<VirksomhetEksportPerKvartal> virksomheterTilEksport =
        getListeAvVirksomhetEksportPerKvartal(årstallOgKvartal, eksporteringBegrensning,
            eksporteringRepository);

    if (virksomheterTilEksport.size() == 0) {
      log.info("Ingen statistikk å eksportere for årstall '{}' og kvartal '{}'.",
          årstallOgKvartal.getÅrstall(),
          årstallOgKvartal.getKvartal()
      );
      return 0;
    }

    log.info(
        "Eksportering av '{}' for årstall '{}' og kvartal '{}'. Skal eksportere '{}' rader med statistikk.",
        statistikkategori.name(),
        årstallOgKvartal.getÅrstall(),
        årstallOgKvartal.getKvartal(),
        virksomheterTilEksport.size()
    );
    int antallEksporterteVirksomheter = 0;

    try {
      antallEksporterteVirksomheter = eksporterSykefraværsstatistikkVirksomhet(
          virksomheterTilEksport,
          årstallOgKvartal
      );
    } catch (KafkaUtsendingException | KafkaException e) {
      log.warn("Fikk Exception fra Kafka med melding:'{}'. Avbryter prosess.", e.getMessage(), e);
    }

    return antallEksporterteVirksomheter;
  }

  public int eksporterSykefraværsstatistikkLand(ÅrstallOgKvartal årstallOgKvartal) {

    long startProcess = System.nanoTime();
    List<UmaskertSykefraværForEttKvartal> umaskertSykefraværsstatistikkSiste4KvartalerLand =
        sykefraværRepository.hentUmaskertSykefraværForNorge(
            årstallOgKvartal.minusKvartaler(3)
        );

    SykefraværMedKategori landSykefravær = getSykefraværMedKategoriForLand(
        årstallOgKvartal,
        mapToSykefraværsstatistikkLand(
            hentSisteKvartalIBeregningen(
                umaskertSykefraværsstatistikkSiste4KvartalerLand,
                årstallOgKvartal
            )
        )
    );

    boolean erSent = false;
    AtomicInteger antallEksportert = new AtomicInteger();
    AtomicInteger antallIkkeEksportert = new AtomicInteger();
    try {
      SykefraværFlereKvartalerForEksport sykefraværOverFlereKvartaler = new SykefraværFlereKvartalerForEksport(
          umaskertSykefraværsstatistikkSiste4KvartalerLand
      );

      erSent = kafkaService.sendTilStatistikkKategoriTopic(
          årstallOgKvartal,
          Statistikkategori.LAND,
          "NO",
          landSykefravær,
          sykefraværOverFlereKvartaler
      );
    } catch (KafkaUtsendingException | KafkaException e) {
      log.warn("Fikk Exception fra Kafka med melding:'{}'. Avbryter prosess.", e.getMessage(), e);
    }

    if (erSent) {
      antallEksportert.incrementAndGet();
    } else {
      antallIkkeEksportert.incrementAndGet();
    }
    long tidsbrukt = System.nanoTime() - startProcess;
    log.info(format("Eksport av statistikk for kategori %s er ferdig. "
            + "Antall statistikk eksportert er: %d, "
            + "antall ikke eksportert er: %d. "
            + "Tid for eksportering : %d",
        Statistikkategori.LAND.name(),
        antallEksportert.get(),
        antallIkkeEksportert.get(),
        tidsbrukt
    ));

    return antallEksportert.get();
  }

  public int eksporterSykefraværsstatistikkVirksomhet(
      List<VirksomhetEksportPerKvartal> virksomheterTilEksport,
      ÅrstallOgKvartal årstallOgKvartal
  ) {
    long startEksportering = System.currentTimeMillis();
    kafkaService.nullstillUtsendingRapport(virksomheterTilEksport.size(),
        KafkaProperties.EKSPORT_ALLE_KATEGORIER);

    // Hente data
    log.info("Starting utregning av statistikk");
    List<SykefraværsstatistikkVirksomhetUtenVarighet> alleKvartal =
        sykefraværsstatistikkTilEksporteringRepository.hentSykefraværAlleVirksomheter(
            årstallOgKvartal.minusKvartaler(3),
            årstallOgKvartal
        );

    Map<String, List<SykefraværsstatistikkVirksomhetUtenVarighet>> sykefraværGruppertEtterOrgNr = alleKvartal.stream()
        .collect(
            Collectors.groupingBy(SykefraværsstatistikkVirksomhetUtenVarighet::getOrgnr));

    Map<String, SykefraværMedKategori> sykefraværMedKategoriSisteKvartalMap =
        createSykefraværMedKategoriMap(sykefraværGruppertEtterOrgNr);

    Map<String, SykefraværFlereKvartalerForEksport> sykefraværOverFlereKvartalerMap =
        createSykefraværOverFlereKvartalerMap(sykefraværGruppertEtterOrgNr);

    // #2 Split i subsets
    log.info(format("Starting utsending av statistikk. '%d' meldinger vil bli sendt",
        virksomheterTilEksport.size()));
    List<? extends List<VirksomhetEksportPerKvartal>> subsets =
        Lists.partition(virksomheterTilEksport, EKSPORT_BATCH_STØRRELSE);
    int totalBatchAntall = subsets.size();
    log.info(format("Deler utsending av statistikk i '%d' batch ", totalBatchAntall));

    // #3 send til kafka
    AtomicInteger batchAntallProsessert = new AtomicInteger();
    AtomicInteger antallEksportert = new AtomicInteger();
    AtomicInteger antallSentTilEksport = new AtomicInteger();
    AtomicInteger antallIkkeEksportert = new AtomicInteger();
    AtomicInteger antallUtenStatistikk = new AtomicInteger();
    AtomicInteger antallVirksomheterLagretSomEksportertIDb = new AtomicInteger();
    List<String> eksporterteVirksomheterListe = new ArrayList<>();

    subsets.forEach(subset -> {
          long startUtsendingProcessForSubset = System.nanoTime();
          subset.stream().forEach(
              virksomhet -> {
                long startUtsendingProcess = System.nanoTime();
                SykefraværMedKategori sykefraværMedKategori = getSykefraværMedKategori(
                    sykefraværMedKategoriSisteKvartalMap,
                    Statistikkategori.VIRKSOMHET,
                    virksomhet.getOrgnr(),
                    virksomhet.getÅrstallOgKvartal());
                SykefraværFlereKvartalerForEksport sykefraværOverFlereKvartaler =
                    getSykefraværOverFlereKvartaler(
                        sykefraværOverFlereKvartalerMap,
                        virksomhet.getOrgnr()
                    );

                if (Objects.equals(
                    sykefraværMedKategori,
                    SykefraværMedKategori.utenStatistikk(
                        Statistikkategori.VIRKSOMHET,
                        virksomhet.getOrgnr(),
                        årstallOgKvartal
                    )) && Objects.equals(
                    sykefraværOverFlereKvartaler,
                    SykefraværFlereKvartalerForEksport.utenStatistikk())
                ) {
                  antallUtenStatistikk.incrementAndGet();
                }

                boolean erSent = kafkaService.sendTilStatistikkKategoriTopic(
                    årstallOgKvartal,
                    Statistikkategori.VIRKSOMHET,
                    virksomhet.getOrgnr(),
                    sykefraværMedKategori,
                    sykefraværOverFlereKvartaler
                );
                long stopUtsendingProcess = System.nanoTime();
                kafkaService.addUtsendingTilKafkaProcessingTime(startUtsendingProcess,
                    stopUtsendingProcess);
                if (erSent) {
                  antallEksportert.incrementAndGet();
                  int antallVirksomhetertLagretSomEksportert =
                      leggTilOrgnrIEksporterteVirksomheterListaOglagreIDbNårListaErFull(
                          virksomhet.getOrgnr(),
                          årstallOgKvartal,
                          eksporterteVirksomheterListe,
                          eksporteringRepository,
                          kafkaService
                      );
                  antallVirksomheterLagretSomEksportertIDb.addAndGet(
                      antallVirksomhetertLagretSomEksportert);
                } else {
                  antallIkkeEksportert.incrementAndGet();
                }

                int antallRestendeOppdatert = lagreEksporterteVirksomheterOgNullstillLista(
                    årstallOgKvartal,
                    eksporterteVirksomheterListe,
                    eksporteringRepository,
                    kafkaService
                );
                antallVirksomheterLagretSomEksportertIDb.addAndGet(antallRestendeOppdatert);

                cleanUpEtterBatch(eksporteringRepository);
              }
          );

          int eksportertHittilNå = antallEksportert.addAndGet(antallSentTilEksport.get());
          long stopUtsendingProcessForSubset = System.nanoTime();
          long prosesseringtid = stopUtsendingProcessForSubset - startUtsendingProcessForSubset;
          batchAntallProsessert.incrementAndGet();

          log.info(format("Ferdig med å prosessere subset %d av %d. "
                  + "Antall statistikk sent frem til nå er: %d. "
                  + "Utsending tid var (i millis): %d ",
              batchAntallProsessert.get(),
              totalBatchAntall,
              eksportertHittilNå,
              (prosesseringtid / 1000000)
          ));
        }
    );

    long stoptEksportering = System.currentTimeMillis();
    long totalProsesseringTidISekunder = (stoptEksportering - startEksportering) / 1000;
    log.info(format("Ferdig med utsending av alle meldinger til Kafka for Virksomhet statistikk. "
            + "Antall eksportert er: %d, "
            + "antall ikke eksportert er: %d "
            + "og antall virksomhet uten statistikk er: %d",
        antallEksportert.get(),
        antallIkkeEksportert.get(),
        antallUtenStatistikk.get()
    ));

    log.info("Eksportering per statistikk kategori er ferdig med: " +
            "antall statistikk for {} prosessert='{}', " +
            "Eksportering tok '{}' sekunder totalt. " +
            "Snitt prossesseringstid (i millis) ved utsending til Kafka er: '{}'. " +
            "Snitt prossesseringstid (i millis) for å oppdatere DB er: '{}'.",
        Statistikkategori.VIRKSOMHET.name(),
        kafkaService.getAntallMeldingerMottattForUtsending(),
        totalProsesseringTidISekunder,
        kafkaService.getSnittTidUtsendingTilKafka() / 1000000,
        kafkaService.getSnittTidOppdateringIDB() / 1000000
    );
    log.info("[Måling] Rå data ved detaljert måling: {}",
        kafkaService.getRåDataVedDetaljertMåling()
    );

    return antallEksportert.get();
  }

  private SykefraværFlereKvartalerForEksport getSykefraværOverFlereKvartaler(
      Map<String, SykefraværFlereKvartalerForEksport> sykefraværOverFlereKvartalerMap,
      String identifikator) {

    SykefraværFlereKvartalerForEksport sykefraværFlereKvartalerForEksport =
        sykefraværOverFlereKvartalerMap.get(identifikator);
    return Objects.requireNonNullElseGet(sykefraværFlereKvartalerForEksport,
        () -> SykefraværFlereKvartalerForEksport.utenStatistikk());
  }

  private static SykefraværMedKategori getSykefraværMedKategori(
      Map<String, SykefraværMedKategori> sykefraværMedKategoriSisteKvartalMap,
      Statistikkategori statistikkategori,
      String identifikator,
      ÅrstallOgKvartal årstallOgKvartal) {
    SykefraværMedKategori sykefraværMedKategori = sykefraværMedKategoriSisteKvartalMap.get(
        identifikator);
    return Objects.requireNonNullElseGet(sykefraværMedKategori,
        () -> SykefraværMedKategori.utenStatistikk(
            statistikkategori,
            identifikator,
            årstallOgKvartal
        ));
  }

  private Map<String, SykefraværMedKategori> createSykefraværMedKategoriMap(
      Map<String, List<SykefraværsstatistikkVirksomhetUtenVarighet>> sykefraværGruppertEtterOrgNr) {
    Map<String, SykefraværMedKategori> sykefraværSisteKvartalPerOrg = new HashMap<String, SykefraværMedKategori>();

    sykefraværGruppertEtterOrgNr.forEach(
        (orgnr, sykefravær) -> {
          SykefraværsstatistikkVirksomhetUtenVarighet sykefraværSisteKvartal = sykefravær.stream()
              .max(
                  Comparator.comparing(
                      kvartal -> new ÅrstallOgKvartal(kvartal.getÅrstall(), kvartal.getKvartal())))
              .get();
          SykefraværMedKategori sykefraværMedKategori = new SykefraværMedKategori(
              Statistikkategori.VIRKSOMHET,
              sykefraværSisteKvartal.getOrgnr(),
              new ÅrstallOgKvartal(sykefraværSisteKvartal.getÅrstall(),
                  sykefraværSisteKvartal.getKvartal()),
              sykefraværSisteKvartal.getTapteDagsverk(),
              sykefraværSisteKvartal.getMuligeDagsverk(),
              sykefraværSisteKvartal.getAntallPersoner()
          );
          sykefraværSisteKvartalPerOrg.put(orgnr, sykefraværMedKategori);
        });

    return sykefraværSisteKvartalPerOrg;
  }

  private Map<String, SykefraværFlereKvartalerForEksport> createSykefraværOverFlereKvartalerMap(
      Map<String, List<SykefraværsstatistikkVirksomhetUtenVarighet>> sykefraværGruppertEtterOrgNr) {
    Map<String, SykefraværFlereKvartalerForEksport> sykefraværOverFlereKvartalerPerOrgNr = new HashMap<>();
    sykefraværGruppertEtterOrgNr.forEach((orgnr, sykefravær) -> {
      List<UmaskertSykefraværForEttKvartal> umaskertSykefravær = sykefravær.stream()
          .map(item -> new UmaskertSykefraværForEttKvartal(
              new ÅrstallOgKvartal(item.getÅrstall(), item.getKvartal()),
              item.getTapteDagsverk(),
              item.getMuligeDagsverk(),
              item.getAntallPersoner()
          )).collect(Collectors.toList());

      sykefraværOverFlereKvartalerPerOrgNr.put(orgnr,
          new SykefraværFlereKvartalerForEksport(umaskertSykefravær));
    });

    return sykefraværOverFlereKvartalerPerOrgNr;
  }
}
