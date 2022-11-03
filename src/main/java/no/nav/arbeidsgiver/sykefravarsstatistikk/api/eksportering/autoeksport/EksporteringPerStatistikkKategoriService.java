package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.EksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.SykefraværsstatistikkTilEksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetEksportPerKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.utils.EitherUtils;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhetUtenVarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaUtsendingException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværOverFlereKvartaler;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.Aggregeringskalkulator;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.Sykefraværsdata;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository;
import org.springframework.kafka.KafkaException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.getSykefraværMedKategoriForLand;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.hentSisteKvartalIBeregningen;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.mapToSykefraværsstatistikkLand;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.toMap;

@Slf4j
@Component
public class EksporteringPerStatistikkKategoriService {

    private final EksporteringRepository eksporteringRepository;
    private final SykefraværRepository sykefraværRepository;
    private final SykefraværsstatistikkTilEksporteringRepository sykefraværsstatistikkTilEksporteringRepository;
    private final KafkaService kafkaService;

    public EksporteringPerStatistikkKategoriService(
          SykefraværRepository sykefraværRepository,
          SykefraværsstatistikkTilEksporteringRepository sykefraværsstatistikkTilEksporteringRepository,
          EksporteringRepository eksporteringRepository,
          KafkaService kafkaService
    ) {
        this.eksporteringRepository = eksporteringRepository;
        this.sykefraværRepository = sykefraværRepository;
        this.sykefraværsstatistikkTilEksporteringRepository = sykefraværsstatistikkTilEksporteringRepository;
        this.kafkaService = kafkaService;
    }

    public int eksporterSykefraværsstatistikkLand(ÅrstallOgKvartal årstallOgKvartal) {

        List<UmaskertSykefraværForEttKvartal> umaskertSykefraværsstatistikkSiste4KvartalerLand =
                sykefraværRepository.hentUmaskertSykefraværForNorge(
                        årstallOgKvartal.minusKvartaler(3)
                );

        Aggregeringskalkulator aggregeringskalkulatorLand = new Aggregeringskalkulator(
                new Sykefraværsdata(
                        Map.of(Statistikkategori.LAND, umaskertSykefraværsstatistikkSiste4KvartalerLand)
                ),
                årstallOgKvartal
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

        List<SykefraværOverFlereKvartaler> sykefraværForFlereKvartaler =
                EitherUtils.filterRights(aggregeringskalkulatorLand.getSykefraværForFlereKvartaler(Statistikkategori.LAND));

        SykefraværOverFlereKvartaler sykefraværOverFlereKvartalerLand = sykefraværForFlereKvartaler.get(0);
        kafkaService.nullstillUtsendingRapport(1, Statistikkategori.LAND.name());
        long startUtsendingProcess = System.nanoTime();

        int antallEksportert = 0;
        try {
            antallEksportert = kafkaService.sendTilStatistikkKategoriTopic(
                    årstallOgKvartal,
                    landSykefravær,
                    sykefraværOverFlereKvartalerLand
            );
        } catch (KafkaUtsendingException | KafkaException e) {
            log.warn("Fikk Exception fra Kafka med melding:'{}'. Avbryter prosess.", e.getMessage(), e);
        }

        long stopUtsendingProcess = System.nanoTime();
        kafkaService.addUtsendingTilKafkaProcessingTime(startUtsendingProcess, stopUtsendingProcess);

        return antallEksportert;
    }

    public int eksporterSykefraværsstatistikkVirksomhet(ÅrstallOgKvartal årstallOgKvartal) {
        // Hente data
        List<SykefraværsstatistikkVirksomhetUtenVarighet> alleKvartal =
                sykefraværsstatistikkTilEksporteringRepository.hentSykefraværAlleVirksomheter(
                        årstallOgKvartal.minusKvartaler(3),
                        årstallOgKvartal
                );

        Map<String, SykefraværOverFlereKvartaler> sykefraværOverFlereKvartalerMap =
                toMap1(alleKvartal);

        Map<String, SykefraværMedKategori> sykefraværMedKategoriSisteKvartalMap =
                toMap2(alleKvartal);


        // #1 Vi tar utgangspunktet fra en liste av virksomheter vi skal eksportere (som vi gjør i EksporteringService)
        List<VirksomhetEksportPerKvartal> virksomheterSomSkalEksporteres =
                eksporteringRepository.hentVirksomhetEksportPerKvartal(årstallOgKvartal);


        // #2 send til kafka
        virksomheterSomSkalEksporteres.stream().forEach(
                virksomhet -> kafkaService.sendTilStatistikkKategoriTopic(
                        årstallOgKvartal,
                        sykefraværMedKategoriSisteKvartalMap.get(virksomhet.getOrgnr()),
                        sykefraværOverFlereKvartalerMap.get(virksomhet.getOrgnr())
                        )
        );
        log.info("Ferdig :P ");

        // Kalle Kafka og returnere antall meldinger sent

        return 0;
    }

    private Map<String, SykefraværMedKategori> toMap2(List<SykefraværsstatistikkVirksomhetUtenVarighet> alleKvartal) {
        return null;
    }

    private Map<String, SykefraværOverFlereKvartaler> toMap1(List<SykefraværsstatistikkVirksomhetUtenVarighet> alleKvartal) {
        return null;
    }
}
