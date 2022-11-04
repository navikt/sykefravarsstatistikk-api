package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.EksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.SykefraværsstatistikkTilEksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetEksportPerKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhetUtenVarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaUtsendingException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository;
import org.springframework.kafka.KafkaException;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.getSykefraværMedKategoriForLand;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.hentSisteKvartalIBeregningen;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.mapToSykefraværsstatistikkLand;

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

    SykefraværMedKategori landSykefravær = getSykefraværMedKategoriForLand(
        årstallOgKvartal,
        mapToSykefraværsstatistikkLand(
            hentSisteKvartalIBeregningen(
                umaskertSykefraværsstatistikkSiste4KvartalerLand,
                årstallOgKvartal
            )
        )
    );

    kafkaService.nullstillUtsendingRapport(1, Statistikkategori.LAND.name());
    long startUtsendingProcess = System.nanoTime();

    int antallEksportert = 0;
    try {
      SykefraværFlereKvartalerForEksport sykefraværOverFlereKvartaler = new SykefraværFlereKvartalerForEksport(
              umaskertSykefraværsstatistikkSiste4KvartalerLand
      );

      antallEksportert = kafkaService.sendTilStatistikkKategoriTopic(
          årstallOgKvartal,
          landSykefravær,
          sykefraværOverFlereKvartaler
      );
    } catch (KafkaUtsendingException | KafkaException e) {
      log.warn("Fikk Exception fra Kafka med melding:'{}'. Avbryter prosess.", e.getMessage(), e);
    }

    long stopUtsendingProcess = System.nanoTime();
    kafkaService.addUtsendingTilKafkaProcessingTime(startUtsendingProcess, stopUtsendingProcess);

    return antallEksportert;
  }

  // Felles metode exportIBatch()
  public int eksporterSykefraværsstatistikkVirksomhet(ÅrstallOgKvartal årstallOgKvartal) {
    // Hente data
    List<SykefraværsstatistikkVirksomhetUtenVarighet> alleKvartal =
        sykefraværsstatistikkTilEksporteringRepository.hentSykefraværAlleVirksomheter(
            årstallOgKvartal.minusKvartaler(3),
            årstallOgKvartal
        );

    Map<String, List<SykefraværsstatistikkVirksomhetUtenVarighet>> sykefraværGruppertEtterOrgNr = alleKvartal.stream()
        .collect(
            Collectors.groupingBy(SykefraværsstatistikkVirksomhetUtenVarighet::getOrgnr));

    Map<String, SykefraværMedKategori> sykefraværMedKategoriSisteKvartalMap =
        createSykefraværMedKategoriMap(sykefraværGruppertEtterOrgNr);

    Map<String, SykefraværFlereKvartalerForEksport> sykefraværOverFlereKvartalerMap =
        createSykefraværOverFlereKvartalerMap(sykefraværGruppertEtterOrgNr);

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

  private Map<String, SykefraværMedKategori> createSykefraværMedKategoriMap(
      Map<String, List<SykefraværsstatistikkVirksomhetUtenVarighet>> sykefraværGruppertEtterOrgNr) {
    Map<String, SykefraværMedKategori> sykefraværSisteKvartalPerOrg = new HashMap<String, SykefraværMedKategori>();

    sykefraværGruppertEtterOrgNr.forEach(
        (orgnr, sykefravær) -> {
          SykefraværsstatistikkVirksomhetUtenVarighet sykefraværSisteKvartal = sykefravær.stream()
              .max(
                  Comparator.comparing(
                      kvartal -> new ÅrstallOgKvartal(kvartal.getÅrstall(), kvartal.getKvartal())))
              .get();
          SykefraværMedKategori sykefraværMedKategori = new SykefraværMedKategori(
              Statistikkategori.VIRKSOMHET,
              sykefraværSisteKvartal.getOrgnr(),
              new ÅrstallOgKvartal(sykefraværSisteKvartal.getÅrstall(),
                  sykefraværSisteKvartal.getKvartal()),
              sykefraværSisteKvartal.getTapteDagsverk(),
              sykefraværSisteKvartal.getMuligeDagsverk(),
              sykefraværSisteKvartal.getAntallPersoner()
          );
          sykefraværSisteKvartalPerOrg.put(orgnr, sykefraværMedKategori);
        });

    return sykefraværSisteKvartalPerOrg;
  }

  private Map<String, SykefraværFlereKvartalerForEksport> createSykefraværOverFlereKvartalerMap(
      Map<String, List<SykefraværsstatistikkVirksomhetUtenVarighet>> sykefraværGruppertEtterOrgNr) {
    Map<String, SykefraværFlereKvartalerForEksport> sykefraværOverFlereKvartalerPerOrgNr = new HashMap<>();
    sykefraværGruppertEtterOrgNr.forEach((orgnr, sykefravær) -> {
      List<UmaskertSykefraværForEttKvartal> umaskertSykefravær = sykefravær.stream()
          .map(item -> new UmaskertSykefraværForEttKvartal(
              new ÅrstallOgKvartal(item.getÅrstall(), item.getKvartal()),
              item.getTapteDagsverk(),
              item.getMuligeDagsverk(),
              item.getAntallPersoner()
          )).collect(Collectors.toList());

      sykefraværOverFlereKvartalerPerOrgNr.put(orgnr,
          new SykefraværFlereKvartalerForEksport(umaskertSykefravær));
    });

    return sykefraværOverFlereKvartalerPerOrgNr;
  }
}
