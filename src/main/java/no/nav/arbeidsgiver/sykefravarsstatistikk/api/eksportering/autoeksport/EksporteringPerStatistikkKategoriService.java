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
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.ArbeidsmiljøportalenBransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkSektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhetUtenVarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config.KafkaTopic;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaUtsendingException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto.Kafkamelding;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto.StatistikkategoriKafkamelding;
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
  private final SykefraværsstatistikkTilEksporteringRepository tilEksporteringRepository;
  private final SykefraværRepository sykefraværRepository;
  private final KafkaService kafkaService;
  private final boolean erEksporteringAktivert;

  public EksporteringPerStatistikkKategoriService(
      SykefraværRepository sykefraværRepository,
      SykefraværsstatistikkTilEksporteringRepository sykefraværsstatistikkTilEksporteringRepository,
      EksporteringRepository eksporteringRepository,
      KafkaService kafkaService,
      @Value("${statistikk.eksportering.aktivert}") Boolean erEksporteringAktivert) {
    this.eksporteringRepository = eksporteringRepository;
    this.sykefraværRepository = sykefraværRepository;
    this.tilEksporteringRepository = sykefraværsstatistikkTilEksporteringRepository;
    this.kafkaService = kafkaService;
    this.erEksporteringAktivert = erEksporteringAktivert;
  }

  public void eksporterPerStatistikkKategori(
      ÅrstallOgKvartal årstallOgKvartal,
      Statistikkategori statistikkategori,
      EksporteringBegrensning eksporteringBegrensning) {

    if (!erEksporteringAktivert) {
      log.info("Eksportering er ikke aktivert. Avbryter.");
      return;
    }

    log.info(
        "Starter eksportering av kategori '{}' for årstall '{}' og kvartal '{}' på topic '{}'.",
        statistikkategori.name(),
        årstallOgKvartal.getÅrstall(),
        årstallOgKvartal.getKvartal(),
        KafkaTopic.Companion.from(statistikkategori).getNavn());

    if (Statistikkategori.LAND.equals(statistikkategori)) {
      eksporterSykefraværsstatistikkLand(årstallOgKvartal);
      return;
    }

    if (Statistikkategori.NÆRING.equals(statistikkategori)) {
      eksporterSykefraværsstatistikkNæring(årstallOgKvartal);
      return;
    }

    if (Statistikkategori.SEKTOR.equals(statistikkategori)) {
      eksporterSykefraværsstatistikkSektor(årstallOgKvartal);
      return;
    }

    if (Statistikkategori.BRANSJE.equals(statistikkategori)) {
      eksporterSykefraværsstatistikkBransje(årstallOgKvartal);
      return;
    }

    if (Statistikkategori.VIRKSOMHET.equals(statistikkategori)) {
      eksporterSykefraværsstatistikkVirksomhet(årstallOgKvartal, eksporteringBegrensning);
      return;
    }

    log.warn("Ikke implementert eksport for kategori '{}'", statistikkategori.name());
  }

  private void eksporterSykefraværsstatistikkBransje(ÅrstallOgKvartal sisteKvartal) {
    List<SykefraværsstatistikkBransje> sykefraværsstatistikkSisteFireKvartalerBransje =
        tilEksporteringRepository.hentSykefraværAlleBransjerFraOgMed(
            sisteKvartal.minusKvartaler(3));

    Map<ArbeidsmiljøportalenBransje, List<SykefraværsstatistikkBransje>>
        sykefraværGruppertEtterBransje =
            sykefraværsstatistikkSisteFireKvartalerBransje.stream()
                .collect(Collectors.groupingBy(SykefraværsstatistikkBransje::getBransje));

    sykefraværGruppertEtterBransje.forEach(
        (bransje, sykefraværsstatistikkForEnBransje) -> {
          boolean erSendt = false;

          List<UmaskertSykefraværForEttKvartal> sykefraværForFireKvartaler =
              sykefraværsstatistikkForEnBransje.stream()
                  .map(UmaskertSykefraværForEttKvartal::new)
                  .collect(Collectors.toList());

          SykefraværMedKategori sykefraværMedKategoriSisteKvartal =
              new SykefraværMedKategori(
                  Statistikkategori.BRANSJE,
                  bransje.name(),
                  getSykefraværSisteKvartal(sykefraværForFireKvartaler));

          assertForespurteKvartalFinnesIStatistikken(
              sisteKvartal, sykefraværMedKategoriSisteKvartal);

          SykefraværFlereKvartalerForEksport sykefraværOverFlereKvartaler =
              new SykefraværFlereKvartalerForEksport(sykefraværForFireKvartaler);

          StatistikkategoriKafkamelding melding =
              new StatistikkategoriKafkamelding(
                  sykefraværMedKategoriSisteKvartal, sykefraværOverFlereKvartaler);
          kafkaService.sendMessage(melding, KafkaTopic.SYKEFRAVARSSTATISTIKK_BRANSJE_V1);
        });
  }

  protected void eksporterSykefraværsstatistikkSektor(ÅrstallOgKvartal årstallOgKvartal) {
    long startProcess = System.nanoTime();
    List<SykefraværsstatistikkSektor> sykefraværsstatistikkSiste4KvartalerSektor =
        tilEksporteringRepository.hentSykefraværAlleSektorerFraOgMed(
            årstallOgKvartal.minusKvartaler(3));

    Map<String, List<SykefraværsstatistikkSektor>> sykefraværGruppertEtterSektor =
        sykefraværsstatistikkSiste4KvartalerSektor.stream()
            .collect(Collectors.groupingBy(SykefraværsstatistikkSektor::getSektorkode));

    sykefraværGruppertEtterSektor.forEach(
        (sektor, sykefraværForEnSektor) -> {
          boolean erSendt = false;

          List<UmaskertSykefraværForEttKvartal> umaskertSykefraværsstatistikkSiste4KvartalerSektor =
              sykefraværForEnSektor.stream()
                  .map(UmaskertSykefraværForEttKvartal::new)
                  .collect(Collectors.toList());

          UmaskertSykefraværForEttKvartal umaskertSykefraværSisteKvartal =
              getSykefraværSisteKvartal(umaskertSykefraværsstatistikkSiste4KvartalerSektor);
          SykefraværMedKategori sykefraværMedKategoriSisteKvartal =
              new SykefraværMedKategori(
                  Statistikkategori.SEKTOR, sektor, umaskertSykefraværSisteKvartal);

          assertForespurteKvartalFinnesIStatistikken(
              årstallOgKvartal, sykefraværMedKategoriSisteKvartal);

          SykefraværFlereKvartalerForEksport sykefraværOverFlereKvartaler =
              new SykefraværFlereKvartalerForEksport(
                  umaskertSykefraværsstatistikkSiste4KvartalerSektor);

          StatistikkategoriKafkamelding melding =
              new StatistikkategoriKafkamelding(
                  sykefraværMedKategoriSisteKvartal, sykefraværOverFlereKvartaler);
          kafkaService.sendMessage(melding, KafkaTopic.SYKEFRAVARSSTATISTIKK_SEKTOR_V1);
        });
  }

  protected void eksporterSykefraværsstatistikkNæring(ÅrstallOgKvartal årstallOgKvartal) {

    List<SykefraværsstatistikkNæring> sykefraværsstatistikkSiste4KvartalerNæring =
        tilEksporteringRepository.hentSykefraværAlleNæringerFraOgMed(
            årstallOgKvartal.minusKvartaler(3));

    Map<String, List<SykefraværsstatistikkNæring>> sykefraværGruppertEtterNæring =
        sykefraværsstatistikkSiste4KvartalerNæring.stream()
            .collect(Collectors.groupingBy(SykefraværsstatistikkNæring::getNæringkode));

    sykefraværGruppertEtterNæring.forEach(
        (næring, sykefraværForEnNæring) -> {
          boolean erSendt = false;

          List<UmaskertSykefraværForEttKvartal> umaskertSykefraværsstatistikkSiste4KvartalerNæring =
              sykefraværForEnNæring.stream()
                  .map(
                      sykefraværForEttKvartal ->
                          new UmaskertSykefraværForEttKvartal(
                              new ÅrstallOgKvartal(
                                  sykefraværForEttKvartal.getÅrstall(),
                                  sykefraværForEttKvartal.getKvartal()),
                              sykefraværForEttKvartal.getTapteDagsverk(),
                              sykefraværForEttKvartal.getMuligeDagsverk(),
                              sykefraværForEttKvartal.getAntallPersoner()))
                  .collect(Collectors.toList());

          UmaskertSykefraværForEttKvartal umaskertSykefraværSisteKvartal =
              getSykefraværSisteKvartal(umaskertSykefraværsstatistikkSiste4KvartalerNæring);
          SykefraværMedKategori sykefraværMedKategoriSisteKvartal =
              new SykefraværMedKategori(
                  Statistikkategori.NÆRING,
                  næring,
                  umaskertSykefraværSisteKvartal.getÅrstallOgKvartal(),
                  umaskertSykefraværSisteKvartal.getDagsverkTeller(),
                  umaskertSykefraværSisteKvartal.getDagsverkNevner(),
                  umaskertSykefraværSisteKvartal.getAntallPersoner());

          assertForespurteKvartalFinnesIStatistikken(
              årstallOgKvartal, sykefraværMedKategoriSisteKvartal);
          SykefraværFlereKvartalerForEksport sykefraværOverFlereKvartaler =
              new SykefraværFlereKvartalerForEksport(
                  umaskertSykefraværsstatistikkSiste4KvartalerNæring);

          StatistikkategoriKafkamelding melding =
              new StatistikkategoriKafkamelding(
                  sykefraværMedKategoriSisteKvartal, sykefraværOverFlereKvartaler);
          kafkaService.sendMessage(melding, KafkaTopic.SYKEFRAVARSSTATISTIKK_BRANSJE_V1);
        });
  }

  protected void eksporterSykefraværsstatistikkLand(ÅrstallOgKvartal årstallOgKvartal) {
    long startProcess = System.nanoTime();
    List<UmaskertSykefraværForEttKvartal> umaskertSykefraværsstatistikkSiste4KvartalerLand =
        sykefraværRepository.hentUmaskertSykefraværForNorge(årstallOgKvartal.minusKvartaler(3));

    SykefraværMedKategori landSykefravær =
        getSykefraværMedKategoriForLand(
            årstallOgKvartal,
            mapToSykefraværsstatistikkLand(
                hentSisteKvartalIBeregningen(
                    umaskertSykefraværsstatistikkSiste4KvartalerLand, årstallOgKvartal)));

    assertForespurteKvartalFinnesIStatistikken(årstallOgKvartal, landSykefravær);

    SykefraværFlereKvartalerForEksport sykefraværOverFlereKvartaler =
        new SykefraværFlereKvartalerForEksport(umaskertSykefraværsstatistikkSiste4KvartalerLand);

    StatistikkategoriKafkamelding melding =
        new StatistikkategoriKafkamelding(landSykefravær, sykefraværOverFlereKvartaler);
    kafkaService.sendMessage(melding, KafkaTopic.SYKEFRAVARSSTATISTIKK_BRANSJE_V1);
  }

  private static void assertForespurteKvartalFinnesIStatistikken(
      ÅrstallOgKvartal årstallOgKvartal, SykefraværMedKategori landSykefravær) {
    if (!årstallOgKvartal.equals(landSykefravær.getÅrstallOgKvartal())) {
      throw new RuntimeException("Siste kvartal i dataene er ikke lik forespurt kvartal");
    }
  }

  protected int eksporterSykefraværsstatistikkVirksomhet(
      ÅrstallOgKvartal årstallOgKvartal, EksporteringBegrensning eksporteringBegrensning) {

    List<VirksomhetEksportPerKvartal> virksomheterTilEksport =
        getListeAvVirksomhetEksportPerKvartal(
            årstallOgKvartal, eksporteringBegrensning, eksporteringRepository);

    if (virksomheterTilEksport.size() == 0) {
      log.info(
          "Ingen statistikk å eksportere for årstall '{}' og kvartal '{}'.",
          årstallOgKvartal.getÅrstall(),
          årstallOgKvartal.getKvartal());
      return 0;
    }

    log.info(
        "Eksportering av '{}' for årstall '{}' og kvartal '{}'. Skal eksportere '{}' rader med statistikk.",
        Statistikkategori.VIRKSOMHET,
        årstallOgKvartal.getÅrstall(),
        årstallOgKvartal.getKvartal(),
        virksomheterTilEksport.size());
    long startEksportering = System.currentTimeMillis();
    kafkaService.nullstillUtsendingRapport(
        virksomheterTilEksport.size(), KafkaTopic.SYKEFRAVARSSTATISTIKK_VIRKSOMHET_V1);

    log.info("Starting utregning av statistikk");
    List<SykefraværsstatistikkVirksomhetUtenVarighet> alleKvartal =
        tilEksporteringRepository.hentSykefraværAlleVirksomheter(
            årstallOgKvartal.minusKvartaler(3), årstallOgKvartal);

    Map<String, List<SykefraværsstatistikkVirksomhetUtenVarighet>> sykefraværGruppertEtterOrgNr =
        alleKvartal.stream()
            .collect(Collectors.groupingBy(SykefraværsstatistikkVirksomhetUtenVarighet::getOrgnr));

    Map<String, SykefraværMedKategori> sykefraværMedKategoriSisteKvartalMap =
        createSykefraværMedKategoriMap(sykefraværGruppertEtterOrgNr);

    Map<String, SykefraværFlereKvartalerForEksport> sykefraværOverFlereKvartalerMap =
        createSykefraværOverFlereKvartalerMap(sykefraværGruppertEtterOrgNr);

    log.info(
        format(
            "Starting utsending av statistikk. '%d' meldinger vil bli sendt",
            virksomheterTilEksport.size()));
    List<? extends List<VirksomhetEksportPerKvartal>> subsets =
        Lists.partition(virksomheterTilEksport, EKSPORT_BATCH_STØRRELSE);
    int antallSubsetsSomSkalProsesseres = subsets.size();
    AtomicInteger antallSubsetProsessert = new AtomicInteger();
    log.info(
        format("Deler utsending av statistikk i '%d' batch ", antallSubsetsSomSkalProsesseres));

    AtomicInteger antallEksportert = new AtomicInteger();
    AtomicInteger antallSentTilEksport = new AtomicInteger();
    AtomicInteger antallIkkeEksportert = new AtomicInteger();
    AtomicInteger antallUtenStatistikk = new AtomicInteger();
    AtomicInteger antallVirksomheterLagretSomEksportertIDb = new AtomicInteger();
    List<String> eksporterteVirksomheterListe = new ArrayList<>();

    subsets.forEach(
        virksomheter -> {
          long startUtsendingProcessForSubset = System.nanoTime();

          virksomheter.forEach(
              virksomhet ->
                  sendStatistikkForVirksomhetOgOppdaterMetrikker(
                      virksomhet,
                      årstallOgKvartal,
                      sykefraværMedKategoriSisteKvartalMap,
                      sykefraværOverFlereKvartalerMap,
                      antallEksportert,
                      antallIkkeEksportert,
                      antallUtenStatistikk,
                      antallVirksomheterLagretSomEksportertIDb,
                      eksporterteVirksomheterListe));

          int antallRestendeOppdatert =
              lagreEksporterteVirksomheterOgNullstillLista(
                  årstallOgKvartal,
                  eksporterteVirksomheterListe,
                  eksporteringRepository,
                  kafkaService);
          antallVirksomheterLagretSomEksportertIDb.addAndGet(antallRestendeOppdatert);
          cleanUpEtterBatch(eksporteringRepository);

          int eksportertHittilNå = antallEksportert.addAndGet(antallSentTilEksport.get());
          long stopUtsendingProcessForSubset = System.nanoTime();
          long prosesseringtid = stopUtsendingProcessForSubset - startUtsendingProcessForSubset;
          antallSubsetProsessert.incrementAndGet();

          log.info(
              format(
                  "Ferdig med å prosessere subset %d av %d. "
                      + "Antall statistikk sent frem til nå er: %d. "
                      + "Utsending tid var (i millis): %d ",
                  antallSubsetProsessert.get(),
                  antallSubsetsSomSkalProsesseres,
                  eksportertHittilNå,
                  (prosesseringtid / 1000000)));
        });

    log.info(
        format(
            "Ferdig med utsending av alle meldinger til Kafka for Virksomhet statistikk. "
                + "Antall eksportert er: %d, "
                + "antall ikke eksportert er: %d "
                + "og antall virksomhet uten statistikk er: %d",
            antallEksportert.get(), antallIkkeEksportert.get(), antallUtenStatistikk.get()));

    long stoptEksportering = System.currentTimeMillis();
    long totalProsesseringTidISekunder = (stoptEksportering - startEksportering) / 1000;
    log.info(
        "Eksportering per statistikk kategori er ferdig med: "
            + "antall statistikk for {} prosessert='{}', "
            + "Eksportering tok '{}' sekunder totalt. "
            + "Snitt prossesseringstid (i millis) ved utsending til Kafka er: '{}'. "
            + "Snitt prossesseringstid (i millis) for å oppdatere DB er: '{}'.",
        Statistikkategori.VIRKSOMHET.name(),
        kafkaService.getAntallMeldingerMottattForUtsending(),
        totalProsesseringTidISekunder,
        kafkaService.getSnittTidUtsendingTilKafka() / 1000000,
        kafkaService.getSnittTidOppdateringIDB() / 1000000);

    log.info(
        "[Måling] Rå data ved detaljert måling: {}", kafkaService.getRåDataVedDetaljertMåling());

    return antallEksportert.get();
  }

  private void sendStatistikkForVirksomhetOgOppdaterMetrikker(
      VirksomhetEksportPerKvartal virksomhet,
      ÅrstallOgKvartal årstallOgKvartal,
      Map<String, SykefraværMedKategori> sykefraværMedKategoriSisteKvartalMap,
      Map<String, SykefraværFlereKvartalerForEksport> sykefraværOverFlereKvartalerMap,
      AtomicInteger antallEksportert,
      AtomicInteger antallIkkeEksportert,
      AtomicInteger antallUtenStatistikk,
      AtomicInteger antallVirksomheterLagretSomEksportertIDb,
      List<String> eksporterteVirksomheterListe) {
    long startUtsendingProcess = System.nanoTime();

    SykefraværMedKategori sykefraværMedKategori =
        getSykefraværMedKategori(
            sykefraværMedKategoriSisteKvartalMap,
            Statistikkategori.VIRKSOMHET,
            virksomhet.getOrgnr(),
            virksomhet.getÅrstallOgKvartal());

    assertForespurteKvartalFinnesIStatistikken(årstallOgKvartal, sykefraværMedKategori);

    SykefraværFlereKvartalerForEksport sykefraværOverFlereKvartaler =
        getSykefraværOverFlereKvartaler(sykefraværOverFlereKvartalerMap, virksomhet.getOrgnr());

    if (Objects.equals(
            sykefraværMedKategori,
            SykefraværMedKategori.utenStatistikk(
                Statistikkategori.VIRKSOMHET, virksomhet.getOrgnr(), årstallOgKvartal))
        && Objects.equals(
            sykefraværOverFlereKvartaler, SykefraværFlereKvartalerForEksport.utenStatistikk())) {
      antallUtenStatistikk.incrementAndGet();
    }



    boolean erSent =
        kafkaService.sendTilStatistikkKategoriTopic(
            årstallOgKvartal,
            Statistikkategori.VIRKSOMHET,
            virksomhet.getOrgnr(),
            sykefraværMedKategori,
            sykefraværOverFlereKvartaler);
    long stopUtsendingProcess = System.nanoTime();
    kafkaService.addUtsendingTilKafkaProcessingTime(startUtsendingProcess, stopUtsendingProcess);
    if (erSent) {
      antallEksportert.incrementAndGet();
      int antallVirksomhetertLagretSomEksportert =
          leggTilOrgnrIEksporterteVirksomheterListaOglagreIDbNårListaErFull(
              virksomhet.getOrgnr(),
              årstallOgKvartal,
              eksporterteVirksomheterListe,
              eksporteringRepository,
              kafkaService);
      antallVirksomheterLagretSomEksportertIDb.addAndGet(antallVirksomhetertLagretSomEksportert);
    } else {
      antallIkkeEksportert.incrementAndGet();
    }
  }

  private SykefraværFlereKvartalerForEksport getSykefraværOverFlereKvartaler(
      Map<String, SykefraværFlereKvartalerForEksport> sykefraværOverFlereKvartalerMap,
      String identifikator) {

    SykefraværFlereKvartalerForEksport sykefraværFlereKvartalerForEksport =
        sykefraværOverFlereKvartalerMap.get(identifikator);
    return Objects.requireNonNullElseGet(
        sykefraværFlereKvartalerForEksport, SykefraværFlereKvartalerForEksport::utenStatistikk);
  }

  private static SykefraværMedKategori getSykefraværMedKategori(
      Map<String, SykefraværMedKategori> sykefraværMedKategoriSisteKvartalMap,
      Statistikkategori statistikkategori,
      String identifikator,
      ÅrstallOgKvartal årstallOgKvartal) {
    SykefraværMedKategori sykefraværMedKategori =
        sykefraværMedKategoriSisteKvartalMap.get(identifikator);
    return Objects.requireNonNullElseGet(
        sykefraværMedKategori,
        () ->
            SykefraværMedKategori.utenStatistikk(
                statistikkategori, identifikator, årstallOgKvartal));
  }

  private Map<String, SykefraværMedKategori> createSykefraværMedKategoriMap(
      Map<String, List<SykefraværsstatistikkVirksomhetUtenVarighet>> sykefraværGruppertEtterOrgNr) {
    Map<String, SykefraværMedKategori> sykefraværSisteKvartalPerOrg = new HashMap<>();

    sykefraværGruppertEtterOrgNr.forEach(
        (orgnr, sykefravær) -> {
          SykefraværsstatistikkVirksomhetUtenVarighet sykefraværSisteKvartal =
              sykefravær.stream()
                  .max(
                      Comparator.comparing(
                          kvartal ->
                              new ÅrstallOgKvartal(kvartal.getÅrstall(), kvartal.getKvartal())))
                  .get();
          SykefraværMedKategori sykefraværMedKategori =
              new SykefraværMedKategori(
                  Statistikkategori.VIRKSOMHET,
                  sykefraværSisteKvartal.getOrgnr(),
                  new ÅrstallOgKvartal(
                      sykefraværSisteKvartal.getÅrstall(), sykefraværSisteKvartal.getKvartal()),
                  sykefraværSisteKvartal.getTapteDagsverk(),
                  sykefraværSisteKvartal.getMuligeDagsverk(),
                  sykefraværSisteKvartal.getAntallPersoner());
          sykefraværSisteKvartalPerOrg.put(orgnr, sykefraværMedKategori);
        });

    return sykefraværSisteKvartalPerOrg;
  }

  private UmaskertSykefraværForEttKvartal getSykefraværSisteKvartal(
      List<UmaskertSykefraværForEttKvartal> sykefraværFlereKvartaler) {
    return sykefraværFlereKvartaler.stream()
        .max(Comparator.comparing(UmaskertSykefraværForEttKvartal::getÅrstallOgKvartal))
        .get();
  }

  private Map<String, SykefraværFlereKvartalerForEksport> createSykefraværOverFlereKvartalerMap(
      Map<String, List<SykefraværsstatistikkVirksomhetUtenVarighet>> sykefraværGruppertEtterOrgNr) {
    Map<String, SykefraværFlereKvartalerForEksport> sykefraværOverFlereKvartalerPerOrgNr =
        new HashMap<>();
    sykefraværGruppertEtterOrgNr.forEach(
        (orgnr, sykefravær) -> {
          List<UmaskertSykefraværForEttKvartal> umaskertSykefravær =
              sykefravær.stream()
                  .map(
                      item ->
                          new UmaskertSykefraværForEttKvartal(
                              new ÅrstallOgKvartal(item.getÅrstall(), item.getKvartal()),
                              item.getTapteDagsverk(),
                              item.getMuligeDagsverk(),
                              item.getAntallPersoner()))
                  .collect(Collectors.toList());

          sykefraværOverFlereKvartalerPerOrgNr.put(
              orgnr, new SykefraværFlereKvartalerForEksport(umaskertSykefravær));
        });

    return sykefraværOverFlereKvartalerPerOrgNr;
  }

  private static void logStatusrapport(
      Statistikkategori kategori,
      AtomicInteger antallEksportert,
      AtomicInteger antallIkkeEksportert,
      long tidsbruktIMillisekund) {
    log.info(
        format(
            "Eksport av statistikk for kategori %s er ferdig. "
                + "Antall statistikk eksportert er: %d, "
                + "antall ikke eksportert er: %d. "
                + "Tid for eksportering (i millis): %d",
            kategori, antallEksportert.get(), antallIkkeEksportert.get(), tidsbruktIMillisekund));
  }
}
