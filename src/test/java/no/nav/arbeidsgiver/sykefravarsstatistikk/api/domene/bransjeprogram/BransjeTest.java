package no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.bransjeprogram;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import org.junit.Test;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.enNæringskode5Siffer;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.enUnderenhetBuilder;
import static org.assertj.core.api.Assertions.assertThat;

public class BransjeTest {

    @Test(expected = IllegalArgumentException.class)
    public void bransje__skal_ikke_godta_koder_med_lengde_utenom_5_og_2() {
        new Bransje("navn", "123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void bransje__skal_ikke_godta_koder_med_forskjellige_lengder() {
        new Bransje("navn", "12", "12345");
    }

    @Test
    public void bransje__skal_godta_koder_med_lengde_2_eller_5() {
        new Bransje("navn", "12", "34", "56");
        new Bransje("navn", "12345", "34567", "56789");
    }

    @Test
    public void lengdePåNæringskoder__skal_returnere_riktig_lengde() {
        assertThat(new Bransje("navn", "12345", "34567", "56789").lengdePåNæringskoder()).isEqualTo(5);
        assertThat(new Bransje("navn", "12", "34", "45").lengdePåNæringskoder()).isEqualTo(2);
    }

    @Test
    public void inkludererVirksomhet__skal_returnere_hvorvidt_virksomhetens_næring_er_i_bransjen() {
        Bransje bransje = new Bransje("navn", "12345", "34567", "56789");
        assertThat(bransje.inkludererVirksomhet(underenhetMedNæringskode("12345"))).isTrue();
        assertThat(bransje.inkludererVirksomhet(underenhetMedNæringskode("54321"))).isFalse();
    }

    private Underenhet underenhetMedNæringskode(String næringskode5siffer) {
        return enUnderenhetBuilder().næringskode(enNæringskode5Siffer(næringskode5siffer)).build();
    }
}
