package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importering.SykefraværsstatistikkImporteringService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class ImporteringServiceTest {
    var importeringService = SykefraværsstatistikkImporteringService(mock(), mock(), mock(),  mock())

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
}
