package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import com.google.common.collect.Lists;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.utils.EitherUtils;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaUtsendingException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.VirksomhetSykefravær;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.utils.CollectionUtils.concat;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal.SISTE_PUBLISERTE_KVARTAL;

@Slf4j
@Component
public class EksporteringService {

    private final EksporteringRepository eksporteringRepository;
    private final VirksomhetMetadataRepository virksomhetMetadataRepository;
    private final SykefraværsstatistikkTilEksporteringRepository sykefraværsstatistikkTilEksporteringRepository;
    private final SykefraværRepository sykefraværRepository;
    private final KafkaService kafkaService;
    private final boolean erEksporteringAktivert;
    private final BransjeEllerNæringService bransjeEllerNæringService;

    public static final int OPPDATER_VIRKSOMHETER_SOM_ER_EKSPORTERT_BATCH_STØRRELSE = 1000;
    public static final int EKSPORT_BATCH_STØRRELSE = 10000;

    public EksporteringService(
          EksporteringRepository eksporteringRepository,
          VirksomhetMetadataRepository virksomhetMetadataRepository,
          SykefraværsstatistikkTilEksporteringRepository sykefraværsstatistikkTilEksporteringRepository,
          SykefraværRepository sykefraværRepository, KafkaService kafkaService,
          @Value("${statistikk.eksportering.aktivert}") Boolean erEksporteringAktivert,
          BransjeEllerNæringService bransjeEllerNæringService) {
        this.eksporteringRepository = eksporteringRepository;
        this.virksomhetMetadataRepository = virksomhetMetadataRepository;
        this.sykefraværsstatistikkTilEksporteringRepository = sykefraværsstatistikkTilEksporteringRepository;
        this.sykefraværRepository = sykefraværRepository;
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
        // TODO har starter endringer, bør vi hente siste fire kvartaler(her skjer ikke noe utregning  av
        //  prosent eller maskering , OG vi må legge til kvartaler i beregningen.
/*

        SykefraværsstatistikkLand sykefraværsstatistikkLand =
                sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentLand(årstallOgKvartal);
*/
        List<UmaskertSykefraværForEttKvartal> umaskertSykefraværsstatistikkSiste4KvartalerLand =
              sykefraværRepository.hentUmaskertSykefraværForNorge(SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3));
        Aggregeringskalkulator aggregeringskalkulatorLand = new Aggregeringskalkulator(
              new Sykefraværsdata(
                    Map.of(Statistikkategori.LAND, umaskertSykefraværsstatistikkSiste4KvartalerLand)
              )
        );
        // Sektor trenger IKKE noe endring nå
        List<SykefraværsstatistikkSektor> sykefraværsstatistikkSektor =
              sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleSektorer(årstallOgKvartal);
        // TODO her skal vi endre til å bruke den nye uthenting
        List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring =
              sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleNæringerSiste4Kvartaler(årstallOgKvartal.minusKvartaler(3));

        List<SykefraværsstatistikkNæring5Siffer> sykefraværsstatistikkNæring5Siffer =
              sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleNæringer5SifferSiste4Kvartaler(
                    årstallOgKvartal
              );
        List<SykefraværsstatistikkVirksomhetUtenVarighet> sykefraværsstatistikkVirksomhetUtenVarighet =
              sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleVirksomheter(årstallOgKvartal);

        Map<String, SykefraværsstatistikkVirksomhetUtenVarighet> sykefraværsstatistikkVirksomhetUtenVarighetMap =
              toMap(sykefraværsstatistikkVirksomhetUtenVarighet);

        SykefraværMedKategori landSykefravær = getSykefraværMedKategoriForLand(
              årstallOgKvartal,
              mapToSykefraværsstatistikkLand(
                    hentSistePubliserteKvartal(umaskertSykefraværsstatistikkSiste4KvartalerLand))
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
                        sykefraværsstatistikkNæring5Siffer,
                        sykefraværsstatistikkVirksomhetUtenVarighetMap,
                        landSykefravær,
                        aggregeringskalkulatorLand.fraværsprosentNorge(),
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

    private UmaskertSykefraværForEttKvartal hentSistePubliserteKvartal(
          List<UmaskertSykefraværForEttKvartal> umaskertSykefraværsstatistikkSiste4KvartalerLand) {
        return umaskertSykefraværsstatistikkSiste4KvartalerLand.
              stream().
              filter(u ->
                    u.getÅrstallOgKvartal().equals(SISTE_PUBLISERTE_KVARTAL)
              ).findFirst().orElse(null);
    }

    private SykefraværsstatistikkLand mapToSykefraværsstatistikkLand(UmaskertSykefraværForEttKvartal umaskertSykefraværForEttKvartal) {
        return new SykefraværsstatistikkLand(
              umaskertSykefraværForEttKvartal.getÅrstall(),
              umaskertSykefraværForEttKvartal.getKvartal(),
              umaskertSykefraværForEttKvartal.getAntallPersoner(),
              umaskertSykefraværForEttKvartal.getTapteDagsverk(),
              umaskertSykefraværForEttKvartal.getMuligeDagsverk()
        );
    }

    private Map<String, SykefraværsstatistikkVirksomhetUtenVarighet> toMap(List<SykefraværsstatistikkVirksomhetUtenVarighet> sykefraværsstatistikkVirksomhetUtenVarighet) {
        Map<String, SykefraværsstatistikkVirksomhetUtenVarighet> map = new HashMap<>();
        sykefraværsstatistikkVirksomhetUtenVarighet.forEach(sf -> {
            map.put(sf.getOrgnr(), sf);
        });

        return map;
    }

    protected void sendIBatch(
          List<VirksomhetMetadata> virksomheterMetadata,
          ÅrstallOgKvartal årstallOgKvartal,
          List<SykefraværsstatistikkSektor> sykefraværsstatistikkSektor,
          List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring,
          List<SykefraværsstatistikkNæring5Siffer> sykefraværsstatistikkNæring5Siffer,
          Map<String, SykefraværsstatistikkVirksomhetUtenVarighet> sykefraværsstatistikkVirksomhetUtenVarighet,
          SykefraværMedKategori landSykefravær,
          Either<StatistikkException, StatistikkDto> statistikkDtoLand,
          AtomicInteger antallEksportert,
          int antallTotaltStatistikk
    ) {
        AtomicInteger antallSentTilEksport = new AtomicInteger();
        AtomicInteger antallVirksomheterLagretSomEksportertIDb = new AtomicInteger();
        List<String> eksporterteVirksomheterListe = new ArrayList<>();
        // TODO har starter utregning og utsending
        //  har bør vi endre til å bruke Kalkurer for utregning av prosent.
        //  husk å TA MED KvartalerIBeregningen, siden her har vi endret til at de er med.
        virksomheterMetadata.stream().forEach(
              virksomhetMetadata -> {
                  long startUtsendingProcess = System.nanoTime();
                  BransjeEllerNæring bransjeEllerNæring = bransjeEllerNæringService
                        .finnBransjeFraMetadata(virksomhetMetadata);

                  if (virksomhetMetadata != null) {
                      kafkaService.send(
                            årstallOgKvartal,
                            getVirksomhetSykefravær(
                                  virksomhetMetadata,
                                  sykefraværsstatistikkVirksomhetUtenVarighet
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
                            landSykefravær,
                            concat(
                                  EitherUtils.filterRights(statistikkDtoLand),
                                  getSykefraværMedKategoriForBransjeEllerNæringSiste4Kvartaler(
                                        virksomhetMetadata,
                                        sykefraværsstatistikkNæring,
                                        sykefraværsstatistikkNæring5Siffer,
                                        årstallOgKvartal.minusKvartaler(3),
                                        bransjeEllerNæring
                                  )
                            )
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
    protected static Map<String, VirksomhetMetadata> getVirksomhetMetadataHashMap(
          @NotNull List<VirksomhetMetadata> virksomhetMetadataListe
    ) {
        HashMap<String, VirksomhetMetadata> virksomhetMetadataHashMap = new HashMap<>();
        virksomhetMetadataListe.stream().forEach(
              v -> virksomhetMetadataHashMap.put(v.getOrgnr(), v)
        );

        return virksomhetMetadataHashMap;
    }

    @NotNull
    protected static List<VirksomhetMetadata> getVirksomheterMetadataFraSubset(
          Map<String, VirksomhetMetadata> virksomhetMetadataHashMap,
          List<VirksomhetEksportPerKvartal> subset
    ) {
        List<VirksomhetMetadata> virksomheterMetadata = new ArrayList<>();
        subset.stream().forEach(v -> {
            if (virksomhetMetadataHashMap.containsKey(v.getOrgnr())) {
                virksomheterMetadata.add(virksomhetMetadataHashMap.get(v.getOrgnr()));
            }
        });

        return virksomheterMetadata;
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

    protected static long getAntallSomKanEksporteres(List<VirksomhetEksportPerKvartal> virksomhetEksportPerKvartal) {
        return virksomhetEksportPerKvartal.stream().filter(v -> !v.eksportert()).count();
    }

    protected static VirksomhetMetadata getVirksomhetMetada(
          Orgnr orgnr,
          List<VirksomhetMetadata> virksomhetMetadataSet
    ) {
        List<VirksomhetMetadata> virksomhetMetadataFunnet =
              virksomhetMetadataSet.stream().filter(
                    v -> v.getOrgnr().equals(orgnr.getVerdi())
              ).collect(Collectors.toList());

        if (virksomhetMetadataFunnet.size() != 1) {
            return null;
        } else {
            return virksomhetMetadataFunnet.get(0);
        }
    }

    protected static VirksomhetSykefravær getVirksomhetSykefravær(
          VirksomhetMetadata virksomhetMetadata,
          List<SykefraværsstatistikkVirksomhetUtenVarighet> sykefraværsstatistikkVirksomhetUtenVarighet
    ) {
        SykefraværsstatistikkVirksomhetUtenVarighet sfStatistikk =
              sykefraværsstatistikkVirksomhetUtenVarighet.stream().filter(
                    v -> v.getOrgnr().equals(virksomhetMetadata.getOrgnr())
                          && v.getÅrstall() == virksomhetMetadata.getÅrstall()
                          && v.getKvartal() == virksomhetMetadata.getKvartal()
              ).collect(toSingleton(
                    new SykefraværsstatistikkVirksomhetUtenVarighet(
                          virksomhetMetadata.getÅrstall(),
                          virksomhetMetadata.getKvartal(),
                          virksomhetMetadata.getOrgnr(),
                          0,
                          null,
                          null
                    )
              ));

        return new VirksomhetSykefravær(
              virksomhetMetadata.getOrgnr(),
              virksomhetMetadata.getNavn(),
              new ÅrstallOgKvartal(virksomhetMetadata.getÅrstall(), virksomhetMetadata.getKvartal()),
              sfStatistikk.getTapteDagsverk(),
              sfStatistikk.getMuligeDagsverk(),
              sfStatistikk.getAntallPersoner()
        );
    }

    protected static VirksomhetSykefravær getVirksomhetSykefravær(
          VirksomhetMetadata virksomhetMetadata,
          Map<String, SykefraværsstatistikkVirksomhetUtenVarighet> sykefraværsstatistikkVirksomhetUtenVarighet
    ) {
        SykefraværsstatistikkVirksomhetUtenVarighet sfStatistikk =
              sykefraværsstatistikkVirksomhetUtenVarighet.get(virksomhetMetadata.getOrgnr());

        return new VirksomhetSykefravær(
              virksomhetMetadata.getOrgnr(),
              virksomhetMetadata.getNavn(),
              new ÅrstallOgKvartal(virksomhetMetadata.getÅrstall(), virksomhetMetadata.getKvartal()),
              sfStatistikk != null ? sfStatistikk.getTapteDagsverk() : null,
              sfStatistikk != null ? sfStatistikk.getMuligeDagsverk() : null,
              sfStatistikk != null ? sfStatistikk.getAntallPersoner() : 0
        );
    }

    protected static SykefraværMedKategori getSykefraværMedKategoriForLand(
          ÅrstallOgKvartal årstallOgKvartal,
          SykefraværsstatistikkLand sykefraværsstatistikkLand
    ) {
        return new SykefraværMedKategori(
              Statistikkategori.LAND,
              "NO",
              årstallOgKvartal,
              sykefraværsstatistikkLand.getTapteDagsverk(),
              sykefraværsstatistikkLand.getMuligeDagsverk(),
              sykefraværsstatistikkLand.getAntallPersoner()
        );
    }

    protected static SykefraværMedKategori getSykefraværMedKategoriForSektor(
          VirksomhetMetadata virksomhetMetadata,
          List<SykefraværsstatistikkSektor> sykefraværsstatistikkSektor
    ) {
        SykefraværsstatistikkSektor sfSektor =
              sykefraværsstatistikkSektor.stream().filter(
                    v -> v.getSektorkode().equals(virksomhetMetadata.getSektor())
                          && v.getÅrstall() == virksomhetMetadata.getÅrstall()
                          && v.getKvartal() == virksomhetMetadata.getKvartal()
              ).collect(toSingleton(
                    new SykefraværsstatistikkSektor(
                          virksomhetMetadata.getÅrstall(),
                          virksomhetMetadata.getKvartal(),
                          virksomhetMetadata.getSektor(),
                          0,
                          null,
                          null
                    )
              ));

        return new SykefraværMedKategori(
              Statistikkategori.SEKTOR,
              sfSektor.getSektorkode(),
              new ÅrstallOgKvartal(virksomhetMetadata.getÅrstall(), virksomhetMetadata.getKvartal()),
              sfSektor.getTapteDagsverk(),
              sfSektor.getMuligeDagsverk(),
              sfSektor.getAntallPersoner()
        );
    }

    protected static SykefraværMedKategori getSykefraværMedKategoriForNæring(
          VirksomhetMetadata virksomhetMetadata,
          List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring
    ) {
        SykefraværsstatistikkNæring sfNæring =
              sykefraværsstatistikkNæring.stream().filter(
                    v -> v.getNæringkode().equals(virksomhetMetadata.getNæring())
                          && v.getÅrstall() == virksomhetMetadata.getÅrstall()
                          && v.getKvartal() == virksomhetMetadata.getKvartal()
              ).collect(toSingleton(
                    new SykefraværsstatistikkNæring(
                          virksomhetMetadata.getÅrstall(),
                          virksomhetMetadata.getKvartal(),
                          virksomhetMetadata.getNæring(),
                          0,
                          null,
                          null
                    )
              ));

        return new SykefraværMedKategori(
              Statistikkategori.NÆRING2SIFFER,
              sfNæring.getNæringkode(),
              new ÅrstallOgKvartal(virksomhetMetadata.getÅrstall(), virksomhetMetadata.getKvartal()),
              sfNæring.getTapteDagsverk(),
              sfNæring.getMuligeDagsverk(),
              sfNæring.getAntallPersoner()
        );
    }

    protected static List<StatistikkDto> getSykefraværMedKategoriForBransjeEllerNæringSiste4Kvartaler(
          VirksomhetMetadata virksomhetMetadata,
          List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring,
          List<SykefraværsstatistikkNæring5Siffer> sykefraværsstatistikkNæring5Siffer,
          ÅrstallOgKvartal fraÅrstallOgKvartal,
          BransjeEllerNæring bransjeEllerNæring
    ) {
        Aggregeringskalkulator aggregeringskalkulator;
        if (bransjeEllerNæring.isBransje()) {
            List<UmaskertSykefraværForEttKvartal> sykefraværsstatistikkBransje
                  = sykefraværsstatistikkNæring5Siffer.stream().filter(
                        br -> bransjeEllerNæring.getBransje().getKoderSomSpesifisererNæringer()
                              .contains(br.getNæringkode5siffer())
                              && (
                              (br.getÅrstall() > fraÅrstallOgKvartal.getÅrstall())
                                    ||
                                    (
                                          br.getÅrstall() == fraÅrstallOgKvartal.getÅrstall()
                                                && br.getKvartal() >= fraÅrstallOgKvartal.getKvartal()
                                    )
                        )
                  ).map(EksporteringService::mapTilUmaskertSykefraværForEttKvartal)
                  .collect(Collectors.toList());
            aggregeringskalkulator = new Aggregeringskalkulator(
                  new Sykefraværsdata(Map.of(Statistikkategori.BRANSJE, sykefraværsstatistikkBransje))
            );
            // TODO hente og returnere statistikkDTO for bransje
        } else { // TODO sjekk om det er riktig henting for næring/næringer
        /*SykefraværsstatistikkNæring sfNæring =
              sykefraværsstatistikkNæring.stream().filter(
                    v -> v.getNæringkode().equals(virksomhetMetadata.getNæring())
                          && v.getÅrstall() == virksomhetMetadata.getÅrstall()
                          && v.getKvartal() == virksomhetMetadata.getKvartal()
                          && (
                                (v.getÅrstall() > fraÅrstallOgKvartal.getÅrstall())
                                      ||
                                      (
                                            v.getÅrstall() == fraÅrstallOgKvartal.getÅrstall()
                                            && v.getKvartal() >= fraÅrstallOgKvartal.getKvartal()
                                      )
                    )
              ).collect(toSingleton(
                    new SykefraværsstatistikkNæring(
                          virksomhetMetadata.getÅrstall(),
                          virksomhetMetadata.getKvartal(),
                          virksomhetMetadata.getNæring(),
                          0,
                          null,
                          null
                    )
              ));
*/
            List<UmaskertSykefraværForEttKvartal> umaskertSykefraværForEttKvartaler =
                  sykefraværsstatistikkNæring.stream().filter(
                        v -> v.getNæringkode().equals(virksomhetMetadata.getNæring())
                              && (
                              (v.getÅrstall() > fraÅrstallOgKvartal.getÅrstall())
                                    ||
                                    (
                                          v.getÅrstall() == fraÅrstallOgKvartal.getÅrstall()
                                                && v.getKvartal() >= fraÅrstallOgKvartal.getKvartal()
                                    )
                        )
                  ).map(
                        EksporteringService::mapTilUmaskertSykefraværForEttKvartal
                  ).collect(Collectors.toList());
            aggregeringskalkulator = new Aggregeringskalkulator(
                  new Sykefraværsdata(
                        Map.of(Statistikkategori.NÆRING, umaskertSykefraværForEttKvartaler)
                  )
            );
        }
        return EitherUtils.filterRights(aggregeringskalkulator.fraværsprosentBransjeEllerNæring(bransjeEllerNæring));
    }

    protected static List<SykefraværMedKategori> getSykefraværMedKategoriForNæring5Siffer(
          VirksomhetMetadata virksomhetMetadata,
          List<SykefraværsstatistikkNæring5Siffer> sykefraværsstatistikkNæring5SifferList
    ) {

        List<SykefraværsstatistikkNæring5Siffer> filteredList =
              getSykefraværsstatistikkNæring5Siffers(virksomhetMetadata, sykefraværsstatistikkNæring5SifferList);

        List<SykefraværMedKategori> resultatList = new ArrayList();

        filteredList.stream().forEach(
              sfNæring5Siffer -> {
                  resultatList.add(
                        new SykefraværMedKategori(
                              Statistikkategori.NÆRING5SIFFER,
                              sfNæring5Siffer.getNæringkode5siffer(),
                              new ÅrstallOgKvartal(
                                    virksomhetMetadata.getÅrstall(),
                                    virksomhetMetadata.getKvartal()
                              ),
                              sfNæring5Siffer.getTapteDagsverk(),
                              sfNæring5Siffer.getMuligeDagsverk(),
                              sfNæring5Siffer.getAntallPersoner()
                        )
                  );
              }
        );

        return resultatList;
    }

    @NotNull
    protected static List<SykefraværsstatistikkNæring5Siffer> getSykefraværsstatistikkNæring5Siffers(
          VirksomhetMetadata virksomhetMetadata,
          List<SykefraværsstatistikkNæring5Siffer> sykefraværsstatistikkNæring5SifferList) {
        List<SykefraværsstatistikkNæring5Siffer> filteredList = sykefraværsstatistikkNæring5SifferList.stream()
              .filter(næring5Siffer -> virksomhetMetadata.getNæringOgNæringskode5siffer().stream()
                    .anyMatch(virksomhetNæring5Siffer ->
                          næring5Siffer.getNæringkode5siffer().equals(
                                virksomhetNæring5Siffer.getNæringskode5Siffer()
                          )
                    )
              )
              .collect(Collectors.toList());
        return filteredList;
    }

    private static <T> Collector<T, ?, T> toSingleton(T emptySykefraværsstatistikk) {
        return Collectors.collectingAndThen(
              Collectors.toList(),
              list -> {
                  if (list.size() != 1) {
                      return emptySykefraværsstatistikk;
                  }
                  return list.get(0);
              }
        );
    }

    private static UmaskertSykefraværForEttKvartal mapTilUmaskertSykefraværForEttKvartal(SykefraværsstatistikkNæring sfsNæring) {
        return new UmaskertSykefraværForEttKvartal(
              new ÅrstallOgKvartal(sfsNæring.getÅrstall(), sfsNæring.getKvartal()),
              sfsNæring.getTapteDagsverk(),
              sfsNæring.getMuligeDagsverk(),
              sfsNæring.getAntallPersoner()
        );
    }

    private static UmaskertSykefraværForEttKvartal mapTilUmaskertSykefraværForEttKvartal(SykefraværsstatistikkNæring5Siffer sfsNæring) {
        return new UmaskertSykefraværForEttKvartal(
              new ÅrstallOgKvartal(sfsNæring.getÅrstall(), sfsNæring.getKvartal()),
              sfsNæring.getTapteDagsverk(),
              sfsNæring.getMuligeDagsverk(),
              sfsNæring.getAntallPersoner()
        );
    }
}
