package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.utils.EitherUtils;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaUtsendingException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.Aggregeringskalkulator;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.StatistikkDto;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.Sykefraværsdata;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository;
import org.springframework.kafka.KafkaException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.getSykefraværMedKategoriForLand;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.hentSisteKvartalIBeregningen;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.mapToSykefraværsstatistikkLand;

@Slf4j
@Component
public class EksporteringPerStatistikkKategoriService {

    private final SykefraværRepository sykefraværRepository;
    private final KafkaService kafkaService;

    public EksporteringPerStatistikkKategoriService(
          SykefraværRepository sykefraværRepository,
          KafkaService kafkaService
    ) {
        this.sykefraværRepository = sykefraværRepository;
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

        List<StatistikkDto> statistikkDtos = EitherUtils.filterRights(aggregeringskalkulatorLand.fraværsprosentNorge());

        kafkaService.nullstillUtsendingRapport(1);
        long startUtsendingProcess = System.nanoTime();

        int antallEksportert = 0;
        try {
            antallEksportert = kafkaService.sendTilSykefraværsstatistikkLandTopic(
                    årstallOgKvartal,
                    landSykefravær,
                    statistikkDtos.get(0)
            );
        } catch (KafkaUtsendingException | KafkaException e) {
            log.warn("Fikk Exception fra Kafka med melding:'{}'. Avbryter prosess.", e.getMessage(), e);
        }

        long stopUtsendingProcess = System.nanoTime();
        kafkaService.addUtsendingTilKafkaProcessingTime(startUtsendingProcess, stopUtsendingProcess);

        return antallEksportert;
    }
}
