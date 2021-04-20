package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.VirksomhetSykefravær;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EksporteringService {

    private final EksporteringRepository eksporteringRepository;
    private final VirksomhetMetadataRepository virksomhetMetadataRepository;
    private final SykefraværsstatistikkTilEksporteringRepository sykefraværsstatistikkTilEksporteringRepository;
    private final KafkaService kafkaService;
    private final boolean erEksporteringAktivert;

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

    public int eksporter(ÅrstallOgKvartal årstallOgKvartal) {

        if (!erEksporteringAktivert) {
            log.info("Eksportering er ikke aktivert.");
            return 0;
        }
        List<VirksomhetEksportPerKvartal> virksomhetEksportPerKvartal =
                eksporteringRepository.hentVirksomhetEksportPerKvartal(årstallOgKvartal);

        List<VirksomhetEksportPerKvartal> virksomheterTilEksport =
                virksomhetEksportPerKvartal.stream()
                        .filter(v -> !v.eksportert())
                        .collect(Collectors.toList());

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

        return eksporter(virksomheterTilEksport, årstallOgKvartal);
    }

    protected int eksporter(
            List<VirksomhetEksportPerKvartal> virksomheterTilEksport,
            ÅrstallOgKvartal årstallOgKvartal
    ) {
        List<VirksomhetMetadata> virksomhetMetadataListe =
                virksomhetMetadataRepository.hentVirksomhetMetadata(årstallOgKvartal);
        SykefraværsstatistikkLand sykefraværsstatistikkLand =
                sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentLand(årstallOgKvartal);
        List<SykefraværsstatistikkSektor> sykefraværsstatistikkSektor =
                sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleSektorer(årstallOgKvartal);
        List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring =
                sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleNæringer(årstallOgKvartal);
        // TODO: hent alle ststistikk til 5siffer næringskode
        List<SykefraværsstatistikkNæring5Siffer> sykefraværsstatistikkNæring5Siffer =
                sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleNæringer5Siffer(årstallOgKvartal);
        List<SykefraværsstatistikkVirksomhetUtenVarighet> sykefraværsstatistikkVirksomhetUtenVarighet =
                sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleVirksomheter(årstallOgKvartal);

        SykefraværMedKategori landSykefravær = getSykefraværMedKategoriForLand(
                årstallOgKvartal,
                sykefraværsstatistikkLand
        );

        AtomicInteger antallOppdatertSomEksportert = new AtomicInteger();
        kafkaService.nullstillUtsendingRapport();

        virksomheterTilEksport.stream().forEach(virksomhetTilEksport -> {
                    System.out.println("Eksport for " + virksomhetTilEksport.getOrgnr());
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
                                null,
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

                        eksporteringRepository.oppdaterTilEksportert(virksomhetTilEksport);
                        antallOppdatertSomEksportert.getAndIncrement();
                    }
                }
        );


        log.info("Eksport er ferdig med: antall prosessert='{}', antall bekreftet eksportert='{}' og antall error='{}'",
                kafkaService.getAntallMeldingerMottattForUtsending(),
                kafkaService.getAntallMeldingerSent(),
                kafkaService.getAntallMeldingerIError()
        );
        return antallOppdatertSomEksportert.get();
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
        // 2 list list av næring5siffer(alle)
        // list av næring5siffer i virksomhet
        // filterering og oppretter en ny liste av SykefraværMedKategori

        //populate(emplList, deptList);

        List<SykefraværsstatistikkNæring5Siffer> filteredList = sykefraværsstatistikkNæring5SifferList.stream()
                .filter(empl -> virksomhetMetadata.getNæringOgNæringskode5siffer().stream()
                        .anyMatch(dept ->
                                //dept.getDepartment().equals("sales") &&
                                        empl.getNæringkode5siffer().equals(dept.getNæringskode5Siffer())))
                .collect(Collectors.toList());

        /*assertEquals(1, filteredList.size());
        assertEquals(expectedId, filteredList.get(0)
                .getEmployeeId());*/
        //TODO opprett en List av sykefraværMedKAtegori fra filteredliste and return den.
        return  null;
        // TODO delete me når kode funker(gammel kode)
       /* SykefraværsstatistikkNæring sfNæring5Siffer =
                sykefraværsstatistikkNæring5SifferList.stream().filter(
                        v -> v.getNæringkode5siffer().equals(virksomhetMetadata.getNæringOgNæringskode5siffer().stream().anyMatch(v.))
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
        );*/
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
