package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importering.SykefraværsstatistikkImporteringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.ÅrstallOgKvartal;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

public class ImporteringServiceTest {

  Environment environment;
  SykefraværsstatistikkImporteringService importeringService =
      new SykefraværsstatistikkImporteringService(null, null, null, true, null);

  @Test
  public void
      kanImportStartes__returnerer_TRUE_dersom_alle_årstall_og_kvartal_er_like_OG_sykefraværsstatistikk_ligger_ett_kvartal_bak_Dvh() {
    assertFalse(
        importeringService.kanImportStartes(
            List.of(new ÅrstallOgKvartal(2019, 3)), List.of(new ÅrstallOgKvartal(2020, 1))));

    assertFalse(
        importeringService.kanImportStartes(
            Arrays.asList(new ÅrstallOgKvartal(2019, 4), new ÅrstallOgKvartal(2019, 3)),
            Arrays.asList(new ÅrstallOgKvartal(2020, 1), new ÅrstallOgKvartal(2020, 1))));

    assertTrue(
        importeringService.kanImportStartes(
            Arrays.asList(new ÅrstallOgKvartal(2019, 4), new ÅrstallOgKvartal(2019, 4)),
            Arrays.asList(new ÅrstallOgKvartal(2020, 1), new ÅrstallOgKvartal(2020, 1))));
  }
}
