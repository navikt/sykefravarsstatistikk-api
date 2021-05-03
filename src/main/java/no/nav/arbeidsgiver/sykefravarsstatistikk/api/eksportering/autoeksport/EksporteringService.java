package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.*;
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
import java.util.List;
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

    public static final int EKSPORT_BATCH_STØRRELSE = 1000;

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

    protected static String listeAvVirksomheterSomString(List<VirksomhetEksportPerKvartal> virksomheterTilEksport) {
        StringBuffer buffer = new StringBuffer();
        AtomicInteger counter = new AtomicInteger(0);

        virksomheterTilEksport.stream().forEach(v -> {
            if (counter.get() != 0) {
                buffer.append("; ");
            }
            buffer.append(v.getOrgnr());
            buffer.append(":");
            buffer.append(v.getÅrstall());
            buffer.append("/");
            buffer.append(v.getKvartal());
            buffer.append(":");
            buffer.append(v.eksportert());
            counter.incrementAndGet();
        });
        return buffer.toString();
    }

    public int eksporter(ÅrstallOgKvartal årstallOgKvartal, EksporteringBegrensning eksporteringBegrensning) {

        if (!erEksporteringAktivert) {
            log.info("Eksportering er ikke aktivert.");
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
        List<VirksomhetMetadata> virksomhetMetadataListe =
                virksomhetMetadataRepository.hentVirksomhetMetadata(årstallOgKvartal);
        SykefraværsstatistikkLand sykefraværsstatistikkLand =
                sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentLand(årstallOgKvartal);
        List<SykefraværsstatistikkSektor> sykefraværsstatistikkSektor =
                sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleSektorer(årstallOgKvartal);
        List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring =
                sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleNæringer(årstallOgKvartal);
        List<SykefraværsstatistikkNæring5Siffer> sykefraværsstatistikkNæring5Siffer =
                sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleNæringer5Siffer(årstallOgKvartal);
        List<SykefraværsstatistikkVirksomhetUtenVarighet> sykefraværsstatistikkVirksomhetUtenVarighet =
                sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleVirksomheter(årstallOgKvartal);

        SykefraværMedKategori landSykefravær = getSykefraværMedKategoriForLand(
                årstallOgKvartal,
                sykefraværsstatistikkLand
        );

        kafkaService.nullstillUtsendingRapport();

        List<? extends List<VirksomhetEksportPerKvartal>> subsets =
                Lists.partition(virksomheterTilEksport, EKSPORT_BATCH_STØRRELSE);
        AtomicInteger antallEksportert = new AtomicInteger();

        log.info("Starter utsending av {} meldinger", subsets.size());
        subsets.forEach(subset -> {
                    sendIBatch(
                            subset,
                            årstallOgKvartal,
                            virksomhetMetadataListe,
                            sykefraværsstatistikkSektor,
                            sykefraværsstatistikkNæring,
                            sykefraværsstatistikkNæring5Siffer,
                            sykefraværsstatistikkVirksomhetUtenVarighet,
                            landSykefravær,
                            antallEksportert,
                            virksomheterTilEksport.size()
                    );
                }
        );

        log.info("Eksport er ferdig med: antall prosessert='{}', antall bekreftet eksportert='{}' og antall error='{}'. " +
                        "Snitt prossessering tid ved utsending til Kafka er: '{}'. Snitt tid for å oppdater DB er: '{}'",
                kafkaService.getAntallMeldingerMottattForUtsending(),
                kafkaService.getAntallMeldingerSent(),
                kafkaService.getAntallMeldingerIError(),
                kafkaService.getSnittTidUtsendingTilKafka(),
                kafkaService.getSnittTidOppdateringIDB()
        );
        return antallEksportert.get();
    }

    //TODO test med sending av REN liste uten prosessering av data og sjekk om prosessering tar tid eller kafkasending som tar tid.
    protected void sendIBatch(
            List<VirksomhetEksportPerKvartal> virksomheterTilEksport,
            ÅrstallOgKvartal årstallOgKvartal,
            List<VirksomhetMetadata> virksomhetMetadataListe,
            List<SykefraværsstatistikkSektor> sykefraværsstatistikkSektor,
            List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring,
            List<SykefraværsstatistikkNæring5Siffer> sykefraværsstatistikkNæring5Siffer,
            List<SykefraværsstatistikkVirksomhetUtenVarighet> sykefraværsstatistikkVirksomhetUtenVarighet,
            SykefraværMedKategori landSykefravær,
            AtomicInteger antallEksportert, int antallTotaltStatistikk) {
        AtomicInteger antallSentTilEksportOgOppdatertIDatabase = new AtomicInteger();


        virksomheterTilEksport.stream().forEach(virksomhetTilEksport -> {
                    long startUtsendingProcess = System.nanoTime();
                    VirksomhetMetadata virksomhetMetadata = getVirksomhetMetada(
                            new Orgnr(virksomhetTilEksport.getOrgnr()),
                            virksomhetTilEksport.getÅrstallOgKvartal(),
                            virksomhetMetadataListe
                    );

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
                                landSykefravær
                        );

                        long stopUtsendingProcess = System.nanoTime();
                        long startWriteToDB = System.nanoTime();
                        eksporteringRepository.oppdaterTilEksportert(virksomhetTilEksport); // dette er ikke gratis ;)
                        long stopWriteToDB = System.nanoTime();
                        antallSentTilEksportOgOppdatertIDatabase.getAndIncrement();
                        kafkaService.addProcessingTime(
                                startUtsendingProcess,
                                stopUtsendingProcess,
                                startWriteToDB,
                                stopWriteToDB
                        );
                    }
                }
        );
        int eksportertHittilNå = antallEksportert.addAndGet(antallSentTilEksportOgOppdatertIDatabase.get());
        log.info(
                String.format(
                        "Eksportert '%d' rader av '%d' totalt",
                        eksportertHittilNå,
                        antallTotaltStatistikk
                )
        );
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
            ÅrstallOgKvartal årstallOgKvartal,
            List<VirksomhetMetadata> virksomhetMetadataListe
    ) {
        List<VirksomhetMetadata> virksomhetMetadataFunnet =
                virksomhetMetadataListe.stream().filter(
                        v -> v.getOrgnr().equals(orgnr.getVerdi())
                                && v.getÅrstall() == årstallOgKvartal.getÅrstall()
                                && v.getKvartal() == årstallOgKvartal.getKvartal()
                ).collect(Collectors.toList());

        if (virksomhetMetadataFunnet == null || virksomhetMetadataFunnet.size() != 1) {
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
                "",
                new ÅrstallOgKvartal(virksomhetMetadata.getÅrstall(), virksomhetMetadata.getKvartal()),
                sfStatistikk.getTapteDagsverk(),
                sfStatistikk.getMuligeDagsverk(),
                sfStatistikk.getAntallPersoner()
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
                                næring5Siffer.getNæringkode5siffer().equals(virksomhetNæring5Siffer.getNæringskode5Siffer())))
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
