package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram;

import ia.felles.definisjoner.bransjer.Bransjer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.BransjeEllerNæring;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BransjeEllerNæringTest {

    @Test
    public void BransjeEllerNæring_kan_opprettes_for_en_bransje_og_returnerer_en_bransje() {
        Bransje bransje = new Bransje(Bransjer.BARNEHAGER);
        BransjeEllerNæring bransjeEllerNæring = new BransjeEllerNæring(bransje);

        assertThat(bransjeEllerNæring.isBransje()).isEqualTo(true);
        assertThat(bransjeEllerNæring.getBransje()).isEqualTo(bransje);
        assertThrows(NoSuchElementException.class, bransjeEllerNæring::getNæring);
    }

    @Test
    public void BransjeEllerNæring_kan_opprettes_for_en_næring_og_returnerer_en_næring() {
        Næring næring = new Næring("61");
        BransjeEllerNæring bransjeEllerNæring = new BransjeEllerNæring(næring);

        assertThat(bransjeEllerNæring.isBransje()).isEqualTo(false);
        assertThat(bransjeEllerNæring.getNæring()).isEqualTo(næring);
        assertThrows(NoSuchElementException.class, bransjeEllerNæring::getBransje);
    }
}
