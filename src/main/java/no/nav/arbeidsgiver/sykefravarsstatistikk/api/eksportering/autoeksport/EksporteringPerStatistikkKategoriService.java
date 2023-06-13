package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import static java.lang.String.format;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.getSykefraværMedKategoriForLand;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.hentSisteKvartalIBeregningen;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceUtils.mapToSykefraværsstatistikkLand;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.SykefraværsstatistikkTilEksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.ArbeidsmiljøportalenBransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkSektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhetUtenVarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config.KafkaTopic;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto.StatistikkategoriKafkamelding;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EksporteringPerStatistikkKategoriService {
  private final SykefraværsstatistikkTilEksporteringRepository tilEksporteringRepository;
  private final SykefraværRepository sykefraværRepository;
  private final KafkaService kafkaService;
  private final boolean erEksporteringAktivert;

  public EksporteringPerStatistikkKategoriService(
      SykefraværRepository sykefraværRepository,
      SykefraværsstatistikkTilEksporteringRepository sykefraværsstatistikkTilEksporteringRepository,
      KafkaService kafkaService,
      @Value("${statistikk.eksportering.aktivert}") Boolean erEksporteringAktivert) {
    this.sykefraværRepository = sykefraværRepository;
    this.tilEksporteringRepository = sykefraværsstatistikkTilEksporteringRepository;
    this.kafkaService = kafkaService;
    this.erEksporteringAktivert = erEksporteringAktivert;
  }

  public void eksporterPerStatistikkKategori(
      ÅrstallOgKvartal årstallOgKvartal, Statistikkategori statistikkategori) {

    if (!erEksporteringAktivert) {
      log.info("Eksportering er ikke aktivert. Avbryter.");
      return;
    }

    log.info(
        "Starter eksportering av kategori '{}' for årstall '{}' og kvartal '{}' på topic '{}'.",
        statistikkategori.name(),
        årstallOgKvartal.getÅrstall(),
        årstallOgKvartal.getKvartal(),
        KafkaTopic.Companion.from(statistikkategori).getNavn());

    switch (statistikkategori) {
      case LAND -> eksporterSykefraværsstatistikkLand(årstallOgKvartal);
      case NÆRING -> eksporterSykefraværsstatistikkNæring(årstallOgKvartal);
      case SEKTOR -> eksporterSykefraværsstatistikkSektor(årstallOgKvartal);
      case BRANSJE -> eksporterSykefraværsstatistikkBransje(årstallOgKvartal);
      case VIRKSOMHET -> eksporterSykefraværsstatistikkVirksomhet(årstallOgKvartal);
      default -> log.warn("Ikke implementert eksport for kategori '{}'", statistikkategori.name());
    }
    log.info("Eksportering av kategori '{}' er ferdig.", statistikkategori.name());
  }

  private void eksporterSykefraværsstatistikkBransje(ÅrstallOgKvartal sisteKvartal) {
    List<SykefraværsstatistikkBransje> sykefraværsstatistikkSisteFireKvartalerBransje =
        tilEksporteringRepository.hentSykefraværAlleBransjerFraOgMed(
            sisteKvartal.minusKvartaler(3));

    Map<ArbeidsmiljøportalenBransje, List<SykefraværsstatistikkBransje>>
        sykefraværGruppertEtterBransje =
            sykefraværsstatistikkSisteFireKvartalerBransje.stream()
                .collect(Collectors.groupingBy(SykefraværsstatistikkBransje::getBransje));

    sykefraværGruppertEtterBransje.forEach(
        (bransje, sykefraværsstatistikkForEnBransje) -> {
          List<UmaskertSykefraværForEttKvartal> sykefraværForFireKvartaler =
              sykefraværsstatistikkForEnBransje.stream()
                  .map(UmaskertSykefraværForEttKvartal::new)
                  .collect(Collectors.toList());

          SykefraværMedKategori sykefraværMedKategoriSisteKvartal =
              new SykefraværMedKategori(
                  Statistikkategori.BRANSJE,
                  bransje.name(),
                  getSykefraværSisteKvartal(sykefraværForFireKvartaler));

          assertForespurteKvartalFinnesIStatistikken(
              sisteKvartal, sykefraværMedKategoriSisteKvartal);

          SykefraværFlereKvartalerForEksport sykefraværOverFlereKvartaler =
              new SykefraværFlereKvartalerForEksport(sykefraværForFireKvartaler);

          StatistikkategoriKafkamelding melding =
              new StatistikkategoriKafkamelding(
                  sykefraværMedKategoriSisteKvartal, sykefraværOverFlereKvartaler);
          kafkaService.sendMelding(melding, KafkaTopic.SYKEFRAVARSSTATISTIKK_BRANSJE_V1);
        });
  }

  protected void eksporterSykefraværsstatistikkSektor(ÅrstallOgKvartal årstallOgKvartal) {
    List<SykefraværsstatistikkSektor> sykefraværsstatistikkSiste4KvartalerSektor =
        tilEksporteringRepository.hentSykefraværAlleSektorerFraOgMed(
            årstallOgKvartal.minusKvartaler(3));

    Map<String, List<SykefraværsstatistikkSektor>> sykefraværGruppertEtterSektor =
        sykefraværsstatistikkSiste4KvartalerSektor.stream()
            .collect(Collectors.groupingBy(SykefraværsstatistikkSektor::getSektorkode));

    sykefraværGruppertEtterSektor.forEach(
        (sektor, sykefraværForEnSektor) -> {
          List<UmaskertSykefraværForEttKvartal> umaskertSykefraværsstatistikkSiste4KvartalerSektor =
              sykefraværForEnSektor.stream()
                  .map(UmaskertSykefraværForEttKvartal::new)
                  .collect(Collectors.toList());

          UmaskertSykefraværForEttKvartal umaskertSykefraværSisteKvartal =
              getSykefraværSisteKvartal(umaskertSykefraværsstatistikkSiste4KvartalerSektor);
          SykefraværMedKategori sykefraværMedKategoriSisteKvartal =
              new SykefraværMedKategori(
                  Statistikkategori.SEKTOR, sektor, umaskertSykefraværSisteKvartal);

          assertForespurteKvartalFinnesIStatistikken(
              årstallOgKvartal, sykefraværMedKategoriSisteKvartal);

          SykefraværFlereKvartalerForEksport sykefraværOverFlereKvartaler =
              new SykefraværFlereKvartalerForEksport(
                  umaskertSykefraværsstatistikkSiste4KvartalerSektor);

          StatistikkategoriKafkamelding melding =
              new StatistikkategoriKafkamelding(
                  sykefraværMedKategoriSisteKvartal, sykefraværOverFlereKvartaler);
          kafkaService.sendMelding(melding, KafkaTopic.SYKEFRAVARSSTATISTIKK_SEKTOR_V1);
        });
  }

  protected void eksporterSykefraværsstatistikkNæring(ÅrstallOgKvartal årstallOgKvartal) {

    List<SykefraværsstatistikkNæring> sykefraværsstatistikkSiste4KvartalerNæring =
        tilEksporteringRepository.hentSykefraværAlleNæringerFraOgMed(
            årstallOgKvartal.minusKvartaler(3));

    Map<String, List<SykefraværsstatistikkNæring>> sykefraværGruppertEtterNæring =
        sykefraværsstatistikkSiste4KvartalerNæring.stream()
            .collect(Collectors.groupingBy(SykefraværsstatistikkNæring::getNæringkode));

    sykefraværGruppertEtterNæring.forEach(
        (næring, sykefraværForEnNæring) -> {
          List<UmaskertSykefraværForEttKvartal> umaskertSykefraværsstatistikkSiste4KvartalerNæring =
              sykefraværForEnNæring.stream()
                  .map(
                      sykefraværForEttKvartal ->
                          new UmaskertSykefraværForEttKvartal(
                              new ÅrstallOgKvartal(
                                  sykefraværForEttKvartal.getÅrstall(),
                                  sykefraværForEttKvartal.getKvartal()),
                              sykefraværForEttKvartal.getTapteDagsverk(),
                              sykefraværForEttKvartal.getMuligeDagsverk(),
                              sykefraværForEttKvartal.getAntallPersoner()))
                  .collect(Collectors.toList());

          UmaskertSykefraværForEttKvartal umaskertSykefraværSisteKvartal =
              getSykefraværSisteKvartal(umaskertSykefraværsstatistikkSiste4KvartalerNæring);
          SykefraværMedKategori sykefraværMedKategoriSisteKvartal =
              new SykefraværMedKategori(
                  Statistikkategori.NÆRING,
                  næring,
                  umaskertSykefraværSisteKvartal.getÅrstallOgKvartal(),
                  umaskertSykefraværSisteKvartal.getDagsverkTeller(),
                  umaskertSykefraværSisteKvartal.getDagsverkNevner(),
                  umaskertSykefraværSisteKvartal.getAntallPersoner());

          assertForespurteKvartalFinnesIStatistikken(
              årstallOgKvartal, sykefraværMedKategoriSisteKvartal);
          SykefraværFlereKvartalerForEksport sykefraværOverFlereKvartaler =
              new SykefraværFlereKvartalerForEksport(
                  umaskertSykefraværsstatistikkSiste4KvartalerNæring);

          StatistikkategoriKafkamelding melding =
              new StatistikkategoriKafkamelding(
                  sykefraværMedKategoriSisteKvartal, sykefraværOverFlereKvartaler);
          kafkaService.sendMelding(melding, KafkaTopic.SYKEFRAVARSSTATISTIKK_NARING_V1);
        });
  }

  protected void eksporterSykefraværsstatistikkLand(ÅrstallOgKvartal årstallOgKvartal) {
    List<UmaskertSykefraværForEttKvartal> umaskertSykefraværsstatistikkSiste4KvartalerLand =
        sykefraværRepository.hentUmaskertSykefraværForNorge(årstallOgKvartal.minusKvartaler(3));

    SykefraværMedKategori landSykefravær =
        getSykefraværMedKategoriForLand(
            årstallOgKvartal,
            mapToSykefraværsstatistikkLand(
                hentSisteKvartalIBeregningen(
                    umaskertSykefraværsstatistikkSiste4KvartalerLand, årstallOgKvartal)));

    assertForespurteKvartalFinnesIStatistikken(årstallOgKvartal, landSykefravær);

    SykefraværFlereKvartalerForEksport sykefraværOverFlereKvartaler =
        new SykefraværFlereKvartalerForEksport(umaskertSykefraværsstatistikkSiste4KvartalerLand);

    StatistikkategoriKafkamelding melding =
        new StatistikkategoriKafkamelding(landSykefravær, sykefraværOverFlereKvartaler);
    kafkaService.sendMelding(melding, KafkaTopic.SYKEFRAVARSSTATISTIKK_LAND_V1);
  }

  private static void assertForespurteKvartalFinnesIStatistikken(
      ÅrstallOgKvartal årstallOgKvartal, SykefraværMedKategori sykefravær) {
    if (!årstallOgKvartal.equals(sykefravær.getÅrstallOgKvartal())) {
      throw new RuntimeException("Siste kvartal i dataene er ikke lik forespurt kvartal");
    }
  }

  protected void eksporterSykefraværsstatistikkVirksomhet(ÅrstallOgKvartal årstallOgKvartal) {
    List<SykefraværsstatistikkVirksomhetUtenVarighet> statistikkTilEksport =
        tilEksporteringRepository.hentSykefraværAlleVirksomheter(
            årstallOgKvartal.minusKvartaler(3), årstallOgKvartal);

    Map<String, List<SykefraværsstatistikkVirksomhetUtenVarighet>> sykefraværGruppertEtterOrgNr =
        statistikkTilEksport.stream()
            .collect(Collectors.groupingBy(SykefraværsstatistikkVirksomhetUtenVarighet::getOrgnr));

    Map<String, SykefraværMedKategori> sykefraværMedKategoriSisteKvartalMap =
        createSykefraværMedKategoriMap(sykefraværGruppertEtterOrgNr);

    Map<String, SykefraværFlereKvartalerForEksport> sykefraværOverFlereKvartalerMap =
        createSykefraværOverFlereKvartalerMap(sykefraværGruppertEtterOrgNr);

    log.info(
        format(
            "Starting utsending av statistikk. '%d' meldinger vil bli sendt",
            sykefraværGruppertEtterOrgNr.size()));

    for (String orgnr : sykefraværGruppertEtterOrgNr.keySet()) {
      SykefraværMedKategori sykefraværEttKvartal = sykefraværMedKategoriSisteKvartalMap.get(orgnr);
      SykefraværFlereKvartalerForEksport sykefraværOverFlereKvartaler =
          sykefraværOverFlereKvartalerMap.get(orgnr);

      assertForespurteKvartalFinnesIStatistikken(årstallOgKvartal, sykefraværEttKvartal);

      StatistikkategoriKafkamelding melding =
          new StatistikkategoriKafkamelding(sykefraværEttKvartal, sykefraværOverFlereKvartaler);

      kafkaService.sendMelding(melding, KafkaTopic.SYKEFRAVARSSTATISTIKK_VIRKSOMHET_V1);
    }

    log.info("Ferdig med utsending av alle meldinger til Kafka for statistikkategori VIRKSOMHET.");
  }

  private Map<String, SykefraværMedKategori> createSykefraværMedKategoriMap(
      Map<String, List<SykefraværsstatistikkVirksomhetUtenVarighet>> sykefraværGruppertEtterOrgNr) {
    Map<String, SykefraværMedKategori> sykefraværSisteKvartalPerOrg = new HashMap<>();

    sykefraværGruppertEtterOrgNr.forEach(
        (orgnr, sykefravær) -> {
          SykefraværsstatistikkVirksomhetUtenVarighet sykefraværSisteKvartal =
              sykefravær.stream()
                  .max(
                      Comparator.comparing(
                          kvartal ->
                              new ÅrstallOgKvartal(kvartal.getÅrstall(), kvartal.getKvartal())))
                  .get();
          SykefraværMedKategori sykefraværMedKategori =
              new SykefraværMedKategori(
                  Statistikkategori.VIRKSOMHET,
                  sykefraværSisteKvartal.getOrgnr(),
                  new ÅrstallOgKvartal(
                      sykefraværSisteKvartal.getÅrstall(), sykefraværSisteKvartal.getKvartal()),
                  sykefraværSisteKvartal.getTapteDagsverk(),
                  sykefraværSisteKvartal.getMuligeDagsverk(),
                  sykefraværSisteKvartal.getAntallPersoner());
          sykefraværSisteKvartalPerOrg.put(orgnr, sykefraværMedKategori);
        });

    return sykefraværSisteKvartalPerOrg;
  }

  private UmaskertSykefraværForEttKvartal getSykefraværSisteKvartal(
      List<UmaskertSykefraværForEttKvartal> sykefraværFlereKvartaler) {
    return sykefraværFlereKvartaler.stream()
        .max(Comparator.comparing(UmaskertSykefraværForEttKvartal::getÅrstallOgKvartal))
        .get();
  }

  private Map<String, SykefraværFlereKvartalerForEksport> createSykefraværOverFlereKvartalerMap(
      Map<String, List<SykefraværsstatistikkVirksomhetUtenVarighet>> sykefraværGruppertEtterOrgNr) {
    Map<String, SykefraværFlereKvartalerForEksport> sykefraværOverFlereKvartalerPerOrgNr =
        new HashMap<>();
    sykefraværGruppertEtterOrgNr.forEach(
        (orgnr, sykefravær) -> {
          List<UmaskertSykefraværForEttKvartal> umaskertSykefravær =
              sykefravær.stream()
                  .map(
                      item ->
                          new UmaskertSykefraværForEttKvartal(
                              new ÅrstallOgKvartal(item.getÅrstall(), item.getKvartal()),
                              item.getTapteDagsverk(),
                              item.getMuligeDagsverk(),
                              item.getAntallPersoner()))
                  .collect(Collectors.toList());

          sykefraværOverFlereKvartalerPerOrgNr.put(
              orgnr, new SykefraværFlereKvartalerForEksport(umaskertSykefravær));
        });

    return sykefraværOverFlereKvartalerPerOrgNr;
  }
}
