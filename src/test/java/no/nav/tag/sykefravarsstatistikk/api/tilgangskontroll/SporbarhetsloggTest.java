package no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll;

import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import org.junit.Test;

import static no.nav.tag.sykefravarsstatistikk.api.TestUtils.getInnloggetBruker;

public class SporbarhetsloggTest {

    @Test
    public void loggHendelse__skal_ikke_feile_ved_nullfelter() {
        Sporbarhetslogg sporbarhetslogg = new Sporbarhetslogg();
        sporbarhetslogg.loggHendelse(
                null,
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