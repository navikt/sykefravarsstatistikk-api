package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.domene.ÅrstallOgKvartal;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ImporteringServiceTest {

    ImporteringService importeringService = new ImporteringService(null, null, true);

    @Test
    public void kanImportStartes__returnerer_TRUE_dersom_alle_årstall_og_kvartal_er_like_OG_sykefraværsstatistikk_ligger_ett_kvartal_bak_Dvh() {
        assertFalse(
                importeringService.kanImportStartes(
                        Arrays.asList(new ÅrstallOgKvartal(2019, 3)),
                        Arrays.asList(new ÅrstallOgKvartal(2020, 1))
                )
        );

        assertFalse(
                importeringService.kanImportStartes(
                        Arrays.asList(
                                new ÅrstallOgKvartal(2019, 4),
                                new ÅrstallOgKvartal(2019, 3)
                        ),
                        Arrays.asList(
                                new ÅrstallOgKvartal(2020, 1),
                                new ÅrstallOgKvartal(2020, 1)
                        )
                )
        );

        assertTrue(
                importeringService.kanImportStartes(
                        Arrays.asList(
                                new ÅrstallOgKvartal(2019, 4),
                                new ÅrstallOgKvartal(2019, 4)
                        ),
                        Arrays.asList(
                                new ÅrstallOgKvartal(2020, 1),
                                new ÅrstallOgKvartal(2020, 1)
                        )
                )
        );

    }
}
