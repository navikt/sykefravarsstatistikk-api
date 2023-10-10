package no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.innloggetBruker
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.sporbarhetslog.Loggevent
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.sporbarhetslog.Sporbarhetslogg
import org.junit.jupiter.api.Test

class SporbarhetsloggTest {
    @Test
    fun loggHendelse__skal_ikke_feile_ved_nullfelter() {
        val sporbarhetslogg = Sporbarhetslogg()
        sporbarhetslogg.loggHendelse(
            Loggevent(innloggetBruker, null, false, null, null, null, null)
        )
        sporbarhetslogg.loggHendelse(
            Loggevent(innloggetBruker, null, false, null, null, null, null), null
        )
    }
}
