package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.EksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetEksportPerKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadataRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.StatistikkRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EksporteringService {

    private final EksporteringRepository eksporteringRepository;
    private final StatistikkRepository statistikkRepository;
    private final VirksomhetMetadataRepository virksomhetMetadataRepository;
    private final KafkaService kafkaService;
    private final boolean erEksporteringAktivert;

    public EksporteringService(
            EksporteringRepository eksporteringRepository,
            StatistikkRepository statistikkRepository,
            VirksomhetMetadataRepository virksomhetMetadataRepository,
            KafkaService kafkaService,
            @Value("${statistikk.eksportering.aktivert}") Boolean erEksporteringAktivert
    ) {
        this.eksporteringRepository = eksporteringRepository;
        this.statistikkRepository = statistikkRepository;
        this.virksomhetMetadataRepository = virksomhetMetadataRepository;
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
        int antallStatistikkSomSkalEksporteres =
                virksomhetEksportPerKvartal == null || virksomhetEksportPerKvartal.isEmpty() ?
                        0 :
                        (int) getAntallSomKanEksporteres(virksomhetEksportPerKvartal);

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

        List<VirksomhetEksportPerKvartal> virksomheterTilEksport =
                virksomhetEksportPerKvartal.stream()
                        .filter(VirksomhetEksportPerKvartal::eksportert)
                        .collect(Collectors.toList());

        int antallEksportert = eksporter(virksomheterTilEksport, årstallOgKvartal);

        return antallEksportert;
    }

    protected int eksporter(
            List<VirksomhetEksportPerKvartal> virksomheterTilEksport,
            ÅrstallOgKvartal årstallOgKvartal
    ) {

        // Hent SF Land
        // Hent SF alle sektorer
        // Hent SF alle næringskoder 2 siffer
        // Hent SF alle virksomheter

        AtomicInteger antallEksportert = new AtomicInteger();
        virksomheterTilEksport.stream().forEach( v -> {
                        System.out.println("Eksport for " + v.getOrgnr());

            try {
                kafkaService.send(
                        årstallOgKvartal,
                        null,
                        null,
                        null,
                        null,
                        null
                );
            } catch (JsonProcessingException e) { // TODO: bruk en typed Exception og en counter som får prosessen til å stoppe etter X antall feil
                log.warn("Exception");
            }

            // update eksportert=true
            antallEksportert.getAndIncrement();
                });

        return antallEksportert.get();
    }

    protected long getAntallSomKanEksporteres(List<VirksomhetEksportPerKvartal> virksomhetEksportPerKvartal) {
        return virksomhetEksportPerKvartal.stream().filter(VirksomhetEksportPerKvartal::eksportert).count();
    }
}
