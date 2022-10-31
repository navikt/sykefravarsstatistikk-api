package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import com.google.common.collect.Lists;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.EksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.SykefraværsstatistikkTilEksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetEksportPerKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadata;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadataRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkSektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhetUtenVarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaUtsendingException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.KlassifikasjonerRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.StatistikkException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.Aggregeringskalkulator;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.StatistikkDto;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.Sykefraværsdata;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.KafkaException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.filterByKvartal;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.getSykefraværMedKategoriForLand;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.getSykefraværMedKategoriForNæring;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.getSykefraværMedKategoriForNæring5Siffer;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.getSykefraværMedKategoriForSektor;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.getVirksomhetMetadataHashMap;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.getVirksomhetSykefravær;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.getVirksomheterMetadataFraSubset;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.hentSisteKvartalIBeregningen;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.mapToSykefraværsstatistikkLand;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.toMap;

@Slf4j
@Component
public class EksporteringService {

    private final EksporteringRepository eksporteringRepository;
    private final VirksomhetMetadataRepository virksomhetMetadataRepository;
    private final SykefraværsstatistikkTilEksporteringRepository sykefraværsstatistikkTilEksporteringRepository;
    private final SykefraværRepository sykefraværRepository;
    private final KlassifikasjonerRepository klassifikasjonerRepository;
    private final KafkaService kafkaService;
    private final boolean erEksporteringAktivert;
    private final BransjeEllerNæringService bransjeEllerNæringService;

    public static final int OPPDATER_VIRKSOMHETER_SOM_ER_EKSPORTERT_BATCH_STØRRELSE = 1000;
    public static final int EKSPORT_BATCH_STØRRELSE = 10000;

    public EksporteringService(
          EksporteringRepository eksporteringRepository,
          VirksomhetMetadataRepository virksomhetMetadataRepository,
          SykefraværsstatistikkTilEksporteringRepository sykefraværsstatistikkTilEksporteringRepository,
          SykefraværRepository sykefraværRepository, KlassifikasjonerRepository klassifikasjonerRepository, KafkaService kafkaService,
          @Value("${statistikk.eksportering.aktivert}") Boolean erEksporteringAktivert,
          BransjeEllerNæringService bransjeEllerNæringService) {
        this.eksporteringRepository = eksporteringRepository;
        this.virksomhetMetadataRepository = virksomhetMetadataRepository;
        this.sykefraværsstatistikkTilEksporteringRepository = sykefraværsstatistikkTilEksporteringRepository;
        this.sykefraværRepository = sykefraværRepository;
        this.klassifikasjonerRepository = klassifikasjonerRepository;
        this.kafkaService = kafkaService;
        this.erEksporteringAktivert = erEksporteringAktivert;
        this.bransjeEllerNæringService = bransjeEllerNæringService;
    }


    public int eksporter(ÅrstallOgKvartal årstallOgKvartal, EksporteringBegrensning eksporteringBegrensning) {

        if (!erEksporteringAktivert) {
            log.info("Eksportering er ikke aktivert. Avbrytter. ");
            return 0;
        }
        List<VirksomhetEksportPerKvartal> virksomheterTilEksport =
              getListeAvVirksomhetEksportPerKvartal(årstallOgKvartal, eksporteringBegrensning);

        int antallStatistikkSomSkalEksporteres = virksomheterTilEksport.isEmpty() ?
              0 :
              virksomheterTilEksport.size();

        if (antallStatistikkSomSkalEksporteres == 0) {
            log.info("Ingen statistikk å eksportere for årstall '{}' og kvartal '{}'.",
                  årstallOgKvartal.getÅrstall(),
                  årstallOgKvartal.getKvartal()
            );
            return 0;
        }

        log.info(
              "Starting eksportering for årstall '{}' og kvartal '{}'. Skal eksportere '{}' rader med statistikk. ",
              årstallOgKvartal.getÅrstall(),
              årstallOgKvartal.getKvartal(),
              antallStatistikkSomSkalEksporteres
        );
        int antallEksporterteVirksomheter = 0;

        try {
            antallEksporterteVirksomheter = eksporter(virksomheterTilEksport, årstallOgKvartal);
        } catch (KafkaUtsendingException | KafkaException e) {
            log.warn("Fikk Exception fra Kafka med melding:'{}'. Avbryter prosess.", e.getMessage(), e);
        }

        return antallEksporterteVirksomheter;
    }


    protected int eksporter(
          List<VirksomhetEksportPerKvartal> virksomheterTilEksport,
          ÅrstallOgKvartal årstallOgKvartal
    ) throws KafkaUtsendingException {
        long startEksportering = System.currentTimeMillis();
        kafkaService.nullstillUtsendingRapport(virksomheterTilEksport.size());

        List<VirksomhetMetadata> virksomhetMetadataListe =
              virksomhetMetadataRepository.hentVirksomhetMetadata(årstallOgKvartal);

        List<UmaskertSykefraværForEttKvartal> umaskertSykefraværsstatistikkSiste4KvartalerLand =
              sykefraværRepository.hentUmaskertSykefraværForNorge(årstallOgKvartal.minusKvartaler(3));

        List<SykefraværsstatistikkSektor> sykefraværsstatistikkSektor =
              sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleSektorer(årstallOgKvartal);

        List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring =
              sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleNæringerSiste4Kvartaler(årstallOgKvartal.minusKvartaler(3));
        List<Næring> alleNæringer= klassifikasjonerRepository.hentAlleNæringer();

        List<SykefraværsstatistikkNæring5Siffer> sykefraværsstatistikkNæring5Siffer =
              sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleNæringer5SifferSiste4Kvartaler(
                    årstallOgKvartal
              );
        List<SykefraværsstatistikkVirksomhetUtenVarighet> sykefraværsstatistikkVirksomhetUtenVarighet =
              sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleVirksomheter(årstallOgKvartal.minusKvartaler(3));


        SykefraværMedKategori landSykefravær = getSykefraværMedKategoriForLand(
              årstallOgKvartal,
              mapToSykefraværsstatistikkLand(
                    hentSisteKvartalIBeregningen(umaskertSykefraværsstatistikkSiste4KvartalerLand,årstallOgKvartal))
        );

        Map<String, VirksomhetMetadata> virksomhetMetadataMap = getVirksomhetMetadataHashMap(virksomhetMetadataListe);

        List<? extends List<VirksomhetEksportPerKvartal>> subsets =
              Lists.partition(virksomheterTilEksport, EKSPORT_BATCH_STØRRELSE);
        AtomicInteger antallEksportert = new AtomicInteger();

        subsets.forEach(subset -> {
                  List<VirksomhetMetadata> virksomheterMetadataIDenneSubset =
                        getVirksomheterMetadataFraSubset(virksomhetMetadataMap, subset);

                  log.info("Starter utsending av '{}' statistikk meldinger (fra '{}' virksomheter)",
                        virksomheterMetadataIDenneSubset.size(),
                        subset.size()
                  );

                  sendIBatch(
                        virksomheterMetadataIDenneSubset,
                        årstallOgKvartal,
                        sykefraværsstatistikkSektor,
                        sykefraværsstatistikkNæring,
                        alleNæringer,
                        sykefraværsstatistikkNæring5Siffer,
                        sykefraværsstatistikkVirksomhetUtenVarighet,
                        landSykefravær,
                        antallEksportert,
                        virksomheterTilEksport.size()
                  );
              }
        );

        long stoptEksportering = System.currentTimeMillis();
        long totalProsesseringTidISekunder = (stoptEksportering - startEksportering) / 1000;
        log.info("Eksportering er ferdig med: antall statistikk for virksomhet prosessert='{}', " +
                    "Eksportering tok '{}' sekunder totalt. " +
                    "Snitt prossesseringstid ved utsending til Kafka er: '{}'. " +
                    "Snitt prossesseringstid for å oppdatere DB er: '{}'.",
              kafkaService.getAntallMeldingerMottattForUtsending(),
              totalProsesseringTidISekunder,
              kafkaService.getSnittTidUtsendingTilKafka(),
              kafkaService.getSnittTidOppdateringIDB()
        );
        log.info("[Måling] Rå data ved måling: {}",
              kafkaService.getRåDataVedDetaljertMåling()
        );

        return antallEksportert.get();
    }


    protected void sendIBatch(
          List<VirksomhetMetadata> virksomheterMetadata,
          ÅrstallOgKvartal årstallOgKvartal,
          List<SykefraværsstatistikkSektor> sykefraværsstatistikkSektor,
          List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring,
          List<Næring> alleNæringer, List<SykefraværsstatistikkNæring5Siffer> sykefraværsstatistikkNæring5Siffer,
          List<SykefraværsstatistikkVirksomhetUtenVarighet> sykefraværsstatistikkVirksomhetUtenVarighet,
          SykefraværMedKategori landSykefravær,
          AtomicInteger antallEksportert,
          int antallTotaltStatistikk
    ) {
        AtomicInteger antallSentTilEksport = new AtomicInteger();
        AtomicInteger antallVirksomheterLagretSomEksportertIDb = new AtomicInteger();
        List<String> eksporterteVirksomheterListe = new ArrayList<>();
        Map<String, SykefraværsstatistikkVirksomhetUtenVarighet> sykefraværsstatistikkVirksomhetForEttKvartalUtenVarighetMap =
              toMap(filterByKvartal(årstallOgKvartal, sykefraværsstatistikkVirksomhetUtenVarighet));


        virksomheterMetadata.stream().forEach(
              virksomhetMetadata -> {
                  long startUtsendingProcess = System.nanoTime();

                  if (virksomhetMetadata != null) {
                      kafkaService.send(
                            årstallOgKvartal,
                            getVirksomhetSykefravær(
                                  virksomhetMetadata,
                                  sykefraværsstatistikkVirksomhetForEttKvartalUtenVarighetMap
                            ),
                            getSykefraværMedKategoriForNæring5Siffer(
                                  virksomhetMetadata,
                                  sykefraværsstatistikkNæring5Siffer
                            ),
                            getSykefraværMedKategoriForNæring(
                                  virksomhetMetadata,
                                  sykefraværsstatistikkNæring
                            ),
                            getSykefraværMedKategoriForSektor(
                                  virksomhetMetadata,
                                  sykefraværsstatistikkSektor
                            ),
                            landSykefravær
                      );

                      long stopUtsendingProcess = System.nanoTime();
                      antallSentTilEksport.getAndIncrement();
                      kafkaService.addUtsendingTilKafkaProcessingTime(startUtsendingProcess, stopUtsendingProcess);

                      int antallVirksomhetertLagretSomEksportert =
                            leggTilOrgnrIEksporterteVirksomheterListaOglagreIDbNårListaErFull(
                                  virksomhetMetadata.getOrgnr(),
                                  årstallOgKvartal,
                                  eksporterteVirksomheterListe
                            );
                      antallVirksomheterLagretSomEksportertIDb.addAndGet(antallVirksomhetertLagretSomEksportert);
                  }
              }
        );

        int antallRestendeOppdatert = lagreEksporterteVirksomheterOgNullstillLista(
              årstallOgKvartal,
              eksporterteVirksomheterListe
        );
        antallVirksomheterLagretSomEksportertIDb.addAndGet(antallRestendeOppdatert);
        int eksportertHittilNå = antallEksportert.addAndGet(antallSentTilEksport.get());

        cleanUpEtterBatch();

        log.info(
              String.format(
                    "Eksportert '%d' rader av '%d' totalt ('%d' oppdatert i DB)",
                    eksportertHittilNå,
                    antallTotaltStatistikk,
                    antallVirksomheterLagretSomEksportertIDb.get()
              )
        );
    }

    private void cleanUpEtterBatch() {
        eksporteringRepository.oppdaterAlleVirksomheterIEksportTabellSomErBekrreftetEksportert();
        eksporteringRepository.slettVirksomheterBekreftetEksportert();
    }

    private int leggTilOrgnrIEksporterteVirksomheterListaOglagreIDbNårListaErFull(
          String orgnr,
          ÅrstallOgKvartal årstallOgKvartal,
          @NotNull List<String> virksomheterSomSkalFlaggesSomEksportert
    ) {
        virksomheterSomSkalFlaggesSomEksportert.add(orgnr);

        if (virksomheterSomSkalFlaggesSomEksportert.size() == OPPDATER_VIRKSOMHETER_SOM_ER_EKSPORTERT_BATCH_STØRRELSE) {
            return lagreEksporterteVirksomheterOgNullstillLista(årstallOgKvartal, virksomheterSomSkalFlaggesSomEksportert);
        } else {
            return 0;
        }
    }

    private int lagreEksporterteVirksomheterOgNullstillLista(
          ÅrstallOgKvartal årstallOgKvartal,
          List<String> virksomheterSomSkalFlaggesSomEksportert
    ) {
        int antallSomSkalOppdateres = virksomheterSomSkalFlaggesSomEksportert.size();
        long startWriteToDB = System.nanoTime();
        eksporteringRepository.batchOpprettVirksomheterBekreftetEksportert(
              virksomheterSomSkalFlaggesSomEksportert,
              årstallOgKvartal
        );
        virksomheterSomSkalFlaggesSomEksportert.clear();
        long stopWriteToDB = System.nanoTime();

        kafkaService.addDBOppdateringProcessingTime(startWriteToDB, stopWriteToDB);

        return antallSomSkalOppdateres;
    }


    @NotNull
    protected List<VirksomhetEksportPerKvartal> getListeAvVirksomhetEksportPerKvartal(
          ÅrstallOgKvartal årstallOgKvartal,
          EksporteringBegrensning eksporteringBegrensning
    ) {
        List<VirksomhetEksportPerKvartal> virksomhetEksportPerKvartal =
              eksporteringRepository.hentVirksomhetEksportPerKvartal(årstallOgKvartal);


        Stream<VirksomhetEksportPerKvartal> virksomhetEksportPerKvartalStream = virksomhetEksportPerKvartal
              .stream()
              .filter(v -> !v.eksportert());

        return eksporteringBegrensning.erBegrenset() ?
              virksomhetEksportPerKvartalStream.
                    limit(eksporteringBegrensning.getAntallSomSkalEksporteres())
                    .collect(Collectors.toList()) :
              virksomhetEksportPerKvartalStream.collect(Collectors.toList());
    }
}
