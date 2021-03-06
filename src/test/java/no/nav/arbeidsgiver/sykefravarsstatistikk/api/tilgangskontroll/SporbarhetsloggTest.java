package no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.sporbarhet.Loggevent;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.sporbarhet.Sporbarhetslogg;
import org.junit.jupiter.api.Test;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.getInnloggetBruker;

public class SporbarhetsloggTest {

    @Test
    public void loggHendelse__skal_ikke_feile_ved_nullfelter() {
        Sporbarhetslogg sporbarhetslogg = new Sporbarhetslogg();

        sporbarhetslogg.loggHendelse(new Loggevent(
                getInnloggetBruker(),
                new Orgnr(null),
                false,
                null,
                null,
                null,
                null
        ));

        sporbarhetslogg.loggHendelse(new Loggevent(
                getInnloggetBruker(),
                new Orgnr(null),
                false,
                null,
                null,
                null,
                null
        ), null);
    }
}
