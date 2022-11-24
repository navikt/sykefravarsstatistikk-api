package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport;

import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.SlettOgOpprettResultat;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.Statistikkilde;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.StatistikkildeDvh;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkLand;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæringMedVarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkSektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhetMedGradering;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.Importeringsobjekt;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.StatistikkRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.PubliseringsdatoerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SykefraværsstatistikkImporteringService {

  private final StatistikkRepository statistikkRepository;
  private final DatavarehusRepository datavarehusRepository;
  private final PubliseringsdatoerRepository publiseringsdatoerRepository;
  private final boolean erImporteringAktivert;
  private final Environment environment;


  public SykefraværsstatistikkImporteringService(
      StatistikkRepository statistikkRepository,
      DatavarehusRepository datavarehusRepository,
      PubliseringsdatoerRepository publiseringsdatoerRepository,
      @Value("${statistikk.importering.aktivert}") Boolean erImporteringAktivert,
      Environment environment

  ) {
    this.statistikkRepository = statistikkRepository;
    this.datavarehusRepository = datavarehusRepository;
    this.publiseringsdatoerRepository = publiseringsdatoerRepository;
    this.erImporteringAktivert = erImporteringAktivert;
    this.environment = environment;
  }


  public Importeringstatus importerHvisDetFinnesNyStatistikk() {
    log.info("Er importering aktivert? {}", erImporteringAktivert);

    List<ÅrstallOgKvartal> årstallOgKvartalForSykefraværsstatistikk = Arrays.asList(
        statistikkRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(
            Statistikkilde.LAND),
        statistikkRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(
            Statistikkilde.SEKTOR),
        statistikkRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(
            Statistikkilde.NÆRING),
        statistikkRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(
            Statistikkilde.NÆRING_5_SIFFER),
        statistikkRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(
            Statistikkilde.VIRKSOMHET)
    );

    if (Arrays.asList(environment.getActiveProfiles()).contains("dev")
        || Arrays.asList(environment.getActiveProfiles()).contains("mvc-test")) {
      log.info(
          "Miljø er dev eller test, genererer testdata for virksomheter. Dette skal ikke kjøre i prod!");
    } else {
      log.info("Miljø er ikke dev. Dette skal kjøre i prod!");
    }

    List<ÅrstallOgKvartal> årstallOgKvartalForDvh = Arrays.asList(
        datavarehusRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(
            StatistikkildeDvh.LAND_OG_SEKTOR),
        datavarehusRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(
            StatistikkildeDvh.NÆRING),
        datavarehusRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(
            StatistikkildeDvh.NÆRING_5_SIFFER),
        datavarehusRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(
            StatistikkildeDvh.VIRKSOMHET)
    );

    if (kanImportStartes(årstallOgKvartalForSykefraværsstatistikk, årstallOgKvartalForDvh)) {
      if (erImporteringAktivert) {
        final ÅrstallOgKvartal gjeldendeÅrstallOgKvartal = årstallOgKvartalForDvh.get(0);
        log.info("Importerer ny statistikk");
        importerNyStatistikk(gjeldendeÅrstallOgKvartal);
        oppdaterPubliseringsstatus(gjeldendeÅrstallOgKvartal);
      return Importeringstatus.IMPORTERT;} else {
        log.info("Statistikk er klar til importering men automatisk importering er ikke "
            + "aktivert");
      return Importeringstatus.IKKE_AKTIVERT;}
    } else {
      log.info("Importerer ikke ny statistikk");return Importeringstatus.DATAFEIL;
    }
  }


  private void oppdaterPubliseringsstatus(ÅrstallOgKvartal gjeldendeÅrstallOgKvartal) {
    publiseringsdatoerRepository.oppdaterSisteImporttidspunkt(gjeldendeÅrstallOgKvartal);
  }


  public void reimporterSykefraværsstatistikk(ÅrstallOgKvartal fra, ÅrstallOgKvartal til) {
    ÅrstallOgKvartal.range(fra, til).forEach(this::importerNyStatistikk);
  }


  public void reimporterSykefraværsstatistikk(ÅrstallOgKvartal fra, ÅrstallOgKvartal til,
      List<Importeringsobjekt> importeringsobjekter
  ) {
    ÅrstallOgKvartal.range(fra, til)
        .forEach((årstallOgKvartal) -> importerNyStatistikk(årstallOgKvartal,
            importeringsobjekter));
  }


  protected boolean kanImportStartes(
      List<ÅrstallOgKvartal> årstallOgKvartalForSfsDb,
      List<ÅrstallOgKvartal> årstallOgKvartalForDvh
  ) {

    boolean allImportertStatistikkHarSammeÅrstallOgKvartal =
        alleErLike(årstallOgKvartalForSfsDb);
    boolean allStatistikkFraDvhHarSammeÅrstallOgKvartal = alleErLike(årstallOgKvartalForDvh);

    if (!allImportertStatistikkHarSammeÅrstallOgKvartal
        || !allStatistikkFraDvhHarSammeÅrstallOgKvartal) {
      log.warn(
          "Kunne ikke importere ny statistikk, statistikk hadde forskjellige årstall og "
              + "kvartal. "
              +
              "Kvartaler Sykefraværsstatistikk-DB: {}. " +
              "Kvartaler DVH: {}",
          årstallOgKvartalForSfsDb,
          årstallOgKvartalForDvh
      );
      return false;
    }

    ÅrstallOgKvartal sisteÅrstallOgKvartalForDvh = årstallOgKvartalForDvh.get(0);
    ÅrstallOgKvartal sisteÅrstallOgKvartalForSykefraværsstatistikk =
        årstallOgKvartalForSfsDb.get(0);

    boolean importertStatistikkLiggerEttKvartalBakDvh =
        sisteÅrstallOgKvartalForDvh.minusKvartaler(1)
            .equals(sisteÅrstallOgKvartalForSykefraværsstatistikk);

    if (importertStatistikkLiggerEttKvartalBakDvh) {
      log.info("Skal importere statistikk fra Dvh for årstall {} og kvartal {}",
          sisteÅrstallOgKvartalForDvh.getÅrstall(),
          sisteÅrstallOgKvartalForDvh.getKvartal()
      );
      return true;
    } else if (sisteÅrstallOgKvartalForDvh.equals(
        sisteÅrstallOgKvartalForSykefraværsstatistikk)) {
      log.info("Skal ikke importere statistikk fra Dvh for årstall {} og kvartal {}. Ingen "
              + "ny statistikk funnet.",
          sisteÅrstallOgKvartalForDvh.getÅrstall(),
          sisteÅrstallOgKvartalForDvh.getKvartal()
      );
      return false;
    } else {
      log.warn(
          "Kunne ikke importere ny statistikk fra Dvh fordi årstall {} og kvartal {} ikke"
              + " ligger nøyaktig "
              +
              "ett kvartal foran vår statistikk som har årstall {} og kvartal {}.",
          sisteÅrstallOgKvartalForDvh.getÅrstall(),
          sisteÅrstallOgKvartalForDvh.getKvartal(),
          sisteÅrstallOgKvartalForSykefraværsstatistikk.getÅrstall(),
          sisteÅrstallOgKvartalForSykefraværsstatistikk.getKvartal()
      );
      return false;
    }
  }


  private void importerNyStatistikk(ÅrstallOgKvartal årstallOgKvartal,
      List<Importeringsobjekt> importeringsobjekter
  ) {
    if (importeringsobjekter.contains(Importeringsobjekt.LAND)) {
      importSykefraværsstatistikkLand(årstallOgKvartal);
    }

    if (importeringsobjekter.contains(Importeringsobjekt.SEKTOR)) {
      importSykefraværsstatistikkSektor(årstallOgKvartal);
    }

    if (importeringsobjekter.contains(Importeringsobjekt.NÆRING)) {
      importSykefraværsstatistikkNæring(årstallOgKvartal);
    }

    if (importeringsobjekter.contains(Importeringsobjekt.NÆRING_5_SIFFER)) {
      importSykefraværsstatistikkNæring5siffer(årstallOgKvartal);
    }

    // importerer fiktiv testdata for virksomheter i dev og test
    if (importeringsobjekter.contains(Importeringsobjekt.VIRKSOMHET)) {
      if (Arrays.asList(environment.getActiveProfiles()).contains("dev")
          || Arrays.asList(environment.getActiveProfiles()).contains("mvc-test")) {
        //importerGenerertSykefraværsstatistikkVirksomhetTestmiljøer(årstallOgKvartal);
        importSykefraværsstatistikkVirksomhet(årstallOgKvartal);
      } else {
        importSykefraværsstatistikkVirksomhet(årstallOgKvartal);
      }
    }

    if (importeringsobjekter.contains(Importeringsobjekt.NÆRING_MED_VARIGHET)) {
      importSykefraværsstatistikkNæringMedVarighet(årstallOgKvartal);
    }

    // importerer fiktiv testdata for virksomheter i dev og test
    if (importeringsobjekter.contains(Importeringsobjekt.GRADERING)) {
      if (Arrays.asList(environment.getActiveProfiles()).contains("dev")
          || Arrays.asList(environment.getActiveProfiles()).contains("mvc-test")) {
        //importerGenerertSykefraværsstatistikkMedGraderingTestmiljøer(årstallOgKvartal);
        importSykefraværsstatistikkMedGradering(årstallOgKvartal);
      } else {
        importSykefraværsstatistikkMedGradering(årstallOgKvartal);
      }
    }
  }


  private void importerNyStatistikk(ÅrstallOgKvartal årstallOgKvartal) {
    importSykefraværsstatistikkLand(årstallOgKvartal);
    importSykefraværsstatistikkSektor(årstallOgKvartal);
    importSykefraværsstatistikkNæring(årstallOgKvartal);
    importSykefraværsstatistikkNæring5siffer(årstallOgKvartal);
    importSykefraværsstatistikkVirksomhet(årstallOgKvartal);
    importSykefraværsstatistikkNæringMedVarighet(årstallOgKvartal);
    importSykefraværsstatistikkMedGradering(årstallOgKvartal);
  }


  private SlettOgOpprettResultat importSykefraværsstatistikkLand(
      ÅrstallOgKvartal årstallOgKvartal
  ) {
    List<SykefraværsstatistikkLand> sykefraværsstatistikkLand =
        datavarehusRepository.hentSykefraværsstatistikkLand(årstallOgKvartal);

    SlettOgOpprettResultat resultat = statistikkRepository.importSykefraværsstatistikkLand(
        sykefraværsstatistikkLand,
        årstallOgKvartal
    );
    loggResultat(årstallOgKvartal, resultat, "land");

    return resultat;
  }


  private SlettOgOpprettResultat importSykefraværsstatistikkSektor(
      ÅrstallOgKvartal årstallOgKvartal
  ) {
    List<SykefraværsstatistikkSektor> sykefraværsstatistikkSektor =
        datavarehusRepository.hentSykefraværsstatistikkSektor(årstallOgKvartal);

    SlettOgOpprettResultat resultat = statistikkRepository.importSykefraværsstatistikkSektor(
        sykefraværsstatistikkSektor,
        årstallOgKvartal
    );
    loggResultat(årstallOgKvartal, resultat, "sektor");

    return resultat;
  }


  private SlettOgOpprettResultat importSykefraværsstatistikkNæring(
      ÅrstallOgKvartal årstallOgKvartal
  ) {
    List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring =
        datavarehusRepository.hentSykefraværsstatistikkNæring(årstallOgKvartal);

    SlettOgOpprettResultat resultat = statistikkRepository.importSykefraværsstatistikkNæring(
        sykefraværsstatistikkNæring,
        årstallOgKvartal
    );
    loggResultat(årstallOgKvartal, resultat, "næring");

    return resultat;
  }


  private SlettOgOpprettResultat importSykefraværsstatistikkNæring5siffer(
      ÅrstallOgKvartal årstallOgKvartal
  ) {
    List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring =
        datavarehusRepository.hentSykefraværsstatistikkNæring5siffer(årstallOgKvartal);

    SlettOgOpprettResultat resultat =
        statistikkRepository.importSykefraværsstatistikkNæring5siffer(
            sykefraværsstatistikkNæring,
            årstallOgKvartal
        );
    loggResultat(årstallOgKvartal, resultat, "næring5siffer");

    return resultat;
  }


  private SlettOgOpprettResultat importSykefraværsstatistikkVirksomhet(
      ÅrstallOgKvartal årstallOgKvartal
  ) {
    List<SykefraværsstatistikkVirksomhet> sykefraværsstatistikkVirksomhet =
        datavarehusRepository.hentSykefraværsstatistikkVirksomhet(årstallOgKvartal);

    SlettOgOpprettResultat resultat =
        statistikkRepository.importSykefraværsstatistikkVirksomhet(
            sykefraværsstatistikkVirksomhet,
            årstallOgKvartal
        );
    loggResultat(årstallOgKvartal, resultat, "virksomhet");

    return resultat;
  }

  private SlettOgOpprettResultat importerGenerertSykefraværsstatistikkVirksomhetTestmiljøer(
      ÅrstallOgKvartal årstallOgKvartal
  ) {
    SykefraværsstatistikkImporteringUtils sykefraværsstatistikkImporteringUtils = new SykefraværsstatistikkImporteringUtils();
    List<SykefraværsstatistikkVirksomhet> sykefraværsstatistikkVirksomhet =
        sykefraværsstatistikkImporteringUtils.getSykefraværsstatistikkVirksomhetsTestdata();

    SlettOgOpprettResultat resultat =
        statistikkRepository.importSykefraværsstatistikkVirksomhet(
            sykefraværsstatistikkVirksomhet,
            årstallOgKvartal
        );
    loggResultat(årstallOgKvartal, resultat, "virksomhet (testdata)");

    return resultat;
  }


  private SlettOgOpprettResultat importSykefraværsstatistikkMedGradering(
      ÅrstallOgKvartal årstallOgKvartal
  ) {
    List<SykefraværsstatistikkVirksomhetMedGradering>
        sykefraværsstatistikkVirksomhetMedGradering =
        datavarehusRepository.hentSykefraværsstatistikkVirksomhetMedGradering(årstallOgKvartal);

    SlettOgOpprettResultat resultat =
        statistikkRepository.importSykefraværsstatistikkVirksomhetMedGradering(
            sykefraværsstatistikkVirksomhetMedGradering,
            årstallOgKvartal
        );
    loggResultat(årstallOgKvartal, resultat, "virksomhet gradert sykemelding");

    return resultat;
  }

  private SlettOgOpprettResultat importerGenerertSykefraværsstatistikkMedGraderingTestmiljøer(
      ÅrstallOgKvartal årstallOgKvartal
  ) {
    SykefraværsstatistikkImporteringUtils sykefraværsstatistikkImporteringUtils = new SykefraværsstatistikkImporteringUtils();
    List<SykefraværsstatistikkVirksomhetMedGradering>
        sykefraværsstatistikkVirksomhetMedGradering = sykefraværsstatistikkImporteringUtils.getSykefraværsstatistikkVirksomhetMedGraderingTestdata();

    SlettOgOpprettResultat resultat =
        statistikkRepository.importSykefraværsstatistikkVirksomhetMedGradering(
            sykefraværsstatistikkVirksomhetMedGradering,
            årstallOgKvartal
        );
    loggResultat(årstallOgKvartal, resultat, "virksomhet gradert sykemelding (testdata)");

    return resultat;
  }


  private SlettOgOpprettResultat importSykefraværsstatistikkNæringMedVarighet(
      ÅrstallOgKvartal årstallOgKvartal
  ) {
    List<SykefraværsstatistikkNæringMedVarighet> sykefraværsstatistikkNæringMedVarighet =
        datavarehusRepository.hentSykefraværsstatistikkNæringMedVarighet(årstallOgKvartal);

    SlettOgOpprettResultat resultat =
        statistikkRepository.importSykefraværsstatistikkNæringMedVarighet(
            sykefraværsstatistikkNæringMedVarighet,
            årstallOgKvartal
        );
    loggResultat(årstallOgKvartal, resultat, "næring med varighet");

    return resultat;
  }


  private static void loggResultat(ÅrstallOgKvartal årstallOgKvartal,
      SlettOgOpprettResultat resultat, String type
  ) {
    String melding =
        resultat.getAntallRadOpprettet() == 0 && resultat.getAntallRadSlettet() == 0 ?
            "Ingenting å importere"
            :
                String.format(
                    "Antall opprettet: %d, antall slettet: %d",
                    resultat.getAntallRadOpprettet(),
                    resultat.getAntallRadSlettet()
                );

    log.info(
        String.format(
            "Import av sykefraværsstatistikk (%s) for årstall '%d' og kvartal '%d 'er ferdig. %s",
            type,
            årstallOgKvartal.getÅrstall(),
            årstallOgKvartal.getKvartal(),
            melding
        )
    );
  }


  private boolean alleErLike(List<ÅrstallOgKvartal> årstallOgKvartal) {
    ÅrstallOgKvartal førsteÅrstallOgKvartal = årstallOgKvartal.get(0);
    return årstallOgKvartal.stream().allMatch(p -> p.equals(førsteÅrstallOgKvartal));
  }
}
