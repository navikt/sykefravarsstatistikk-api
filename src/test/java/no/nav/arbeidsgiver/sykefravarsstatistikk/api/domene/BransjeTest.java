package no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.bransjeprogram.Bransjetype;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import org.junit.Test;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.enNæringskode5Siffer;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.enUnderenhetBuilder;
import static org.assertj.core.api.Assertions.assertThat;

public class BransjeTest {

    @Test
    public void virksomhetTilhørerBransjeprogram__skal_gi_true_hvis_næringskode_starter_med_de_definerte_sifrene() {
        Bransje bransje = new Bransje(Bransjetype.SYKEHUS, "test", "12", "45");
        assertThat(bransje.inkludererVirksomhet(underenhetMedNæringskode("45512"))).isTrue();
    }

    @Test
    public void virksomhetTilhørerBransjeprogram__skal_gi_false_hvis_næringskode_ikke_starter_med_de_definerte_sifrene() {
        Bransje bransje = new Bransje(Bransjetype.SYKEHUS, "test", "12", "45");
        assertThat(bransje.inkludererVirksomhet(underenhetMedNæringskode("46512"))).isFalse();
    }

    private Underenhet underenhetMedNæringskode(String næringskode) {
        return enUnderenhetBuilder().næringskode(enNæringskode5Siffer(næringskode)).build();
    }

}
