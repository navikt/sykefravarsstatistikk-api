package no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.Orgnr;
import org.junit.Test;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.getInnloggetBruker;

public class SporbarhetsloggTest {

    @Test
    public void loggHendelse__skal_ikke_feile_ved_nullfelter() {
        Sporbarhetslogg sporbarhetslogg = new Sporbarhetslogg();
        sporbarhetslogg.loggHendelse(
                getInnloggetBruker(),
                new Orgnr(null),
                false,
                null,
                null,
                null,
                null
        );
    }
}
