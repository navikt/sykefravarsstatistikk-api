package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjeprogram.velgPrimærnæringskode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class BransjeprogramTest {

    @Test
    fun `velgPrimærnæringskode velger laveste næringskode fra bransjeprogrammet`() {
        val _11111 = Næringskode5Siffer("11111", "")
        val _86102 = Næringskode5Siffer("86102", "")
        val _88911 = Næringskode5Siffer("86102", "")
        val _22222 = Næringskode5Siffer("22222", "")

        val næringskoder = listOf(_11111, _88911, _86102, _22222)

        val primærnæringskode = velgPrimærnæringskode(næringskoder);
        assertEquals(_86102, primærnæringskode)
    }

    @Test
    fun `velgPrimærnæringskode velger laveste næringskode hvis ingen av dem er i bransjeprogrammet`() {
        val _11111 = Næringskode5Siffer("11111", "")
        val _33333 = Næringskode5Siffer("86102", "")
        val _22222 = Næringskode5Siffer("22222", "")

        val næringskoder = listOf(_11111, _33333, _22222)

        val primærnæringskode = velgPrimærnæringskode(næringskoder);
        assertEquals(_11111, primærnæringskode)
    }
}