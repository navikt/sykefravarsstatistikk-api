package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.EksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.SykefraværsstatistikkTilEksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetEksportPerKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadata;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadataRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Kvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkLand;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkSektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhetUtenVarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaUtsendingException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.VirksomhetSykefravær;
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

@Slf4j
@Component
public class EksporteringService {

    private final EksporteringRepository eksporteringRepository;
    private final VirksomhetMetadataRepository virksomhetMetadataRepository;
    private final SykefraværsstatistikkTilEksporteringRepository sykefraværsstatistikkTilEksporteringRepository;
    private final KafkaService kafkaService;
    private final boolean erEksporteringAktivert;

    public static final int OPPDATER_VIRKSOMHETER_SOM_ER_EKSPORTERT_BATCH_STØRRELSE = 1000;
    public static final int EKSPORT_BATCH_STØRRELSE = 10000;

    public EksporteringService(
            EksporteringRepository eksporteringRepository,
            VirksomhetMetadataRepository virksomhetMetadataRepository,
            SykefraværsstatistikkTilEksporteringRepository sykefraværsstatistikkTilEksporteringRepository,
            KafkaService kafkaService,
            @Value("${statistikk.eksportering.aktivert}") Boolean erEksporteringAktivert
    ) {
        this.eksporteringRepository = eksporteringRepository;
        this.virksomhetMetadataRepository = virksomhetMetadataRepository;
        this.sykefraværsstatistikkTilEksporteringRepository = sykefraværsstatistikkTilEksporteringRepository;
        this.kafkaService = kafkaService;
        this.erEksporteringAktivert = erEksporteringAktivert;
    }


    public int eksporter(Kvartal kvartal, EksporteringBegrensning eksporteringBegrensning) {

        if (!erEksporteringAktivert) {
            log.info("Eksportering er ikke aktivert. Avbrytter. ");
            return 0;
        }
        List<VirksomhetEksportPerKvartal> virksomheterTilEksport =
                getListeAvVirksomhetEksportPerKvartal(kvartal, eksporteringBegrensning);


        int antallStatistikkSomSkalEksporteres = virksomheterTilEksport.isEmpty() ?
                0 :
                virksomheterTilEksport.size();

        if (antallStatistikkSomSkalEksporteres == 0) {
            log.info("Ingen statistikk å eksportere for årstall '{}' og kvartal '{}'.",
                    kvartal.getÅrstall(),
                    kvartal.getKvartal()
            );
            return 0;
        }

        log.info(
                "Starting eksportering for årstall '{}' og kvartal '{}'. Skal eksportere '{}' rader med statistikk. ",
                kvartal.getÅrstall(),
                kvartal.getKvartal(),
                antallStatistikkSomSkalEksporteres
        );
        int antallEksporterteVirksomheter = 0;

        try {
            antallEksporterteVirksomheter = eksporter(virksomheterTilEksport, kvartal);
        } catch (KafkaUtsendingException | KafkaException e) {
            log.warn("Fikk Exception fra Kafka med melding:'{}'. Avbryter prosess.", e.getMessage(), e);
        }

        return antallEksporterteVirksomheter;
    }


    protected int eksporter(
            List<VirksomhetEksportPerKvartal> virksomheterTilEksport,
            Kvartal kvartal
    ) throws KafkaUtsendingException {
        long startEksportering = System.currentTimeMillis();
        kafkaService.nullstillUtsendingRapport(virksomheterTilEksport.size());

        List<VirksomhetMetadata> virksomhetMetadataListe =
                virksomhetMetadataRepository.hentVirksomhetMetadata(kvartal);
        SykefraværsstatistikkLand sykefraværsstatistikkLand =
                sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentLand(kvartal);
        List<SykefraværsstatistikkSektor> sykefraværsstatistikkSektor =
                sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleSektorer(kvartal);
        List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring =
                sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleNæringer(kvartal);
        List<SykefraværsstatistikkNæring5Siffer> sykefraværsstatistikkNæring5Siffer =
                sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleNæringer5Siffer(
                        kvartal
                );
        List<SykefraværsstatistikkVirksomhetUtenVarighet> sykefraværsstatistikkVirksomhetUtenVarighet =
                sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleVirksomheter(kvartal);

        Map<String, SykefraværsstatistikkVirksomhetUtenVarighet> sykefraværsstatistikkVirksomhetUtenVarighetMap =
                toMap(sykefraværsstatistikkVirksomhetUtenVarighet);

        SykefraværMedKategori landSykefravær = getSykefraværMedKategoriForLand(
                kvartal,
                sykefraværsstatistikkLand
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
                            kvartal,
                            sykefraværsstatistikkSektor,
                            sykefraværsstatistikkNæring,
                            sykefraværsstatistikkNæring5Siffer,
                            sykefraværsstatistikkVirksomhetUtenVarighetMap,
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

    private Map<String, SykefraværsstatistikkVirksomhetUtenVarighet> toMap(List<SykefraværsstatistikkVirksomhetUtenVarighet> sykefraværsstatistikkVirksomhetUtenVarighet) {
        Map<String, SykefraværsstatistikkVirksomhetUtenVarighet> map = new HashMap<>();
        sykefraværsstatistikkVirksomhetUtenVarighet.forEach(sf -> {
            map.put(sf.getOrgnr(), sf);
        });

        return map;
    }

    protected void sendIBatch(
            List<VirksomhetMetadata> virksomheterMetadata,
            Kvartal kvartal,
            List<SykefraværsstatistikkSektor> sykefraværsstatistikkSektor,
            List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring,
            List<SykefraværsstatistikkNæring5Siffer> sykefraværsstatistikkNæring5Siffer,
            Map<String, SykefraværsstatistikkVirksomhetUtenVarighet> sykefraværsstatistikkVirksomhetUtenVarighet,
            SykefraværMedKategori landSykefravær,
            AtomicInteger antallEksportert,
            int antallTotaltStatistikk
    ) {
        AtomicInteger antallSentTilEksport = new AtomicInteger();
        AtomicInteger antallVirksomheterLagretSomEksportertIDb = new AtomicInteger();
        List<String> eksporterteVirksomheterListe = new ArrayList<>();

        virksomheterMetadata.stream().forEach(
                virksomhetMetadata -> {
                    long startUtsendingProcess = System.nanoTime();

                    if (virksomhetMetadata != null) {
                        kafkaService.send(
                                kvartal,
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
                                landSykefravær
                        );

                        long stopUtsendingProcess = System.nanoTime();
                        antallSentTilEksport.getAndIncrement();
                        kafkaService.addUtsendingTilKafkaProcessingTime(startUtsendingProcess, stopUtsendingProcess);

                        int antallVirksomhetertLagretSomEksportert =
                                leggTilOrgnrIEksporterteVirksomheterListaOglagreIDbNårListaErFull(
                                        virksomhetMetadata.getOrgnr(),
                                        kvartal,
                                        eksporterteVirksomheterListe
                                );
                        antallVirksomheterLagretSomEksportertIDb.addAndGet(antallVirksomhetertLagretSomEksportert);
                    }
                }
        );

        int antallRestendeOppdatert = lagreEksporterteVirksomheterOgNullstillLista(
                kvartal,
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
            Kvartal kvartal,
            @NotNull List<String> virksomheterSomSkalFlaggesSomEksportert
    ) {
        virksomheterSomSkalFlaggesSomEksportert.add(orgnr);

        if (virksomheterSomSkalFlaggesSomEksportert.size() == OPPDATER_VIRKSOMHETER_SOM_ER_EKSPORTERT_BATCH_STØRRELSE) {
            return lagreEksporterteVirksomheterOgNullstillLista(kvartal, virksomheterSomSkalFlaggesSomEksportert);
        } else {
            return 0;
        }
    }

    private int lagreEksporterteVirksomheterOgNullstillLista(
            Kvartal kvartal,
            List<String> virksomheterSomSkalFlaggesSomEksportert
    ) {
        int antallSomSkalOppdateres = virksomheterSomSkalFlaggesSomEksportert.size();
        long startWriteToDB = System.nanoTime();
        eksporteringRepository.batchOpprettVirksomheterBekreftetEksportert(
                virksomheterSomSkalFlaggesSomEksportert,
                kvartal
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
            Kvartal kvartal,
            EksporteringBegrensning eksporteringBegrensning
    ) {
        List<VirksomhetEksportPerKvartal> virksomhetEksportPerKvartal =
                eksporteringRepository.hentVirksomhetEksportPerKvartal(kvartal);


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
                new Kvartal(virksomhetMetadata.getÅrstall(), virksomhetMetadata.getKvartal()),
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
                new Kvartal(virksomhetMetadata.getÅrstall(), virksomhetMetadata.getKvartal()),
                sfStatistikk != null ? sfStatistikk.getTapteDagsverk() : null,
                sfStatistikk != null ? sfStatistikk.getMuligeDagsverk() : null,
                sfStatistikk != null ? sfStatistikk.getAntallPersoner() : 0
        );
    }

    protected static SykefraværMedKategori getSykefraværMedKategoriForLand(
            Kvartal kvartal,
            SykefraværsstatistikkLand sykefraværsstatistikkLand
    ) {
        return new SykefraværMedKategori(
                Statistikkategori.LAND,
                "NO",
                kvartal,
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
                new Kvartal(virksomhetMetadata.getÅrstall(), virksomhetMetadata.getKvartal()),
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
                new Kvartal(virksomhetMetadata.getÅrstall(), virksomhetMetadata.getKvartal()),
                sfNæring.getTapteDagsverk(),
                sfNæring.getMuligeDagsverk(),
                sfNæring.getAntallPersoner()
        );
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
                                    new Kvartal(
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
}
