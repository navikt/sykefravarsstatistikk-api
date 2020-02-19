package no.nav.tag.sykefravarsstatistikk.api.tapteDagsverkForKostnadsberegning;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KvartalsvisTapteDagsverkTest {

    @Test
    void getTapteDagsverk__skal_ikke_være_maskert_når_antall_personer_er_5_eller_mer() {
        KvartalsvisTapteDagsverk kvartalsvisTapteDagsverk = new KvartalsvisTapteDagsverk(new BigDecimal(100), 2019, 2, 5);
        assertEquals(kvartalsvisTapteDagsverk.getTapteDagsverk(), new BigDecimal(100));
        KvartalsvisTapteDagsverk kvartalsvisTapteDagsverk_6 = new KvartalsvisTapteDagsverk(new BigDecimal(100), 2019, 2, 6);
        assertEquals(kvartalsvisTapteDagsverk_6.getTapteDagsverk(), new BigDecimal(100));
    }

    @Test
    void getTapteDagsverk__skal_være_maskert_når_antall_personer_er_4_eller_færre() {
        KvartalsvisTapteDagsverk kvartalsvisTapteDagsverk = new KvartalsvisTapteDagsverk(new BigDecimal(100), 2019, 2, 4);
        assertEquals(kvartalsvisTapteDagsverk.getTapteDagsverk(), null);
        KvartalsvisTapteDagsverk kvartalsvisTapteDagsverk_3 = new KvartalsvisTapteDagsverk(new BigDecimal(100), 2019, 2, 3);
        assertEquals(kvartalsvisTapteDagsverk_3.getTapteDagsverk(), null);
    }

    @Test
    void isErMaskert__skal_ikke_være_true_når_antall_personer_er_5_eller_mer() {
        KvartalsvisTapteDagsverk kvartalsvisTapteDagsverk = new KvartalsvisTapteDagsverk(new BigDecimal(100), 2019, 2, 5);
        assertEquals(kvartalsvisTapteDagsverk.isErMaskert(), false);
        KvartalsvisTapteDagsverk kvartalsvisTapteDagsverk_6 = new KvartalsvisTapteDagsverk(new BigDecimal(100), 2019, 2, 6);
        assertEquals(kvartalsvisTapteDagsverk_6.isErMaskert(), false);
    }

    @Test
    void isErMaskert__skal_være_true_når_antall_personer_er_4_eller_færre() {
        KvartalsvisTapteDagsverk kvartalsvisTapteDagsverk = new KvartalsvisTapteDagsverk(new BigDecimal(100), 2019, 2, 4);
        assertEquals(kvartalsvisTapteDagsverk.isErMaskert(), true);
        KvartalsvisTapteDagsverk kvartalsvisTapteDagsverk_3 = new KvartalsvisTapteDagsverk(new BigDecimal(100), 2019, 2, 3);
        assertEquals(kvartalsvisTapteDagsverk_3.isErMaskert(), true);
    }
}
