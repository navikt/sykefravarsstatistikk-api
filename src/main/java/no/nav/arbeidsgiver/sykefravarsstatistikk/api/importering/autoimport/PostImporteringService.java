package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.EksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetEksportPerKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadata;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadataNæringskode5siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadataRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.virksomhetsklassifikasjoner.Orgenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaUtsendingHistorikkRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.GraderingRepository;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PostImporteringService {

  private final DatavarehusRepository datavarehusRepository;
  private final VirksomhetMetadataRepository virksomhetMetadataRepository;
  private final GraderingRepository graderingRepository;
  private final EksporteringRepository eksporteringRepository;
  private final KafkaUtsendingHistorikkRepository kafkaUtsendingHistorikkRepository;
  private final boolean erImporteringAktivert;
  private final boolean erEksporteringAktivert;

  public PostImporteringService(
      DatavarehusRepository datavarehusRepository,
      VirksomhetMetadataRepository virksomhetMetadataRepository,
      GraderingRepository graderingRepository,
      EksporteringRepository eksporteringRepository,
      KafkaUtsendingHistorikkRepository kafkaUtsendingHistorikkRepository,
      @Value("${statistikk.importering.aktivert}") Boolean erImporteringAktivert,
      @Value("${statistikk.eksportering.aktivert}") Boolean erEksporteringAktivert) {
    this.datavarehusRepository = datavarehusRepository;
    this.virksomhetMetadataRepository = virksomhetMetadataRepository;
    this.graderingRepository = graderingRepository;
    this.eksporteringRepository = eksporteringRepository;
    this.erImporteringAktivert = erImporteringAktivert;
    this.erEksporteringAktivert = erEksporteringAktivert;
    this.kafkaUtsendingHistorikkRepository = kafkaUtsendingHistorikkRepository;
  }

  // Kall fra Scheduler / Importering
  // TODO: ikke tatt i bruk enda
  public int fullførPostImporteringOgForberedNesteEksport(ÅrstallOgKvartal årstallOgKvartal) {
    Pair<Integer, Integer> antallVirksomheterImportert =
        importVirksomhetMetadataOgVirksomhetNæringskode5sifferMapping(årstallOgKvartal);
    boolean harNoeÅForbereddeTilNesteEksport = antallVirksomheterImportert.getFirst() > 0;

    if (!harNoeÅForbereddeTilNesteEksport) {
      log.info("Post-importering er ferdig. Ingenting å forberedde til neste eksport");
      return 0;
    } else {
      log.info(
          "Post-importering for årstall '{}' og kvartal '{}' er ferdig med "
              + "'{}' VirksomhetMetadata opprettet og "
              + "'{}' VirksomhetMetadataNæringskode5siffer opprettet",
          årstallOgKvartal.getÅrstall(),
          årstallOgKvartal.getKvartal(),
          antallVirksomheterImportert.getFirst(),
          antallVirksomheterImportert.getSecond());
    }

    int antallRaderTilNesteEksportering = forberedNesteEksport(årstallOgKvartal, true);

    log.info(
        "Forberedelse til neste eksport er ferdig, med '{}' rader klare til neste eksportering "
            + "(årstall '{}', kvartal '{}')",
        antallRaderTilNesteEksportering,
        årstallOgKvartal.getÅrstall(),
        årstallOgKvartal.getKvartal());
    return antallRaderTilNesteEksportering;
  }

  // Kall fra Controller / backdoor
  protected Pair<Integer, Integer> importVirksomhetMetadataOgVirksomhetNæringskode5sifferMapping(
      ÅrstallOgKvartal årstallOgKvartal) {
    if (!erImporteringAktivert) {
      log.info(
          "Importering er ikke aktivert. Skal ikke importere VirksomhetMetadata "
              + "og VirksomhetNæringskode5sifferMapping");
      return Pair.of(0, 0);
    }

    int antallVirksomhetMetadataOpprettet = importVirksomhetMetadata(årstallOgKvartal);
    int antallVirksomhetMetadataNæringskode5siffer =
        importVirksomhetNæringskode5sifferMapping(årstallOgKvartal);

    log.info(
        "Importering av VirksomhetMetadata og VirksomhetNæringskode5sifferMapping er ferdig. "
            + "'{}' VirksomhetMetadata og '{}' VirksomhetNæringskode5sifferMapping har blitt importert. ",
        antallVirksomhetMetadataOpprettet,
        antallVirksomhetMetadataNæringskode5siffer);
    return Pair.of(antallVirksomhetMetadataOpprettet, antallVirksomhetMetadataNæringskode5siffer);
  }

  // Kall fra Controller / backdoor
  protected int forberedNesteEksport(ÅrstallOgKvartal årstallOgKvartal, boolean slettHistorikk) {
    if (!erEksporteringAktivert) {
      log.info(
          "Eksportering er ikke aktivert. "
              + "Skal ikke forberedde til neste eksportering for årstall '{}' og kvartal '{}'. ",
          årstallOgKvartal.getÅrstall(),
          årstallOgKvartal.getKvartal());
      return 0;
    }

    log.info("Forberede neste eksport: prosessen starter.");
    if (slettHistorikk) {
      long slettUtsendingHistorikkStart = System.currentTimeMillis();
      int antallRaderSlettetIUtsendingHistorikk =
          kafkaUtsendingHistorikkRepository.slettHistorikk();
      log.info(
          "Forberede neste eksport: utsending historikk (working table) har blitt nullstilt. "
              + "{} rader har blitt slettet. Tok {} millis. ",
          antallRaderSlettetIUtsendingHistorikk,
          System.currentTimeMillis() - slettUtsendingHistorikkStart);
    } else {
      log.info("Forberede neste eksport: skal ikke slette historikk.");
    }

    int antallIkkeEksportertSykefaværsstatistikk =
        eksporteringRepository.hentAntallIkkeFerdigEksportert();

    if (antallIkkeEksportertSykefaværsstatistikk > 0) {
      log.warn(
          "Det finnes '{}' rader som IKKE er ferdig eksportert (eksportert=false). "
              + "Skal ikke importere en ny liste av virksomheter i 'eksport_per_kvartal' da det ligger "
              + "fortsatt noen rader markert som ikke eksportert. "
              + "Du kan enten kjøre ferdig siste eksport eller oppdatere manuelt gjenstående rader "
              + "med 'eksportert=true' i tabell 'eksport_per_kvartal'. "
              + "Etter det kan du kjøre denne prosessen (forbered neste eksport) på nytt. ",
          antallIkkeEksportertSykefaværsstatistikk);
      return 0;
    }
    int antallSlettetEksportertPerKvartal = eksporteringRepository.slettEksportertPerKvartal();
    log.info("Slettet '{}' rader fra forrige eksportering.", antallSlettetEksportertPerKvartal);
    List<VirksomhetMetadata> virksomhetMetadata =
        virksomhetMetadataRepository.hentVirksomhetMetadata(årstallOgKvartal);

    List<VirksomhetEksportPerKvartal> virksomhetEksportPerKvartalListe =
        mapToVirksomhetEksportPerKvartal(virksomhetMetadata);
    log.info(
        "Skal gjøre klar '{}' virksomheter til neste eksportering. ",
        virksomhetEksportPerKvartalListe == null ? 0 : virksomhetEksportPerKvartalListe.size());

    int antallOpprettet = eksporteringRepository.opprettEksport(virksomhetEksportPerKvartalListe);
    log.info("Antall rader opprettet til neste eksportering: {}", antallOpprettet);

    return antallOpprettet;
  }

  private int importVirksomhetMetadata(ÅrstallOgKvartal årstallOgKvartal) {
    List<Orgenhet> orgenhetList = hentOrgenhetListeFraDvh(årstallOgKvartal);

    if (orgenhetList.isEmpty()) {
      log.warn("Stopper import av metadata.");
      return 0;
    }

    log.info("Antall orgenhet fra DVH: {}", orgenhetList.size());
    int antallSlettet = virksomhetMetadataRepository.slettVirksomhetMetadata();
    log.info(
        "Slettet '{}' VirksomhetMetadata for årstall '{}' og kvartal '{}'",
        antallSlettet,
        årstallOgKvartal.getÅrstall(),
        årstallOgKvartal.getKvartal());
    int antallOpprettet =
        virksomhetMetadataRepository.opprettVirksomhetMetadata(
            mapToVirksomhetMetadata(orgenhetList));
    log.info("Antall rader VirksomhetMetadata opprettet: {}", antallOpprettet);

    return antallOpprettet;
  }

  @Nullable
  private List<Orgenhet> hentOrgenhetListeFraDvh(ÅrstallOgKvartal årstallOgKvartal) {
    List<Orgenhet> orgenhetList = datavarehusRepository.hentOrgenhet(årstallOgKvartal, true);

    if (orgenhetList.isEmpty()) {
      List<ÅrstallOgKvartal> alleSisteTilgjengeligKvartal =
          datavarehusRepository.hentSisteKvartalForOrgenhet();

      if (alleSisteTilgjengeligKvartal == null || alleSisteTilgjengeligKvartal.isEmpty()) {
        log.warn("Ingen Orgenhet i DVH funnet til import.");
        return Collections.emptyList();
      }

      if (alleSisteTilgjengeligKvartal.size() != 1) {
        log.warn(
            "Har ikke funnet Orgenhet for årstall '{}' og kvartal '{}'. "
                + "Flere enn 1 årstal og kvartal funnet i DVH for Orgenhet, antall: '{}'.",
            årstallOgKvartal.getÅrstall(),
            årstallOgKvartal.getKvartal(),
            alleSisteTilgjengeligKvartal.size());
        return Collections.emptyList();
      }

      ÅrstallOgKvartal tilgjengeligÅrstallOgKvartal = alleSisteTilgjengeligKvartal.get(0);
      log.warn(
          "Har ikke funnet Orgenhet for årstall '{}' og kvartal '{}'. Importerer VirksomhetMetadata "
              + "med det årstall og kvartal som er tilgjengelig i datavarehus: '{} {}'",
          årstallOgKvartal.getÅrstall(),
          årstallOgKvartal.getKvartal(),
          tilgjengeligÅrstallOgKvartal.getÅrstall(),
          tilgjengeligÅrstallOgKvartal.getKvartal());
      orgenhetList = datavarehusRepository.hentOrgenhet(årstallOgKvartal);
    }
    return orgenhetList;
  }

  private int importVirksomhetNæringskode5sifferMapping(ÅrstallOgKvartal årstallOgKvartal) {
    List<VirksomhetMetadataNæringskode5siffer> virksomhetMetadataNæringskode5siffer =
        graderingRepository.hentVirksomhetMetadataNæringskode5siffer(årstallOgKvartal);

    if (virksomhetMetadataNæringskode5siffer.isEmpty()) {
      log.warn(
          "Ingen virksomhetMetadataNæringskode5siffer funnet i vår statistikk tabell. Stopper import. ");
      return 0;
    }

    int antallSlettetNæringskode5Siffer =
        virksomhetMetadataRepository.slettNæringOgNæringskode5siffer();
    log.info(
        "Slettet '{}' eksisterende NæringOgNæringskode5siffer. ", antallSlettetNæringskode5Siffer);

    int antallOpprettet =
        virksomhetMetadataRepository.opprettVirksomhetMetadataNæringskode5siffer(
            virksomhetMetadataNæringskode5siffer);

    log.info("Antall rader VirksomhetMetadataNæringskode5siffer opprettet: {}", antallOpprettet);
    return antallOpprettet;
  }

  private static List<VirksomhetMetadata> mapToVirksomhetMetadata(List<Orgenhet> orgenhetList) {
    return orgenhetList.stream()
        .map(
            orgenhet ->
                new VirksomhetMetadata(
                    orgenhet.getOrgnr(),
                    orgenhet.getNavn(),
                    orgenhet.getRectype(),
                    orgenhet.getSektor(),
                    orgenhet.getNæring(),
                    orgenhet.getÅrstallOgKvartal()))
        .collect(Collectors.toList());
  }

  private static List<VirksomhetEksportPerKvartal> mapToVirksomhetEksportPerKvartal(
      List<VirksomhetMetadata> virksomhetMetadataList) {
    return virksomhetMetadataList.stream()
        .map(
            virksomhetMetadata ->
                new VirksomhetEksportPerKvartal(
                    new Orgnr(virksomhetMetadata.getOrgnr()),
                    new ÅrstallOgKvartal(
                        virksomhetMetadata.getÅrstall(), virksomhetMetadata.getKvartal()),
                    false))
        .collect(Collectors.toList());
  }
}
