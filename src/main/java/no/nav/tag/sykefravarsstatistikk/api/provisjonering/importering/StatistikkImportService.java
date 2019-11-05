package no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.sykefravarsstatistikk.api.domene.SlettOgOpprettResultat;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.SykefraværsstatistikkLand;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.DataverehusRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Profile({"local", "dev"})
@Slf4j
@Component
public class StatistikkImportService {

  private final DataverehusRepository datavarehusRepository;
  private final StatistikkImportRepository statistikkImportRepository;

  public StatistikkImportService(
      DataverehusRepository datavarehusRepository,
      StatistikkImportRepository statistikkImportRepository) {
    this.datavarehusRepository = datavarehusRepository;
    this.statistikkImportRepository = statistikkImportRepository;
  }

  // TODO: DELETE ME --> bare til versifisering
  public List<SykefraværsstatistikkLand> hentSykefraværsstatistikkLand(int årstall, int kvartal) {
    return datavarehusRepository.hentSykefraværsstatistikkLand(
        new ÅrstallOgKvartal(årstall, kvartal));
  }

  public void importSykefraværsstatistikkLand(int årstall, int kvartal) {
    List<SykefraværsstatistikkLand> sykefraværsstatistikkLand =
        datavarehusRepository.hentSykefraværsstatistikkLand(new ÅrstallOgKvartal(årstall, kvartal));

    if (sykefraværsstatistikkLand.isEmpty()) {
      log.info(
          String.format(
              "Import av sykefraværsstatistikk (land) for årstall '%d' og kvartal '%d 'er ferdig. "
                  + "Ingenting å importere.",
              årstall, kvartal));
      return;
    }

    SlettOgOpprettResultat resultat =
        statistikkImportRepository.importSykefraværsstatistikkLand(
            sykefraværsstatistikkLand, new ÅrstallOgKvartal(årstall, kvartal));
    log.info(
        String.format(
            "Import av sykefraværsstatistikk (land) for årstall '%d' og kvartal '%d 'er ferdig. "
                + "Antall opprettet: %d, antall slettet: %d",
            årstall, kvartal, resultat.getAntallRadOpprettet(), resultat.getAntallRadSlettet()));
  }
}
