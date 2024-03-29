package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class ImporteringServiceTest {
    var importeringService =
        SykefraværsstatistikkImporteringService(
            mock(),
            mock(),
            mock(),
            mock(),
            mock(),
            mock(),
            mock(),
            mock(),
            mock(),
            mock(),
            mock(),
            mock(),
            mock(),
            mock(),
        )

    @Test
    fun kanImportStartes__returnerer_TRUE_dersom_alle_årstall_og_kvartal_er_like_OG_sykefraværsstatistikk_ligger_ett_kvartal_bak_Dvh() {
        Assertions.assertFalse(
            importeringService.kanImportStartes(
                listOf(ÅrstallOgKvartal(2019, 3)), listOf(ÅrstallOgKvartal(2020, 1))
            )
        )
        Assertions.assertFalse(
            importeringService.kanImportStartes(
                listOf(ÅrstallOgKvartal(2019, 4), ÅrstallOgKvartal(2019, 3)),
                listOf(ÅrstallOgKvartal(2020, 1), ÅrstallOgKvartal(2020, 1))
            )
        )
        Assertions.assertTrue(
            importeringService.kanImportStartes(
                listOf(ÅrstallOgKvartal(2019, 4), ÅrstallOgKvartal(2019, 4)),
                listOf(ÅrstallOgKvartal(2020, 1), ÅrstallOgKvartal(2020, 1))
            )
        )
    }

    @Test
    fun `import kan starte dersom vi mangler et kvartal i en av våre tabeller (dette kan skje når import tryner)`() {
        Assertions.assertTrue(
                importeringService.kanImportStartes(
                        årstallOgKvartalForSfsDb = listOf(
                                ÅrstallOgKvartal(2023, 3),
                                ÅrstallOgKvartal(2023, 3),
                                ÅrstallOgKvartal(2023, 2),
                                ÅrstallOgKvartal(2023, 2),
                        ),
                        årstallOgKvartalForDvh = listOf(ÅrstallOgKvartal(2023, 3))
                )
        )
    }

    @Test
    fun `import skal IKKE starte dersom vi mangler flere kvartal i våre tabeller (dette skal IKKE skje)`() {
        Assertions.assertFalse(
                importeringService.kanImportStartes(
                        årstallOgKvartalForSfsDb = listOf(
                                ÅrstallOgKvartal(2023, 3),
                                ÅrstallOgKvartal(2023, 3),
                                ÅrstallOgKvartal(2023, 1),
                                ÅrstallOgKvartal(2023, 2),
                        ),
                        årstallOgKvartalForDvh = listOf(ÅrstallOgKvartal(2023, 3))
                )
        )
    }
}
