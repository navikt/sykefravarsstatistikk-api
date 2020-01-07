package no.nav.tag.sykefravarsstatistikk.api.domene;

import no.nav.tag.sykefravarsstatistikk.api.domene.bransjeprogram.Bransje;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import org.junit.Test;

import java.util.Arrays;

import static no.nav.tag.sykefravarsstatistikk.api.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

public class BransjeTest {

    @Test
    public void virksomhetTilhørerBransjeprogram__skal_gi_true_hvis_næringskode_starter_med_de_definerte_sifrene() {
        Bransje bransje = new Bransje(
                "test",
                Arrays.asList("12", "45", "12345")
        );

        assertThat(bransje.inkludererVirksomhet(underenhetMedNæringskode("45512"))).isTrue();
        assertThat(bransje.inkludererVirksomhet(underenhetMedNæringskode("12345"))).isTrue();
    }

    @Test
    public void virksomhetTilhørerBransjeprogram__skal_gi_false_hvis_næringskode_ikke_starter_med_de_definerte_sifrene() {
        Bransje bransje = new Bransje(
                "test",
                Arrays.asList("12", "45")
        );

        assertThat(bransje.inkludererVirksomhet(underenhetMedNæringskode("46512"))).isFalse();
    }

    private Underenhet underenhetMedNæringskode(String næringskode) {
        return enUnderenhetBuilder().næringskode(enNæringskode5Siffer(næringskode)).build();
    }

}
