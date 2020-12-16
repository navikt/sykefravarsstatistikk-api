package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.domene.bransjeprogram;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjetype;
import org.junit.jupiter.api.Test;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.enNæringskode5Siffer;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.enUnderenhetBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    public void bransje__skal_ikke_godta_koder_med_lengde_utenom_5_og_2() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Bransje(Bransjetype.SYKEHUS, "navn", "123")
        );
    }

    @Test
    public void bransje__skal_ikke_godta_koder_med_forskjellige_lengder() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Bransje(Bransjetype.SYKEHUS, "navn", "12", "12345")
        );
    }

    @Test
    public void bransje__skal_godta_koder_med_lengde_2_eller_5() {
        new Bransje(Bransjetype.SYKEHUS, "navn", "12", "34", "56");
        new Bransje(Bransjetype.SYKEHUS, "navn", "12345", "34567", "56789");
    }

    @Test
    public void lengdePåNæringskoder__skal_returnere_riktig_lengde() {
        assertThat(new Bransje(Bransjetype.SYKEHUS, "navn", "12345", "34567", "56789").lengdePåNæringskoder()).isEqualTo(5);
        assertThat(new Bransje(Bransjetype.SYKEHUS, "navn", "12", "34", "45").lengdePåNæringskoder()).isEqualTo(2);
    }

    @Test
    public void inkludererVirksomhet__skal_returnere_hvorvidt_virksomhetens_næring_er_i_bransjen() {
        Bransje bransje = new Bransje(Bransjetype.SYKEHUS, "navn", "12345", "34567", "56789");
        assertThat(bransje.inkludererVirksomhet(underenhetMedNæringskode("12345"))).isTrue();
        assertThat(bransje.inkludererVirksomhet(underenhetMedNæringskode("54321"))).isFalse();
    }


    private Underenhet underenhetMedNæringskode(String næringskode) {
        return enUnderenhetBuilder().næringskode(enNæringskode5Siffer(næringskode)).build();
    }

}
