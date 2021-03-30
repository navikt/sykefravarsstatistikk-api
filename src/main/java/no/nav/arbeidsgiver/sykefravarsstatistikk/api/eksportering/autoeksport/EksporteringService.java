package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadataRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.StatistikkRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.VirksomhetSykefravær;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartalMedOrgNr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.AlleNaring5SifferRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.AlleNaringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.AlleVirksomheterRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class EksporteringService {

    private final StatistikkRepository statistikkRepository;
    private final KafkaService kafkaService;
    private final AlleVirksomheterRepository alleVirksomheterRepository;
    private final AlleNaring5SifferRepository alleNæring5SifferRepository;
    private final AlleNaringRepository alleNæringRepository;
    private final VirksomhetMetadataRepository virksomhetMetadataRepository;
    private final boolean erEksporteringAktivert;

    public EksporteringService(
            StatistikkRepository statistikkRepository,
            KafkaService kafkaService,
            AlleVirksomheterRepository alleVirksomheterRepository,
            AlleNaring5SifferRepository alleNæring5SifferRepository,
            AlleNaringRepository alleNæringRepository,
            VirksomhetMetadataRepository virksomhetMetadataRepository, @Value("${statistikk.eksportering.aktivert}") Boolean erEksporteringAktivert
    ) {
        this.statistikkRepository = statistikkRepository;
        this.kafkaService = kafkaService;
        this.alleVirksomheterRepository = alleVirksomheterRepository;
        this.alleNæring5SifferRepository = alleNæring5SifferRepository;
        this.alleNæringRepository = alleNæringRepository;
        this.virksomhetMetadataRepository = virksomhetMetadataRepository;
        this.erEksporteringAktivert = erEksporteringAktivert;
    }

    /*
      #1 Hente alle ÅrstallOgKvartal som skal eksporteres: 2019/4, 2020/1, 2020/2

      #2 Samle opp data og bygge et Objekt (Key/Value) som er klart til å sende til Kafka

      #3 Til hvert Objekt:
        #3.1 Send til Kafka
        #3.2 Oppdater database med Sendt=true

     */

    public void eksporterHvisDetFinnesNyStatistikk() {
        // Todo sette opp riktig eksportering flag
        log.info("Er eksportering aktivert? {}", erEksporteringAktivert);

        if (!erEksporteringAktivert) {
            log.info("Skal ikke eksportere fordi erEksporteringAktivert er {}", erEksporteringAktivert);
            return;
        }


        List<ÅrstallOgKvartal> årstallOgKvartalTilEksport
                = alleVirksomheterRepository.hentAlleÅrstallOgKvartalTilEksport();
        log.info("Fant " + årstallOgKvartalTilEksport.size() + " rekord av årstall og kvartaler");

        årstallOgKvartalTilEksport.stream().forEach(årstallOgKvartal -> {
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
            List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring5Siffers =
                    alleNæring5SifferRepository.hentSykefraværprosentAlleNæringer5SifferForEttKvartal(
                            årstallOgKvartal
                    );
            List<SykefraværsstatistikkNæring> sykefraværsstatistikkNærings =
                    alleNæringRepository.hentSykefraværprosentAlleNæringerForEttKvartal(
                            årstallOgKvartal
                    );

            sykefraværForEttKvartalMedOrgNrs.stream().forEach(
                    sykefraværForEttKvartalMedOrgNr ->
                    {
                        // TODO: bygg disse 5 objekter
                        VirksomhetSykefravær virksomhetSykefravær = null;
                        SykefraværMedKategori næring5SifferSykefravær = null;
                        SykefraværMedKategori næringSykefravær = null;
                        SykefraværMedKategori sektorSykefravær = null;
                        SykefraværMedKategori landSykefravær = null;

                        try {
                            /*
                             // Et objekt
                                    sykefraværForEttKvartalMedOrgNr,
                                    mapNæringTilSykefraværForETTKvartal(
                                            sykefraværsstatistikkNæring5Siffers.stream().filter(
                                                    næring -> næring.getNæringkode().equals(sykefraværForEttKvartalMedOrgNr.getNæringskode5Siffer())
                                            )
                                                    .findFirst()
                                                    .get()
                                    ),
                                    mapNæringTilSykefraværForETTKvartal(
                                            sykefraværsstatistikkNærings.stream().filter(//TODO hent næring på en bedre måte
                                                    næring -> næring.getNæringkode().equals(sykefraværForEttKvartalMedOrgNr.getNæringskode5Siffer().substring(0, 1))
                                            )
                                                    .findFirst()
                                                    .get()
                                    )

                             */
                            kafkaService.send(
                                    årstallOgKvartal,
                                    virksomhetSykefravær,
                                    næring5SifferSykefravær,
                                    næringSykefravær,
                                    sektorSykefravær,
                                    landSykefravær
                            );

                            alleVirksomheterRepository.oppdaterOgSetErEksportertTilTrue( // Objekt.key
                                    "sykefravar_statistikk_virksomhet_med_gradering",
                                    new Orgnr(sykefraværForEttKvartalMedOrgNr.getOrgnr()),
                                    new ÅrstallOgKvartal(
                                            sykefraværForEttKvartalMedOrgNr.getÅrstall(),
                                            sykefraværForEttKvartalMedOrgNr.getKvartal())
                            );
                        } catch (JsonProcessingException e) {
                            log.warn("En feil har skjedd ved eksportering til kafka topic: " + e.getMessage(),
                                    e);
                            throw new EksporteringException(e.getMessage());
                        }
                    });
            log.info("ending av statistikk eksportering");

        });
    }

    private SykefraværForEttKvartal mapNæringTilSykefraværForETTKvartal(
            SykefraværsstatistikkNæring næringSykefravær
    ) {
        return new SykefraværForEttKvartal(
                new ÅrstallOgKvartal(
                        næringSykefravær.getÅrstall(),
                        næringSykefravær.getKvartal()
                ),
                næringSykefravær.getTapteDagsverk(),
                næringSykefravær.getMuligeDagsverk(),
                næringSykefravær.getAntallPersoner()
        );
    }

}
