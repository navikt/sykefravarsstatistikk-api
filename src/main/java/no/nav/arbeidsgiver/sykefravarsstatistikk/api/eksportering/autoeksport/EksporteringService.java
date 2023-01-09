package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.EKSPORT_BATCH_STØRRELSE;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.cleanUpEtterBatch;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.filterByKvartal;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.getListeAvVirksomhetEksportPerKvartal;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.getSykefraværMedKategoriForLand;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.getSykefraværMedKategoriForNæring;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.getSykefraværMedKategoriForNæring5Siffer;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.getSykefraværMedKategoriForSektor;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.getVirksomhetMetadataHashMap;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.getVirksomhetSykefravær;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.getVirksomheterMetadataFraSubset;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.hentSisteKvartalIBeregningen;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.lagreEksporterteVirksomheterOgNullstillLista;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.leggTilOrgnrIEksporterteVirksomheterListaOglagreIDbNårListaErFull;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.mapToSykefraværsstatistikkLand;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.toMap;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.EksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.SykefraværsstatistikkTilEksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetEksportPerKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadata;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadataRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkSektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhetUtenVarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config.KafkaProperties;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaUtsendingException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.KafkaException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EksporteringService {

  private final EksporteringRepository eksporteringRepository;
  private final VirksomhetMetadataRepository virksomhetMetadataRepository;
  private final SykefraværsstatistikkTilEksporteringRepository
      sykefraværsstatistikkTilEksporteringRepository;
  private final SykefraværRepository sykefraværRepository;
  private final KafkaService kafkaService;
  private final boolean erEksporteringAktivert;

  public EksporteringService(
      EksporteringRepository eksporteringRepository,
      VirksomhetMetadataRepository virksomhetMetadataRepository,
      SykefraværsstatistikkTilEksporteringRepository sykefraværsstatistikkTilEksporteringRepository,
      SykefraværRepository sykefraværRepository,
      KafkaService kafkaService,
      @Value("${statistikk.eksportering.aktivert}") Boolean erEksporteringAktivert) {
    this.eksporteringRepository = eksporteringRepository;
    this.virksomhetMetadataRepository = virksomhetMetadataRepository;
    this.sykefraværsstatistikkTilEksporteringRepository =
        sykefraværsstatistikkTilEksporteringRepository;
    this.sykefraværRepository = sykefraværRepository;
    this.kafkaService = kafkaService;
    this.erEksporteringAktivert = erEksporteringAktivert;
  }

  public int eksporter(
      ÅrstallOgKvartal årstallOgKvartal, EksporteringBegrensning eksporteringBegrensning) {

    if (!erEksporteringAktivert) {
      log.info("Eksportering er ikke aktivert. Avbrytter. ");
      return 0;
    }
    List<VirksomhetEksportPerKvartal> virksomheterTilEksport =
        getListeAvVirksomhetEksportPerKvartal(
            årstallOgKvartal, eksporteringBegrensning, eksporteringRepository);

    int antallStatistikkSomSkalEksporteres =
        virksomheterTilEksport.isEmpty() ? 0 : virksomheterTilEksport.size();

    if (antallStatistikkSomSkalEksporteres == 0) {
      log.info(
          "Ingen statistikk å eksportere for årstall '{}' og kvartal '{}'.",
          årstallOgKvartal.getÅrstall(),
          årstallOgKvartal.getKvartal());
      return 0;
    }

    log.info(
        "Starting eksportering for årstall '{}' og kvartal '{}'. Skal eksportere '{}' rader med statistikk. ",
        årstallOgKvartal.getÅrstall(),
        årstallOgKvartal.getKvartal(),
        antallStatistikkSomSkalEksporteres);
    int antallEksporterteVirksomheter = 0;

    try {
      antallEksporterteVirksomheter = eksporter(virksomheterTilEksport, årstallOgKvartal);
    } catch (KafkaUtsendingException | KafkaException e) {
      log.warn("Fikk Exception fra Kafka med melding:'{}'. Avbryter prosess.", e.getMessage(), e);
    }

    return antallEksporterteVirksomheter;
  }

  protected int eksporter(
      List<VirksomhetEksportPerKvartal> virksomheterTilEksport, ÅrstallOgKvartal årstallOgKvartal)
      throws KafkaUtsendingException {
    long startEksportering = System.currentTimeMillis();
    kafkaService.nullstillUtsendingRapport(
        virksomheterTilEksport.size(), KafkaProperties.EKSPORT_ALLE_KATEGORIER);

    List<VirksomhetMetadata> virksomhetMetadataListe =
        virksomhetMetadataRepository.hentVirksomhetMetadata(årstallOgKvartal);

    List<UmaskertSykefraværForEttKvartal> umaskertSykefraværsstatistikkSistePublisertKvartalLand =
        sykefraværRepository.hentUmaskertSykefraværForNorge(årstallOgKvartal);

    List<SykefraværsstatistikkSektor> sykefraværsstatistikkSektor =
        sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleSektorer(
            årstallOgKvartal);

    List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring =
        sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleNæringer(
            årstallOgKvartal);

    List<SykefraværsstatistikkNæring5Siffer> sykefraværsstatistikkNæring5Siffer =
        sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleNæringer5Siffer(
            årstallOgKvartal);
    List<SykefraværsstatistikkVirksomhetUtenVarighet> sykefraværsstatistikkVirksomhetUtenVarighet =
        sykefraværsstatistikkTilEksporteringRepository.hentSykefraværAlleVirksomheter(
            årstallOgKvartal);

    SykefraværMedKategori landSykefravær =
        getSykefraværMedKategoriForLand(
            årstallOgKvartal,
            mapToSykefraværsstatistikkLand(
                hentSisteKvartalIBeregningen(
                    umaskertSykefraværsstatistikkSistePublisertKvartalLand, årstallOgKvartal)));

    Map<String, VirksomhetMetadata> virksomhetMetadataMap =
        getVirksomhetMetadataHashMap(virksomhetMetadataListe);

    List<? extends List<VirksomhetEksportPerKvartal>> subsets =
        Lists.partition(virksomheterTilEksport, EKSPORT_BATCH_STØRRELSE);
    AtomicInteger antallEksportert = new AtomicInteger();

    subsets.forEach(
        subset -> {
          List<VirksomhetMetadata> virksomheterMetadataIDenneSubset =
              getVirksomheterMetadataFraSubset(virksomhetMetadataMap, subset);

          log.info(
              "Starter utsending av '{}' statistikk meldinger (fra '{}' virksomheter)",
              virksomheterMetadataIDenneSubset.size(),
              subset.size());

          sendIBatch(
              virksomheterMetadataIDenneSubset,
              årstallOgKvartal,
              sykefraværsstatistikkSektor,
              sykefraværsstatistikkNæring,
              sykefraværsstatistikkNæring5Siffer,
              sykefraværsstatistikkVirksomhetUtenVarighet,
              landSykefravær,
              antallEksportert,
              virksomheterTilEksport.size());
        });

    long stoptEksportering = System.currentTimeMillis();
    long totalProsesseringTidISekunder = (stoptEksportering - startEksportering) / 1000;
    log.info(
        "Eksportering er ferdig med: antall statistikk for virksomhet prosessert='{}', "
            + "Eksportering tok '{}' sekunder totalt. "
            + "Snitt prossesseringstid ved utsending til Kafka er: '{}'. "
            + "Snitt prossesseringstid for å oppdatere DB er: '{}'.",
        kafkaService.getAntallMeldingerMottattForUtsending(),
        totalProsesseringTidISekunder,
        kafkaService.getSnittTidUtsendingTilKafka(),
        kafkaService.getSnittTidOppdateringIDB());
    log.info("[Måling] Rå data ved måling: {}", kafkaService.getRåDataVedDetaljertMåling());

    return antallEksportert.get();
  }

  protected void sendIBatch(
      List<VirksomhetMetadata> virksomheterMetadata,
      ÅrstallOgKvartal årstallOgKvartal,
      List<SykefraværsstatistikkSektor> sykefraværsstatistikkSektor,
      List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring,
      List<SykefraværsstatistikkNæring5Siffer> sykefraværsstatistikkNæring5Siffer,
      List<SykefraværsstatistikkVirksomhetUtenVarighet> sykefraværsstatistikkVirksomhetUtenVarighet,
      SykefraværMedKategori landSykefravær,
      AtomicInteger antallEksportert,
      int antallTotaltStatistikk) {
    AtomicInteger antallSentTilEksport = new AtomicInteger();
    AtomicInteger antallVirksomheterLagretSomEksportertIDb = new AtomicInteger();
    List<String> eksporterteVirksomheterListe = new ArrayList<>();
    Map<String, SykefraværsstatistikkVirksomhetUtenVarighet>
        sykefraværsstatistikkVirksomhetForEttKvartalUtenVarighetMap =
            toMap(filterByKvartal(årstallOgKvartal, sykefraværsstatistikkVirksomhetUtenVarighet));

    virksomheterMetadata.stream()
        .forEach(
            virksomhetMetadata -> {
              long startUtsendingProcess = System.nanoTime();

              if (virksomhetMetadata != null) {
                kafkaService.send(
                    årstallOgKvartal,
                    getVirksomhetSykefravær(
                        virksomhetMetadata,
                        sykefraværsstatistikkVirksomhetForEttKvartalUtenVarighetMap),
                    getSykefraværMedKategoriForNæring5Siffer(
                        virksomhetMetadata, sykefraværsstatistikkNæring5Siffer),
                    getSykefraværMedKategoriForNæring(
                        virksomhetMetadata, sykefraværsstatistikkNæring),
                    getSykefraværMedKategoriForSektor(
                        virksomhetMetadata, sykefraværsstatistikkSektor),
                    landSykefravær);

                long stopUtsendingProcess = System.nanoTime();
                antallSentTilEksport.getAndIncrement();
                kafkaService.addUtsendingTilKafkaProcessingTime(
                    startUtsendingProcess, stopUtsendingProcess);

                int antallVirksomhetertLagretSomEksportert =
                    leggTilOrgnrIEksporterteVirksomheterListaOglagreIDbNårListaErFull(
                        virksomhetMetadata.getOrgnr(),
                        årstallOgKvartal,
                        eksporterteVirksomheterListe,
                        eksporteringRepository,
                        kafkaService);
                antallVirksomheterLagretSomEksportertIDb.addAndGet(
                    antallVirksomhetertLagretSomEksportert);
              }
            });

    int antallRestendeOppdatert =
        lagreEksporterteVirksomheterOgNullstillLista(
            årstallOgKvartal, eksporterteVirksomheterListe, eksporteringRepository, kafkaService);
    antallVirksomheterLagretSomEksportertIDb.addAndGet(antallRestendeOppdatert);
    int eksportertHittilNå = antallEksportert.addAndGet(antallSentTilEksport.get());

    cleanUpEtterBatch(eksporteringRepository);

    log.info(
        String.format(
            "Eksportert '%d' rader av '%d' totalt ('%d' oppdatert i DB)",
            eksportertHittilNå,
            antallTotaltStatistikk,
            antallVirksomheterLagretSomEksportertIDb.get()));
  }
}
