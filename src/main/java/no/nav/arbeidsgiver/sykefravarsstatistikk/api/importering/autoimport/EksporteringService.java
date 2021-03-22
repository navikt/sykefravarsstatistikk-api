package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.Statistikkilde;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.StatistikkRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartalMedOrgNr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.AlleVirksomheterRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class EksporteringService {

    private final StatistikkRepository statistikkRepository;
    private final KafkaService kafkaService;
    private final AlleVirksomheterRepository alleVirksomheterRepository;
    private final boolean erEksporteringAktivert;

    public EksporteringService(
            StatistikkRepository statistikkRepository,
            KafkaService kafkaService, AlleVirksomheterRepository alleVirksomheterRepository, @Value("${statistikk.importering.aktivert}") Boolean erEksporteringAktivert) {
        this.statistikkRepository = statistikkRepository;
        this.kafkaService = kafkaService;
        this.alleVirksomheterRepository = alleVirksomheterRepository;
        this.erEksporteringAktivert = erEksporteringAktivert;
    }

    public void eksporterHvisDetFinnesNyStatistikk() {
        // Todo sette opp riktig eksportering flag
        log.info("Er ekportering aktivert? {}", erEksporteringAktivert);

        List<ÅrstallOgKvartal> årstallOgKvartalForSykefraværsstatistikk
                = statistikkRepository.hentAlleÅrstallOgKvartalForSykefraværsstatistikk(Statistikkilde.VIRKSOMHET_MED_GRADERING);
        log.info("Fant " + årstallOgKvartalForSykefraværsstatistikk.size() + " rekord av årstall og kvartaler");
        årstallOgKvartalForSykefraværsstatistikk.stream().forEach(årstallOgKvartal -> {
            if (erEksporteringAktivert) {
                log.info("Eksportering er aktivert for årstall:" +
                        årstallOgKvartal.getÅrstall() +
                        " og kvartal:" + årstallOgKvartal.getKvartal());
                List<SykefraværForEttKvartalMedOrgNr> sykefraværForEttKvartalMedOrgNrs =
                        alleVirksomheterRepository.
                                hentSykefraværprosentAlleVirksomheterForEttKvartal(
                                        årstallOgKvartal
                                );
                log.info("Eksporter ny statistikk:" +
                        sykefraværForEttKvartalMedOrgNrs.size() +
                        " til eksportering");
                sykefraværForEttKvartalMedOrgNrs.stream().forEach(
                        sykefraværForEttKvartalMedOrgNr ->
                        {
                            try {
                                kafkaService.send(sykefraværForEttKvartalMedOrgNr);
                                alleVirksomheterRepository.oppdaterOgSetErEksportertTilTrue(
                                        "sykefravar_statistikk_virksomhet_med_gradering",
                                        new Orgnr(sykefraværForEttKvartalMedOrgNr.getOrgnr()),
                                        new ÅrstallOgKvartal(
                                                sykefraværForEttKvartalMedOrgNr.getÅrstall(),
                                                sykefraværForEttKvartalMedOrgNr.getKvartal())
                                );
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }
                        });
                log.info("ending av statistikk eksportering");

            } else {
                log.info("Ikke ny statistikk for eksportering");
            }
        });
    }


}
